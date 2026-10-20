package my.utem.ftmk.silverlink

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import my.utem.ftmk.silverlink.databinding.FragmentHomeBinding
import my.utem.ftmk.silverlink.databinding.FragmentRequestBinding
import my.utem.ftmk.silverlink.databinding.FragmentReviewBinding
import my.utem.ftmk.silverlink.databinding.FragmentTrackingBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : ScreenFragment<FragmentHomeBinding>(R.layout.fragment_home, FragmentHomeBinding::bind) {
    override fun onReady() = with(binding) {
        requestHelpButton.setOnClickListener { host.open(Screen.REQUEST) }
        parentOptions.setOnClickListener { notice(R.string.support_title, R.string.support_details) }
        recentSupportButton.setOnClickListener { notice(R.string.recent_title, R.string.recent_details) }
    }
}

class RequestFragment : ScreenFragment<FragmentRequestBinding>(R.layout.fragment_request, FragmentRequestBinding::bind) {
    override fun onReady() = with(binding) {
        requestTitle.setText(model.draftTitle)
        requestNotes.setText(model.draftNotes)
        requestWhen.text = model.draftWhen
        requestLocation.text = model.draftLocation
        recipientToggle.check(if (model.draftForParent) R.id.recipient_parent else R.id.recipient_self)
        updateRecipient()
        updateCategory()
        requestTitle.doAfterTextChanged { model.draftTitle = it.toString() }
        requestNotes.doAfterTextChanged { model.draftNotes = it.toString() }
        recipientToggle.addOnButtonCheckedListener { _, id, checked ->
            if (checked) {
                model.draftForParent = id == R.id.recipient_parent
                model.draftLocation = getString(if (model.draftForParent) R.string.mum_address else R.string.own_address)
                updateRecipient()
                requestLocation.text = model.draftLocation
            }
        }
        val categories = mapOf(categoryErrands to TaskCategory.ERRANDS, categoryCompanionship to TaskCategory.COMPANIONSHIP,
            categoryHousehold to TaskCategory.HOUSEHOLD, categoryDigital to TaskCategory.DIGITAL)
        categories.forEach { (button, category) -> button.setOnClickListener { model.draftCategory = category; updateCategory() } }
        requestWhen.setOnClickListener { pickDateTime() }
        requestLocation.setOnClickListener {
            val addresses = arrayOf(getString(R.string.mum_address), getString(R.string.own_address))
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.choose_location)
                .setItems(addresses) { _, index -> model.draftLocation = addresses[index]; requestLocation.text = addresses[index] }.show()
        }
        broadcastButton.setOnClickListener {
            if (requestTitle.text.isNullOrBlank()) {
                requestTitle.error = getString(R.string.field_required)
                requestTitle.requestFocus()
            } else if (requestNotes.text.isNullOrBlank()) {
                requestNotes.error = getString(R.string.field_required)
                requestNotes.requestFocus()
            } else {
                model.publishRequest()
                val activity = host
                activity.open(Screen.TRACKING, topLevel = true)
                activity.notice(R.string.request_sent)
            }
        }
    }

    private fun updateRecipient() = with(binding) {
        recipientDescription.setText(if (model.draftForParent) R.string.parent_description else R.string.myself_description)
    }

    private fun updateCategory() = with(binding) {
        val buttons = listOf(categoryErrands, categoryCompanionship, categoryHousehold, categoryDigital)
        buttons.zip(TaskCategory.entries).forEach { (button, category) ->
            val label = SpannableString(button.text)
            val subtitleStart = label.indexOf('\n') + 1
            if (subtitleStart > 0) {
                label.setSpan(AbsoluteSizeSpan(12, true), subtitleStart, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                label.setSpan(ForegroundColorSpan(requireContext().getColor(R.color.sl_muted)), subtitleStart, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                button.text = label
            }
            val selected = model.draftCategory == category
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(if (selected) R.color.sl_tint else R.color.sl_surface))
            button.strokeColor = android.content.res.ColorStateList.valueOf(requireContext().getColor(if (selected) R.color.sl_primary else R.color.sl_border))
            button.isSelected = selected
        }
        pointsPolicy.text = getString(R.string.policy_format, getString(model.draftCategory.label), model.draftCategory.points)
    }

    private fun pickDateTime() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(year, month, day)
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val date = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(calendar.time)
                val time = DateFormat.getTimeFormat(requireContext())
                val start = time.format(calendar.time)
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                model.draftWhen = getString(R.string.date_time_format, date, start, time.format(calendar.time))
                if (view != null) binding.requestWhen.text = model.draftWhen
            }, 16, 0, DateFormat.is24HourFormat(requireContext())).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}

class TrackingFragment : ScreenFragment<FragmentTrackingBinding>(R.layout.fragment_tracking, FragmentTrackingBinding::bind) {
    override fun onReady() = with(binding) {
        bindRequestSummary(root, includeCategory = false)
        trackingPointsGuidance.text = getString(R.string.tracking_points_format, model.category.points)
        if (model.stage == RequestStage.OPEN) {
            trackingTitle.setText(R.string.tracking_pending_title)
            trackingSubtitle.setText(R.string.tracking_pending_subtitle)
        } else if (model.stage >= RequestStage.SUBMITTED) {
            trackingTitle.setText(R.string.tracking_review_title)
            trackingSubtitle.setText(R.string.tracking_review_subtitle)
        } else trackingSubtitle.setText(R.string.tracking_accepted_subtitle)
        journeyCard.isVisible = model.stage != RequestStage.OPEN
        timelineAccepted.alpha = if (model.stage >= RequestStage.ACCEPTED) 1f else 0.4f
        timelineProgress.alpha = if (model.stage >= RequestStage.ACCEPTED) 1f else 0.4f
        timelineVerification.alpha = if (model.stage >= RequestStage.SUBMITTED) 1f else 0.4f
        (summaryCategory.getChildAt(summaryCategory.childCount - 1) as TextView).setText(
            when (model.stage) {
                RequestStage.OPEN -> R.string.tracking_status_open
                RequestStage.ACCEPTED -> R.string.in_progress
                RequestStage.SUBMITTED -> R.string.pending_verification
                RequestStage.VERIFIED -> R.string.completion_verified
            }
        )
        reviewCompletionButton.isVisible = model.stage >= RequestStage.SUBMITTED
        reviewCompletionButton.setOnClickListener { host.open(Screen.REVIEW) }
        messageButton.setOnClickListener { notice(R.string.message_title, R.string.message_unavailable) }
    }
}

class ReviewFragment : ScreenFragment<FragmentReviewBinding>(R.layout.fragment_review, FragmentReviewBinding::bind) {
    override fun onReady() {
        renderReview()
        binding.deliveryPhotoButton.setOnClickListener { showPhoto() }
        binding.reviewActionButton.setOnClickListener {
            if (model.stage == RequestStage.SUBMITTED) {
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.confirm_received)
                    .setMessage(R.string.pending_record_description)
                    .setPositiveButton(R.string.confirm_received) { _, _ ->
                        if (model.verifyCompletion()) { renderReview(); host.notice(R.string.verified_snackbar) }
                    }.setNegativeButton(R.string.cancel, null).show()
            } else host.open(Screen.TRACKING, topLevel = true)
        }
    }

    private fun renderReview() = with(binding) {
        val verified = model.stage == RequestStage.VERIFIED
        summaryTitle.text = model.title
        summaryRecipient.setText(if (model.forParent) R.string.for_parent else R.string.for_self)
        reviewNotes.text = model.completionNotes
        reviewCategory.text = getString(R.string.request_summary_category, getString(model.category.label))
        fixedAward.text = getString(R.string.fixed_format, model.category.points)
        (verifiedPill.getChildAt(verifiedPill.childCount - 1) as TextView).setText(if (verified) R.string.completion_verified else R.string.pending_verification)
        reviewTitle.setText(when {
            verified && model.forParent -> R.string.mum_s_help_is_complete
            verified -> R.string.self_help_complete
            model.forParent -> R.string.awaiting_review
            else -> R.string.self_help_review
        })
        reviewDescription.setText(when {
            !verified && model.forParent -> R.string.awaiting_review_description
            !verified -> R.string.pending_record_description
            model.forParent -> R.string.review_verified_description
            else -> R.string.self_help_verified_description
        })
        verificationRecordTitle.setText(when {
            !verified && model.forParent -> R.string.pending_record_title
            !verified -> R.string.confirm_received
            model.forParent -> R.string.i_confirmed_with_mum
            else -> R.string.self_help_received
        })
        verificationRecordDescription.setText(if (verified) R.string.the_agreed_help_was_received_verified_by_priya_today_4_45_pm else R.string.pending_record_description)
        awardTitle.text = if (verified) getString(R.string.award_format, model.category.points) else getString(R.string.points_format, model.category.points)
        awardDescription.setText(if (verified) R.string.awarded_to_daniel_tan else R.string.pending_award)
        reviewActionButton.setText(if (verified) R.string.back_to_my_requests else R.string.confirm_received)
    }
}

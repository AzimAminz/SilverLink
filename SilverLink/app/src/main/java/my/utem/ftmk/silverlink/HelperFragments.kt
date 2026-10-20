package my.utem.ftmk.silverlink

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import my.utem.ftmk.silverlink.databinding.FragmentNearbyBinding
import my.utem.ftmk.silverlink.databinding.FragmentTaskBinding

class NearbyFragment : ScreenFragment<FragmentNearbyBinding>(R.layout.fragment_nearby, FragmentNearbyBinding::bind) {
    override fun onReady() = with(binding) {
        bindRequestSummary(root)
        nearbyWhen.text = model.whenText
        taskFilters.check(model.nearbyFilter)
        filterRequests()
        taskFilters.setOnCheckedStateChangeListener { _, ids ->
            model.nearbyFilter = ids.firstOrNull() ?: R.id.filter_all
            filterRequests()
        }
        acceptRequestButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.confirm_accept)
                .setMessage(R.string.accept_detail)
                .setPositiveButton(R.string.accept) { _, _ ->
                    if (model.acceptRequest()) host.open(Screen.TASK)
                }.setNegativeButton(R.string.cancel, null).show()
        }
        teaDetailsButton.setOnClickListener { notice(R.string.task_details, R.string.secondary_task) }
        videoDetailsButton.setOnClickListener { notice(R.string.task_details, R.string.secondary_task) }
    }

    private fun filterRequests() = with(binding) {
        val filter = model.nearbyFilter
        val matches = filter == R.id.filter_all || (filter == R.id.filter_errands && model.category == TaskCategory.ERRANDS) ||
            (filter == R.id.filter_companionship && model.category == TaskCategory.COMPANIONSHIP)
        errandsCard.isVisible = matches && model.stage == RequestStage.OPEN
        teaCard.isVisible = filter != R.id.filter_errands
        videoCard.isVisible = filter == R.id.filter_all
        val count = listOf(errandsCard, teaCard, videoCard).count { it.isVisible }
        openRequestCount.text = resources.getQuantityString(R.plurals.open_requests_count, count, count)
        emptyRequests.isVisible = count == 0
    }
}

class TaskFragment : ScreenFragment<FragmentTaskBinding>(R.layout.fragment_task, FragmentTaskBinding::bind) {
    private val pickPhoto = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val previous = model.photoUri
            model.photoUri = uri.toString()
            if (view != null && loadEvidence(binding.evidencePhoto)) {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Some document providers grant access for this session only.
                }
                host.notice(R.string.photo_replaced)
            } else {
                model.photoUri = previous
                host.notice(R.string.photo_error)
            }
        }
    }

    override fun onReady() = with(binding) {
        bindRequestSummary(root)
        taskWhen.text = model.whenText
        taskLocation.text = model.location
        taskInstructions.text = model.notes
        taskPendingPoints.text = getString(R.string.pending_format, model.category.points)
        completionNotes.setText(model.completionNotes)
        completionConfirmed.isChecked = model.completionConfirmed
        if (!model.forParent) completionConfirmed.setText(R.string.self_completion_confirmation)
        completionNotes.doAfterTextChanged { model.completionNotes = it.toString() }
        completionConfirmed.setOnCheckedChangeListener { _, checked -> model.completionConfirmed = checked }
        if (!loadEvidence(evidencePhoto)) host.notice(R.string.photo_error)
        replacePhotoButton.setOnClickListener { pickPhoto.launch(arrayOf("image/*")) }
        evidencePhoto.setOnClickListener { showPhoto() }
        taskChatButton.setOnClickListener { notice(R.string.chat_title, R.string.message_unavailable) }
        renderStatus()
        sendVerificationButton.setOnClickListener {
            when {
                completionNotes.text.isNullOrBlank() -> {
                    completionNotes.error = getString(R.string.notes_required)
                    completionNotes.requestFocus()
                }
                !completionConfirmed.isChecked -> host.notice(R.string.confirm_required)
                model.submitCompletion() -> {
                    renderStatus()
                    host.notice(R.string.submitted)
                }
            }
        }
    }

    private fun renderStatus() = with(binding) {
        val editable = model.stage == RequestStage.ACCEPTED
        completionNotes.isEnabled = editable
        completionConfirmed.isEnabled = editable
        replacePhotoButton.isEnabled = editable
        sendVerificationButton.isEnabled = editable
        sendVerificationButton.setText(when (model.stage) {
            RequestStage.SUBMITTED -> R.string.awaiting_priya
            RequestStage.VERIFIED -> R.string.already_verified
            else -> R.string.send_for_verification
        })
        if (model.stage == RequestStage.VERIFIED) taskPendingPoints.text = getString(R.string.award_format, model.category.points)
    }
}

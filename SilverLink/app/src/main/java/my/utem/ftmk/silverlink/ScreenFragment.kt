package my.utem.ftmk.silverlink

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.core.net.toUri
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import my.utem.ftmk.silverlink.databinding.DialogPhotoBinding

abstract class ScreenFragment<B : ViewBinding>(
    @LayoutRes layout: Int,
    private val bind: (View) -> B
) : Fragment(layout) {
    private var viewBinding: B? = null
    protected val binding: B get() = checkNotNull(viewBinding)
    protected val host: MainActivity get() = requireActivity() as MainActivity
    protected val model: DemoViewModel get() = host.model

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = bind(view)
        onReady()
    }

    protected abstract fun onReady()

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }

    protected fun notice(title: Int, message: Int) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(title).setMessage(message)
            .setPositiveButton(R.string.close, null).show()
    }

    protected fun bindRequestSummary(root: View, includeCategory: Boolean = true) {
        root.findViewById<TextView>(R.id.summary_title)?.text = model.title
        root.findViewById<TextView>(R.id.summary_points)?.text = getString(R.string.points_format, model.category.points)
        root.findViewById<TextView>(R.id.summary_recipient)?.setText(if (model.forParent) R.string.for_parent else R.string.for_self)
        if (includeCategory) root.findViewById<LinearLayout>(R.id.summary_category)?.let {
            (it.getChildAt(it.childCount - 1) as TextView).text = getString(R.string.request_summary_category, getString(model.category.label))
        }
    }

    /** Decode a bounded image from the system document picker; never load a full camera image. */
    protected fun loadEvidence(image: ImageView): Boolean {
        val source = model.photoUri ?: return true
        return try {
            val uri = source.toUri()
            val resolver = requireContext().contentResolver
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            if (options.outWidth <= 0 || options.outHeight <= 0) return false
            options.inSampleSize = 1
            while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > 1200) options.inSampleSize *= 2
            options.inJustDecodeBounds = false
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return false
            image.setImageBitmap(bitmap)
            true
        } catch (_: java.io.IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    protected fun showPhoto() {
        val photo = DialogPhotoBinding.inflate(layoutInflater)
        if (!loadEvidence(photo.fullPhoto)) host.notice(R.string.photo_error)
        MaterialAlertDialogBuilder(requireContext()).setView(photo.root).setPositiveButton(R.string.close, null).show()
    }
}

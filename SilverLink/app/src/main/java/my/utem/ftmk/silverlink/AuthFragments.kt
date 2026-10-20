package my.utem.ftmk.silverlink

import android.util.Patterns
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import my.utem.ftmk.silverlink.databinding.DialogLoginBinding
import my.utem.ftmk.silverlink.databinding.FragmentSignupBinding
import my.utem.ftmk.silverlink.databinding.FragmentWelcomeBinding

class WelcomeFragment : ScreenFragment<FragmentWelcomeBinding>(R.layout.fragment_welcome, FragmentWelcomeBinding::bind) {
    override fun onReady() = with(binding) {
        val malay = resources.configuration.locales[0].language == "ms"
        languageToggle.check(if (malay) R.id.language_bm else R.id.language_en)
        languageToggle.addOnButtonCheckedListener { _, checkedId, checked ->
            if (checked) {
                val language = if (checkedId == R.id.language_bm) "ms" else "en"
                if (resources.configuration.locales[0].language != language) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
                }
            }
        }
        loginButton.setOnClickListener { chooseDemoRole() }
        signupButton.setOnClickListener { host.open(Screen.SIGNUP) }
        googleButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.demo_title)
                .setMessage(R.string.google_unavailable)
                .setPositiveButton(R.string.enter_demo) { _, _ -> chooseDemoRole() }
                .setNegativeButton(R.string.cancel, null).show()
        }
    }

    private fun chooseDemoRole() {
        val form = DialogLoginBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext()).setView(form.root)
            .setPositiveButton(R.string.enter_demo) { _, _ ->
                host.enterDemo(if (form.loginRole.checkedRadioButtonId == R.id.login_helper) Role.HELPER else Role.REQUESTER)
            }.setNegativeButton(R.string.cancel, null).show()
    }
}

class SignupFragment : ScreenFragment<FragmentSignupBinding>(R.layout.fragment_signup, FragmentSignupBinding::bind) {
    override fun onReady() = with(binding) {
        createProfileButton.setOnClickListener {
            val inputs = listOf(fullName, email, township, address)
            var valid = true
            for (input in inputs) {
                input.error = if (input.text.isNullOrBlank()) getString(R.string.field_required) else null
                valid = valid && input.error == null
            }
            if (!email.text.isNullOrBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()) {
                email.error = getString(R.string.email_invalid)
                valid = false
            }
            if (valid) host.enterDemo(if (signupRole.checkedRadioButtonId == R.id.role_helper) Role.HELPER else Role.REQUESTER)
        }
    }
}

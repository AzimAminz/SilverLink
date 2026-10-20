package my.utem.ftmk.silverlink

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import my.utem.ftmk.silverlink.databinding.ActivityMainBinding

enum class Screen { WELCOME, SIGNUP, HOME, REQUEST, TRACKING, NEARBY, TASK, REVIEW, REWARDS }

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val model: DemoViewModel by lazy { ViewModelProvider(this)[DemoViewModel::class.java] }
    private var updatingNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
            // Insets are handled by the shell; Material navigation must not apply them twice.
            WindowInsetsCompat.CONSUMED
        }
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.roleButton.setOnClickListener { chooseRole() }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (updatingNavigation) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> open(Screen.HOME, topLevel = true)
                R.id.nav_requests -> open(if (model.stage >= RequestStage.SUBMITTED) Screen.REVIEW else Screen.TRACKING, topLevel = true)
                R.id.nav_community, R.id.nav_rewards -> open(Screen.REWARDS, topLevel = true)
                R.id.nav_nearby -> open(Screen.NEARBY, topLevel = true)
                R.id.nav_tasks -> if (model.stage == RequestStage.OPEN) {
                    notice(R.string.no_active_task)
                    return@setOnItemSelectedListener false
                } else open(Screen.TASK, topLevel = true)
                R.id.nav_profile -> {
                    showProfile()
                    return@setOnItemSelectedListener false
                }
            }
            true
        }
        supportFragmentManager.addOnBackStackChangedListener { updateChrome() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStackImmediate()
                    currentScreen() !in listOf(Screen.WELCOME, Screen.HOME, Screen.NEARBY) -> open(if (model.role == Role.HELPER) Screen.NEARBY else Screen.HOME, topLevel = true)
                    else -> { isEnabled = false; onBackPressedDispatcher.onBackPressed(); isEnabled = true }
                }
            }
        })
        if (savedInstanceState == null) open(Screen.WELCOME, topLevel = true)
        updateChrome()
    }

    fun currentScreen(): Screen = supportFragmentManager.findFragmentById(R.id.screen_container)
        ?.tag?.let { Screen.valueOf(it) } ?: Screen.WELCOME

    fun open(screen: Screen, topLevel: Boolean = false) {
        if (supportFragmentManager.isStateSaved) return
        if (topLevel) supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        if (currentScreen() == screen && supportFragmentManager.findFragmentById(R.id.screen_container) != null) {
            updateChrome()
            return
        }
        val fragment: Fragment = when (screen) {
            Screen.WELCOME -> WelcomeFragment()
            Screen.SIGNUP -> SignupFragment()
            Screen.HOME -> HomeFragment()
            Screen.REQUEST -> RequestFragment()
            Screen.TRACKING -> TrackingFragment()
            Screen.NEARBY -> NearbyFragment()
            Screen.TASK -> TaskFragment()
            Screen.REVIEW -> ReviewFragment()
            Screen.REWARDS -> RewardsFragment()
        }
        val transaction = supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.screen_container, fragment, screen.name)
        if (!topLevel) transaction.addToBackStack(screen.name)
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
        updateChrome()
    }

    fun enterDemo(role: Role) {
        model.role = role
        open(if (role == Role.REQUESTER) Screen.HOME else Screen.NEARBY, topLevel = true)
    }

    private fun updateChrome() {
        if (!::binding.isInitialized) return
        val screen = currentScreen()
        val welcome = screen == Screen.WELCOME
        binding.appBar.isVisible = !welcome
        binding.bottomNavigation.isVisible = screen !in listOf(Screen.WELCOME, Screen.SIGNUP)
        val back = screen in listOf(Screen.SIGNUP, Screen.REQUEST, Screen.TRACKING, Screen.TASK, Screen.REVIEW)
        binding.backButton.isVisible = back
        binding.brandMark.isVisible = !back
        binding.toolbarTitle.setText(when (screen) {
            Screen.REQUEST -> R.string.request_help
            Screen.TRACKING -> R.string.your_request
            Screen.TASK -> R.string.your_task
            Screen.REVIEW -> R.string.completion_review
            Screen.REWARDS -> R.string.community_rewards
            else -> R.string.app_name
        })
        binding.roleButton.isVisible = screen != Screen.SIGNUP
        binding.roleButton.setText(if (model.role == Role.REQUESTER) R.string.requester else R.string.helper)
        binding.roleButton.contentDescription = getString(R.string.switch_role)
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = !welcome
            isAppearanceLightNavigationBars = !welcome
        }
        binding.root.setBackgroundColor(getColor(if (welcome) R.color.sl_muted else R.color.sl_background))
        updatingNavigation = true
        val helper = model.role == Role.HELPER
        val expected = if (helper) R.id.nav_nearby else R.id.nav_home
        if (binding.bottomNavigation.menu.findItem(expected) == null) {
            binding.bottomNavigation.menu.clear()
            binding.bottomNavigation.inflateMenu(if (helper) R.menu.navigation_helper else R.menu.navigation_requester)
        }
        binding.bottomNavigation.selectedItemId = when (screen) {
            Screen.HOME -> R.id.nav_home
            Screen.REQUEST, Screen.TRACKING, Screen.REVIEW -> R.id.nav_requests
            Screen.NEARBY -> R.id.nav_nearby
            Screen.TASK -> R.id.nav_tasks
            Screen.REWARDS -> if (helper) R.id.nav_rewards else R.id.nav_community
            else -> expected
        }
        updatingNavigation = false
    }

    private fun chooseRole() {
        MaterialAlertDialogBuilder(this).setTitle(R.string.switch_role).setMessage(R.string.switch_demo_role)
            .setPositiveButton(R.string.requester) { _, _ -> enterDemo(Role.REQUESTER) }
            .setNegativeButton(R.string.helper) { _, _ -> enterDemo(Role.HELPER) }.show()
    }

    private fun showProfile() {
        MaterialAlertDialogBuilder(this).setTitle(if (model.role == Role.HELPER) "Daniel Tan" else "Priya Menon")
            .setMessage(R.string.demo_profile)
            .setPositiveButton(if (model.role == Role.HELPER) R.string.switch_to_requester else R.string.switch_to_helper) { _, _ ->
                enterDemo(if (model.role == Role.HELPER) Role.REQUESTER else Role.HELPER)
            }.setNegativeButton(R.string.sign_out) { _, _ -> open(Screen.WELCOME, topLevel = true) }
            .setNeutralButton(R.string.close, null).show()
    }

    fun notice(message: Int) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (binding.bottomNavigation.isVisible) snackbar.anchorView = binding.bottomNavigation
        snackbar.show()
    }
}

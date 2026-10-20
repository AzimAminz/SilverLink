package my.utem.ftmk.silverlink

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

enum class Role { REQUESTER, HELPER }
enum class RequestStage { OPEN, ACCEPTED, SUBMITTED, VERIFIED }
enum class TaskCategory(@param:StringRes val label: Int, val points: Int) {
    ERRANDS(R.string.request_category_errands, 40),
    COMPANIONSHIP(R.string.request_category_companionship, 30),
    HOUSEHOLD(R.string.request_category_household, 50),
    DIGITAL(R.string.request_category_digital, 30)
}

/** Local preview data. Replace actions with authenticated backend calls for production. */
class DemoViewModel(private val saved: SavedStateHandle) : ViewModel() {
    var role: Role
        get() = Role.valueOf(saved["role"] ?: Role.REQUESTER.name)
        set(value) { saved["role"] = value.name }
    var stage: RequestStage
        get() = RequestStage.valueOf(saved["stage"] ?: RequestStage.OPEN.name)
        private set(value) { saved["stage"] = value.name }
    var draftCategory: TaskCategory
        get() = TaskCategory.valueOf(saved["draftCategory"] ?: TaskCategory.ERRANDS.name)
        set(value) { saved["draftCategory"] = value.name }
    var draftTitle: String
        get() = saved["draftTitle"] ?: "Collect prepaid groceries"
        set(value) { saved["draftTitle"] = value }
    var draftNotes: String
        get() = saved["draftNotes"] ?: "Collect prepaid groceries at Damai Fresh, order #DF218. Bring bags to Mum’s kitchen counter."
        set(value) { saved["draftNotes"] = value }
    var draftForParent: Boolean
        get() = saved["draftForParent"] ?: true
        set(value) { saved["draftForParent"] = value }
    var draftWhen: String
        get() = saved["draftWhen"] ?: "Today, 14 Oct · 4:00–5:00 PM"
        set(value) { saved["draftWhen"] = value }
    var draftLocation: String
        get() = saved["draftLocation"] ?: "Mum’s home · 18 Cedar Lane"
        set(value) { saved["draftLocation"] = value }
    val category: TaskCategory get() = TaskCategory.valueOf(saved["category"] ?: TaskCategory.ERRANDS.name)
    val title: String get() = saved["title"] ?: "Collect prepaid groceries"
    val notes: String get() = saved["notes"] ?: "Collect prepaid groceries at Damai Fresh, order #DF218. Bring bags to Mum’s kitchen counter."
    val forParent: Boolean get() = saved["forParent"] ?: true
    val whenText: String get() = saved["when"] ?: "Today, 4:00–5:00 PM"
    val location: String get() = saved["location"] ?: "18 Cedar Lane, Damai Township"
    val points: Int get() = saved["points"] ?: 280
    val assists: Int get() = saved["assists"] ?: 7
    var completionNotes: String
        get() = saved["completionNotes"] ?: "Collected order #DF218 and put the bags on Asha’s kitchen counter. She confirmed everything arrived."
        set(value) { saved["completionNotes"] = value }
    var completionConfirmed: Boolean
        get() = saved["completionConfirmed"] ?: false
        set(value) { saved["completionConfirmed"] = value }
    var photoUri: String?
        get() = saved["photoUri"]
        set(value) { saved["photoUri"] = value }
    var nearbyFilter: Int
        get() = saved["filter"] ?: R.id.filter_all
        set(value) { saved["filter"] = value }

    fun publishRequest() {
        saved["category"] = draftCategory.name
        saved["title"] = draftTitle.trim()
        saved["notes"] = draftNotes.trim()
        saved["forParent"] = draftForParent
        saved["when"] = draftWhen
        saved["location"] = draftLocation
        saved["completionConfirmed"] = false
        saved["photoUri"] = null
        stage = RequestStage.OPEN
    }

    fun acceptRequest(): Boolean {
        if (stage != RequestStage.OPEN) return false
        stage = RequestStage.ACCEPTED
        return true
    }

    fun submitCompletion(): Boolean {
        if (stage != RequestStage.ACCEPTED || !completionConfirmed || completionNotes.isBlank()) return false
        stage = RequestStage.SUBMITTED
        return true
    }

    fun verifyCompletion(): Boolean {
        if (stage != RequestStage.SUBMITTED) return false
        saved["points"] = points + category.points
        saved["assists"] = assists + 1
        stage = RequestStage.VERIFIED
        return true
    }
}

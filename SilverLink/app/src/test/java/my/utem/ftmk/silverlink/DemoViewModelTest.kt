package my.utem.ftmk.silverlink

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test

class DemoViewModelTest {
    @Test fun pointsRequireAcceptedSubmittedAndVerifiedHelpAndCannotBeAwardedTwice() {
        val state = SavedStateHandle()
        val model = DemoViewModel(state)
        assertFalse(model.submitCompletion())
        assertFalse(model.verifyCompletion())
        assertEquals(280, model.points)
        assertTrue(model.acceptRequest())
        assertFalse(model.acceptRequest())
        assertFalse(model.submitCompletion())
        model.completionConfirmed = true
        model.completionNotes = " "
        assertFalse(model.submitCompletion())
        model.completionNotes = "Delivered groceries and confirmed with Asha."
        assertTrue(model.submitCompletion())
        assertFalse(model.submitCompletion())
        assertEquals(280, model.points)
        val restored = DemoViewModel(state)
        assertEquals(RequestStage.SUBMITTED, restored.stage)
        assertTrue(restored.verifyCompletion())
        assertFalse(restored.verifyCompletion())
        assertEquals(320, restored.points)
        assertEquals(8, restored.assists)
    }

    @Test fun publishedRequestUsesItsOwnCategoryAndDoesNotFollowDraftEdits() {
        val model = DemoViewModel(SavedStateHandle())
        model.draftCategory = TaskCategory.COMPANIONSHIP
        model.draftForParent = false
        model.draftTitle = "  Tea with Mum  "
        model.draftNotes = "Spend half an hour with Mum."
        model.publishRequest()
        model.draftCategory = TaskCategory.HOUSEHOLD
        assertEquals("Tea with Mum", model.title)
        assertEquals(TaskCategory.COMPANIONSHIP, model.category)
        assertFalse(model.forParent)
        model.acceptRequest()
        model.completionConfirmed = true
        model.submitCompletion()
        model.verifyCompletion()
        assertEquals(310, model.points)
    }
}

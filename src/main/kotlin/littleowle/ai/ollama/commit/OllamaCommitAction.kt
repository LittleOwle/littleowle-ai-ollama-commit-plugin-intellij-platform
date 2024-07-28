package littleowle.ai.ollama.commit

import littleowle.ai.ollama.commit.OllamaCommitUtils.commonBranch
import littleowle.ai.ollama.commit.OllamaCommitUtils.computeDiff
import littleowle.ai.ollama.commit.OllamaCommitUtils.constructPrompt
import littleowle.ai.ollama.commit.notifications.Notification
import littleowle.ai.ollama.commit.notifications.sendNotification
import littleowle.ai.ollama.commit.settings.AppSettings2

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler

class OllamaCommitAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val llmClient = AppSettings2.instance.getActiveLLMClient()
        if (llmClient == null) {
            Notification.clientNotSet()
            return
        }
        val project = e.project ?: return

        val commitWorkflowHandler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>?
        if (commitWorkflowHandler == null) {
            sendNotification(Notification.noCommitMessage())
            return
        }

        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges()
        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext) as CommitMessage?

        val diff = computeDiff(includedChanges, false, project)
        if (diff.isBlank()) {
            sendNotification(Notification.emptyDiff())
            return
        }

        val branch = commonBranch(includedChanges, project)
        val hint = commitMessage?.text
        val prompt = constructPrompt(AppSettings2.instance.activePrompt.content, diff, branch, hint)

        if (commitMessage == null) {
            sendNotification(Notification.noCommitMessage())
            return
        }

        llmClient.generateCommitMessage(prompt, project, commitMessage)
    }
}
package littleowle.ai.ollama.commit.listeners

import littleowle.ai.ollama.commit.OllamaCommitBundle
import littleowle.ai.ollama.commit.notifications.Notification
import littleowle.ai.ollama.commit.notifications.sendNotification
import littleowle.ai.ollama.commit.settings.AppSettings2
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

internal class OllamaCommitApplicationStartupListener  : ProjectActivity {

    private var firstTime = true
    override suspend fun execute(project: Project) {
        showVersionNotification(project)
    }
    private fun showVersionNotification(project: Project) {
        val settings = AppSettings2.instance
        val version = OllamaCommitBundle.plugin()?.version

        if (version == settings.lastVersion) {
            return
        }

        settings.lastVersion = version
        if (firstTime && version != null) {
            sendNotification(Notification.welcome(version), project)
        }
        firstTime = false
    }
}

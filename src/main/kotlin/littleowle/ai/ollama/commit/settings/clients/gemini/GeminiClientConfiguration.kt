package littleowle.ai.ollama.commit.settings.clients.gemini

import littleowle.ai.ollama.commit.Icons
import littleowle.ai.ollama.commit.settings.clients.LLMClientConfiguration
import littleowle.ai.ollama.commit.settings.clients.LLMClientSharedState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import javax.swing.Icon

class GeminiClientConfiguration : LLMClientConfiguration(
    "Gemini",
    "gemini-pro",
    "0.7"
) {
    @Attribute
    var projectId: String = "project-id"
    @Attribute
    var location: String = "us-central1"

    companion object {
        const val CLIENT_NAME = "Gemini"
    }

    override fun getClientName(): String {
        return CLIENT_NAME
    }

    override fun getClientIcon(): Icon {
        return Icons.GEMINI_LOGO
    }

    override fun getSharedState(): LLMClientSharedState {
        return GeminiClientSharedState.getInstance()
    }

    override fun generateCommitMessage(prompt: String, project: Project, commitMessage: CommitMessage) {
        return GeminiClientService.getInstance().generateCommitMessage(this, prompt, project, commitMessage)
    }

    // Model names are hard-coded and do not need to be refreshed.
    override fun getRefreshModelsFunction() = null

    override fun clone(): LLMClientConfiguration {
        val copy = GeminiClientConfiguration()
        copy.id = id
        copy.name = name
        copy.modelId = modelId
        copy.temperature = temperature
        copy.projectId = projectId
        copy.location = location
        return copy
    }

    override fun panel() = GeminiClientPanel(this)


}

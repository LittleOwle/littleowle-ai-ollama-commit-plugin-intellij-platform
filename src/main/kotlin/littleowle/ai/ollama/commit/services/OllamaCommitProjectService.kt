package littleowle.ai.ollama.commit.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import littleowle.ai.ollama.commit.OllamaCommitBundle

@Service(Service.Level.PROJECT)
class OllamaCommitProjectService(project: Project) {

    init {
        thisLogger().info(OllamaCommitBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    fun getRandomNumber() = (1..100).random()
}

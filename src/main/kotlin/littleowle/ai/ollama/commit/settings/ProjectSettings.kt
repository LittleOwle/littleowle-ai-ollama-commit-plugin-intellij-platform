package littleowle.ai.ollama.commit.settings

import littleowle.ai.ollama.commit.OllamaCommitUtils
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
        name = ProjectSettings.SERVICE_NAME,
        storages = [Storage("OllamaCommit.xml")]
)
@Service(Service.Level.PROJECT)
class ProjectSettings : PersistentStateComponent<ProjectSettings?> {

    companion object {
        const val SERVICE_NAME = "littleowle.ai.ollama.commit.settings.ProjectSettings"
    }

    var projectExclusions: Set<String> = setOf()

    override fun getState() = this

    override fun loadState(state: ProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun isPathExcluded(path: String): Boolean {
        return OllamaCommitUtils.matchesGlobs(path, projectExclusions)
    }


}

package littleowle.ai.ollama.commit.settings.clients.gemini

import littleowle.ai.ollama.commit.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "GeminiClientSharedState", storages = [Storage("OllamaCommitGemini.xml")])
class GeminiClientSharedState : PersistentStateComponent<GeminiClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "gemini-pro",
        "gemini-ultra"
    )

    override fun getState(): GeminiClientSharedState = this

    override fun loadState(state: GeminiClientSharedState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}

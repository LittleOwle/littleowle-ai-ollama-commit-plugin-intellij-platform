package littleowle.ai.ollama.commit.settings.clients.gemini

import littleowle.ai.ollama.commit.settings.clients.LLMClientService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.APP)
class GeminiClientService(private val cs: CoroutineScope): LLMClientService<GeminiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiClientService = service()
    }

    override suspend fun buildChatModel(client: GeminiClientConfiguration): ChatLanguageModel {
        return VertexAiGeminiChatModel.builder()
            .project(client.projectId)
            .location(client.location)
            .modelName(client.modelId)
            .temperature(client.temperature.toFloat())
            .build()
    }

}

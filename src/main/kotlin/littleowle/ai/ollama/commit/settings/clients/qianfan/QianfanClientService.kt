package littleowle.ai.ollama.commit.settings.clients.qianfan

import littleowle.ai.ollama.commit.OllamaCommitUtils.getCredentialAttributes
import littleowle.ai.ollama.commit.OllamaCommitUtils.retrieveToken
import littleowle.ai.ollama.commit.notifications.Notification
import littleowle.ai.ollama.commit.notifications.sendNotification
import littleowle.ai.ollama.commit.settings.clients.LLMClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.qianfan.QianfanChatModel
import dev.langchain4j.model.qianfan.QianfanChatModelNameEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Service(Service.Level.APP)
class QianfanClientService(private val cs: CoroutineScope) : LLMClientService<QianfanClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): QianfanClientService = service()
    }

    override suspend fun buildChatModel(client: QianfanClientConfiguration): ChatLanguageModel {
        val apiKey = client.apiKey.nullize(true) ?: retrieveToken(client.id + "apiKey")?.toString(true)
        val secretKey = client.secretKey.nullize(true) ?: retrieveToken(client.id + "secretKey")?.toString(true)

        val builder = QianfanChatModel.builder()
            .baseUrl(client.host)
            .apiKey(apiKey)
            .secretKey(secretKey)
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
        // Fix https://github.com/langchain4j/langchain4j/pull/1426. Remove this 'if' statement when langchain4j releases a new version that resolves this issue.
        if (client.modelId == QianfanChatModelNameEnum.ERNIE_SPEED_128K.modelName) {
            builder.endpoint("ernie-speed-128k")
        }

        return builder.build()
    }

    fun saveApiKey(client: QianfanClientConfiguration, key: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(client.id + "apiKey"), key)
                client.apiKeyIsStored = true
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }

    fun saveSecretKey(client: QianfanClientConfiguration, key: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(client.id + "secretKey"), key)
                client.secretKeyIsStored = true
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }
}

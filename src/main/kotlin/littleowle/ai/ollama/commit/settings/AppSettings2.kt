package littleowle.ai.ollama.commit.settings

import littleowle.ai.ollama.commit.OllamaCommitUtils
import littleowle.ai.ollama.commit.OllamaCommitUtils.getCredentialAttributes
import littleowle.ai.ollama.commit.notifications.Notification
import littleowle.ai.ollama.commit.notifications.sendNotification
import littleowle.ai.ollama.commit.settings.clients.LLMClientConfiguration
import littleowle.ai.ollama.commit.settings.clients.gemini.GeminiClientConfiguration
import littleowle.ai.ollama.commit.settings.clients.ollama.OllamaClientConfiguration
import littleowle.ai.ollama.commit.settings.clients.openAi.OpenAiClientConfiguration
import littleowle.ai.ollama.commit.settings.clients.openAi.OpenAiClientSharedState
import littleowle.ai.ollama.commit.settings.clients.qianfan.QianfanClientConfiguration
import littleowle.ai.ollama.commit.settings.prompts.DefaultPrompts
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XCollection
import com.intellij.util.xmlb.annotations.XMap
import littleowle.ai.ollama.commit.settings.AppSettings
import java.util.*

@State(
    name = AppSettings2.SERVICE_NAME,
    storages = [
        Storage("OllamaCommit2.xml")
    ]
)
@Service(Service.Level.APP)
class AppSettings2 : PersistentStateComponent<AppSettings2> {

    companion object {
        const val SERVICE_NAME = "littleowle.ai.ollama.commit.settings.AppSettings2"
        val instance: AppSettings2
            get() = ApplicationManager.getApplication().getService(AppSettings2::class.java)
    }

    private var hits = 0
    var requestSupport = true
    var lastVersion: String? = null

    @OptionTag(converter = LocaleConverter::class)
    var locale: Locale = Locale.ENGLISH

    @XCollection(
        elementTypes = [
            OpenAiClientConfiguration::class,
            OllamaClientConfiguration::class,
            QianfanClientConfiguration::class,
            GeminiClientConfiguration::class
        ],
        style = XCollection.Style.v2
    )
    var llmClientConfigurations = setOf<LLMClientConfiguration>(
        OpenAiClientConfiguration()
    )

    @Attribute
    var activeLlmClientId: String? = null

    @XMap
    var prompts = DefaultPrompts.toPromptsMap()
    var activePrompt = prompts["basic"]!!

    var appExclusions: Set<String> = setOf()

    override fun getState() = this

    override fun loadState(state: AppSettings2) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun noStateLoaded() {
        val appSettings = AppSettings.instance
        migrateSettingsFromVersion1(appSettings)
        val openAiLlmClient = llmClientConfigurations.find { it.getClientName() == OpenAiClientConfiguration.CLIENT_NAME }
        migrateOpenAiClientFromVersion1(openAiLlmClient as OpenAiClientConfiguration, appSettings)
    }

    private fun migrateSettingsFromVersion1(appSettings: AppSettings) {
        hits = appSettings.hits
        locale = appSettings.locale
        lastVersion = appSettings.lastVersion
        requestSupport = appSettings.requestSupport
        prompts = appSettings.prompts
        activePrompt = appSettings.currentPrompt
        appExclusions = appSettings.appExclusions
    }

    private fun migrateOpenAiClientFromVersion1(openAiLlmClientConfiguration: OpenAiClientConfiguration?, appSettings: AppSettings) {
        openAiLlmClientConfiguration?.apply {
            host = appSettings.openAIHost
            appSettings.openAISocketTimeout.toIntOrNull()?.let { timeout = it }
            proxyUrl = appSettings.proxyUrl
            modelId = appSettings.openAIModelId
            temperature = appSettings.openAITemperature

            val credentialAttributes = getCredentialAttributes(appSettings.openAITokenTitle)
            migrateToken(credentialAttributes)
        }

        OpenAiClientSharedState.getInstance().hosts.addAll(appSettings.openAIHosts)
        OpenAiClientSharedState.getInstance().modelIds.addAll(appSettings.openAIModelIds)
    }

    private fun OpenAiClientConfiguration.migrateToken(credentialAttributes: CredentialAttributes) {
        PasswordSafe.instance.getAsync(credentialAttributes)
            .onSuccess {
                it?.password?.let { token ->
                    try {
                        PasswordSafe.instance.setPassword(getCredentialAttributes(id), token.toString(false))
                    } catch (e: Exception) {
                        sendNotification(Notification.unableToSaveToken(e.message))
                    }
                    tokenIsStored = true
                }
            }
    }

    fun recordHit() {
        hits++
        if (requestSupport && (hits == 50 || hits % 100 == 0)) {
            sendNotification(Notification.star())
        }
    }

    fun isPathExcluded(path: String): Boolean {
        return OllamaCommitUtils.matchesGlobs(path, appExclusions)
    }

    fun getActiveLLMClient(): LLMClientConfiguration? {
        return llmClientConfigurations.find { it.id == activeLlmClientId }
            ?: llmClientConfigurations.firstOrNull()
    }

    fun setActiveLlmClient(newId: String) {
        // TODO @Blarc: Throw exception if llm client id is not valid
        llmClientConfigurations.find { it.id == newId }?.let {
            activeLlmClientId = newId
        }
    }

    class LocaleConverter : Converter<Locale>() {
        override fun toString(value: Locale): String? {
            return value.toLanguageTag()
        }

        override fun fromString(value: String): Locale? {
            return Locale.forLanguageTag(value)
        }
    }
}

package littleowle.ai.ollama.commit.settings.clients.openAi

import littleowle.ai.ollama.commit.OllamaCommitBundle.message
import littleowle.ai.ollama.commit.emptyText
import littleowle.ai.ollama.commit.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class OpenAiClientPanel(private val clientConfiguration: OpenAiClientConfiguration) : LLMClientPanel(clientConfiguration) {
    private val proxyTextField = JBTextField()
    private val tokenPasswordField = JBPasswordField()

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        proxyRow()
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        organizationIdRow()
        temperatureRow()
        verifyRow()

    }

    private fun Panel.proxyRow() {
        row {
            label(message("settings.llmClient.proxy"))
                .widthGroup("label")
            cell(proxyTextField)
                .bindText(clientConfiguration::proxyUrl.toNonNullableProperty(""))
                .resizableColumn()
                .align(Align.FILL)
                .comment(message("settings.llmClient.proxy.comment"))
        }
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    OpenAiClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.openAI.token.stored") else message("settings.openAI.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.openAi.token.comment"), 50)
        }
    }

    private fun Panel.organizationIdRow() {
        row {
            label(message("settings.openAi.organizationId"))
                .widthGroup("label")
            textField()
                .bindText(clientConfiguration::organizationId.toNonNullableProperty(""))
                .align(Align.FILL)
                .resizableColumn()
        }
    }

    override fun verifyConfiguration() {

        clientConfiguration.host = hostComboBox.item
        clientConfiguration.proxyUrl = proxyTextField.text
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)

        OpenAiClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }
}

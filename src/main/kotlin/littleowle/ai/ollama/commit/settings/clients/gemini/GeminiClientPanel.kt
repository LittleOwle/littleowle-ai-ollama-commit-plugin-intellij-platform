package littleowle.ai.ollama.commit.settings.clients.gemini

import littleowle.ai.ollama.commit.OllamaCommitBundle.message
import littleowle.ai.ollama.commit.notBlank
import littleowle.ai.ollama.commit.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class GeminiClientPanel private constructor(
    private val clientConfiguration: GeminiClientConfiguration,
    val service: GeminiClientService
) : LLMClientPanel(clientConfiguration) {

    private val projectIdTextField = JBTextField()
    private val locationTextField = JBTextField()

    constructor(configuration: GeminiClientConfiguration): this(configuration, GeminiClientService.getInstance())

    override fun create() = panel {
        nameRow()
        projectIdRow()
        locationRow()
        modelIdRow()
        temperatureRow()
        verifyRow()
    }

    private fun Panel.projectIdRow() {
        row {
            label(message("settings.gemini.project-id"))
                .widthGroup("label")

            cell(projectIdTextField)
                .bindText(clientConfiguration::projectId)
                .resizableColumn()
                .align(Align.FILL)
                .validationOnInput { notBlank(it.text) }
                .comment(message("settings.gemini.project-id.comment"))
        }

    }

    private fun Panel.locationRow() {
        row {
            label(message("settings.gemini.location"))
                .widthGroup("label")
            cell(locationTextField)
                .bindText(clientConfiguration::location)
                .resizableColumn()
                .validationOnInput { notBlank(it.text) }
                .align(Align.FILL)
        }
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text

        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}

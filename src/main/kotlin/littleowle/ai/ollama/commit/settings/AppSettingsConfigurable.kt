package littleowle.ai.ollama.commit.settings

import littleowle.ai.ollama.commit.OllamaCommitBundle
import littleowle.ai.ollama.commit.OllamaCommitBundle.message
import littleowle.ai.ollama.commit.settings.clients.LLMClientConfiguration
import littleowle.ai.ollama.commit.settings.clients.LLMClientTable
import littleowle.ai.ollama.commit.settings.prompts.Prompt
import littleowle.ai.ollama.commit.settings.prompts.PromptTable
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.*
import java.util.*

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {

    private val llmClientTable = LLMClientTable()
    private lateinit var llmClientToolbarDecorator: ToolbarDecorator
    private lateinit var llmClientConfigurationComboBox: ComboBox<LLMClientConfiguration>
    private val promptTable = PromptTable()
    private lateinit var toolbarDecorator: ToolbarDecorator
    private lateinit var promptComboBox: ComboBox<Prompt>

    override fun createPanel() = panel {

        row {
            label(message("settings.llmClient")).widthGroup("labelPrompt")
            llmClientConfigurationComboBox = comboBox(AppSettings2.instance.llmClientConfigurations, OllamaCommitListCellRenderer())
                .bindItem(getter = AppSettings2.instance::getActiveLLMClient) {
                    it?.let {
                        AppSettings2.instance.setActiveLlmClient(it.id)
                    }
                }.widthGroup("input")
                .component
        }
        row {
            llmClientToolbarDecorator = ToolbarDecorator.createDecorator(llmClientTable.table)
                .setAddAction {
                    llmClientTable.addLlmClient().let {
                        llmClientConfigurationComboBox.addItem(it)
                    }
                }
                .setEditAction {
                    llmClientTable.editLlmClient()?.let {
                        val editingActive = llmClientConfigurationComboBox.selectedItem == it.first
                        llmClientConfigurationComboBox.removeItem(it.first)
                        llmClientConfigurationComboBox.addItem(it.second)

                        if (editingActive) {
                            llmClientConfigurationComboBox.selectedItem = it.second
                        }
                    }
                }
                .setRemoveAction {
                    llmClientTable.removeLlmClient()?.let {
                        llmClientConfigurationComboBox.removeItem(it)
                    }
                }
                .disableUpDownActions()

            cell(llmClientToolbarDecorator.createPanel())
                .align(Align.FILL)
        }.resizableRow()

        row {
            label(message("settings.locale")).widthGroup("labelPrompt")
            comboBox(Locale.getAvailableLocales()
                .distinctBy { it.displayLanguage }
                .sortedBy { it.displayLanguage },
                OllamaCommitListCellRenderer()
            )
                .widthGroup("input")
                .bindItem(AppSettings2.instance::locale.toNullableProperty())

            browserLink(message("settings.more-prompts"), OllamaCommitBundle.URL_PROMPTS_DISCUSSION.toString())
                .align(AlignX.RIGHT)
        }
        row {
            label(message("settings.prompt")).widthGroup("labelPrompt")
            promptComboBox = comboBox(AppSettings2.instance.prompts.values, OllamaCommitListCellRenderer())
                .bindItem(AppSettings2.instance::activePrompt.toNullableProperty())
                .widthGroup("input")
                .component
        }
        row {
            toolbarDecorator = ToolbarDecorator.createDecorator(promptTable.table)
                .setAddAction {
                    promptTable.addPrompt().let {
                        promptComboBox.addItem(it)
                    }
                }
                .setEditAction {
                    promptTable.editPrompt()?.let {
                        val editingSelected = promptComboBox.selectedItem == it.first
                        promptComboBox.removeItem(it.first)
                        promptComboBox.addItem(it.second)

                        if (editingSelected) {
                            promptComboBox.selectedItem = it.second
                        }
                    }
                }
                .setEditActionUpdater {
                    updateActionAvailability(CommonActionsPanel.Buttons.EDIT)
                    true
                }
                .setRemoveAction {
                    promptTable.removePrompt()?.let {
                        promptComboBox.removeItem(it)
                    }
                }
                .setRemoveActionUpdater {
                    updateActionAvailability(CommonActionsPanel.Buttons.REMOVE)
                    true
                }
                .disableUpDownActions()

            cell(toolbarDecorator.createPanel())
                .align(Align.FILL)
        }.resizableRow()

        row {
            browserLink(message("settings.report-bug"), OllamaCommitBundle.URL_BUG_REPORT.toString())
        }
    }

    private fun updateActionAvailability(action: CommonActionsPanel.Buttons) {
        val selectedRow = promptTable.table.selectedRow
        val selectedPrompt = promptTable.table.items[selectedRow]
        toolbarDecorator.actionsPanel.setEnabled(action, selectedPrompt.canBeChanged)
    }

    override fun isModified(): Boolean {
        return super.isModified() || promptTable.isModified() || llmClientTable.isModified()
    }

    override fun apply() {
        promptTable.apply()
        llmClientTable.apply()
        super.apply()
    }

    override fun reset() {
        promptTable.reset()
        llmClientTable.reset()
        super.reset()
    }

}

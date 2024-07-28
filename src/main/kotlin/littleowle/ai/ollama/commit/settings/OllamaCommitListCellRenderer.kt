package littleowle.ai.ollama.commit.settings

import littleowle.ai.ollama.commit.settings.clients.LLMClientConfiguration
import littleowle.ai.ollama.commit.settings.prompts.Prompt
import java.awt.Component
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class OllamaCommitListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        when (value) {
            is Locale -> {
                text = value.displayLanguage
            }

            is Prompt -> {
                text = value.name
            }

            // This is used for combo box in settings dialog
            is LLMClientConfiguration -> {
                text = value.name
                icon = value.getClientIcon()
            }
        }
        return component
    }
}

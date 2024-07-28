package littleowle.ai.ollama.commit.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import littleowle.ai.ollama.commit.OllamaCommitBundle
import littleowle.ai.ollama.commit.services.OllamaCommitProjectService
import javax.swing.JButton


class OllamaCommitToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = OllamaCommitToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class OllamaCommitToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<OllamaCommitProjectService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val label = JBLabel(OllamaCommitBundle.message("randomLabel", "?"))

            add(label)
            add(JButton(OllamaCommitBundle.message("shuffle")).apply {
                addActionListener {
                    label.text = OllamaCommitBundle.message("randomLabel", service.getRandomNumber())
                }
            })
        }
    }
}

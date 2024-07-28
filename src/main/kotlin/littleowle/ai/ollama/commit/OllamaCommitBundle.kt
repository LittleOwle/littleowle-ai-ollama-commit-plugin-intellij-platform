package littleowle.ai.ollama.commit

import com.intellij.DynamicBundle
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.net.URI

@NonNls
private const val BUNDLE = "messages.OllamaCommitBundle"

object OllamaCommitBundle : DynamicBundle(BUNDLE) {

    val URL_BUG_REPORT = URI("https://github.com/LittleOwle/littleowle-ai-ollama-commit-plugin-intellij-platform/issues")
    val URL_PROMPTS_DISCUSSION = URI("https://github.com/LittleOwle/littleowle-ai-ollama-commit-plugin-intellij-platform/discussions/18")

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)

    fun openPluginSettings(project: Project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, message("settings.general.group.title"))
    }

    fun openRepository() {
        BrowserLauncher.instance.open("https://github.com/LittleOwle/littleowle-ai-ollama-commit-plugin-intellij-platform");
    }

    fun plugin() = PluginManagerCore.getPlugin(PluginId.getId("com.github.blarc.ai-commits-intellij-plugin"))


}
package littleowle.ai.ollama.commit

import littleowle.ai.ollama.commit.notifications.Notification
import littleowle.ai.ollama.commit.notifications.sendNotification
import littleowle.ai.ollama.commit.settings.AppSettings2
import littleowle.ai.ollama.commit.settings.ProjectSettings
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.OneTimeString
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.StringWriter
import java.nio.file.FileSystems

object OllamaCommitUtils {

    fun isPathExcluded(path: String, project: Project): Boolean {
        return !AppSettings2.instance.isPathExcluded(path) && !project.service<ProjectSettings>().isPathExcluded(path)
    }

    fun matchesGlobs(text: String, globs: Set<String>): Boolean {
        val fileSystem = FileSystems.getDefault()
        for (globString in globs) {
            val glob = fileSystem.getPathMatcher("glob:$globString")
            if (glob.matches(fileSystem.getPath(text))) {
                return true
            }
        }
        return false
    }

    fun constructPrompt(promptContent: String, diff: String, branch: String, hint: String?): String {
        var content = promptContent
        content = content.replace("{locale}", AppSettings2.instance.locale.displayLanguage)
        content = content.replace("{branch}", branch)
        content = replaceHint(content, hint)

        return if (content.contains("{diff}")) {
            content.replace("{diff}", diff)
        } else {
            "$content\n$diff"
        }
    }

    fun replaceHint(promptContent: String, hint: String?): String {
        val hintRegex = Regex("\\{[^{}]*(\\\$hint)[^{}]*}")

        hintRegex.find(promptContent, 0)?.let {
            if (!hint.isNullOrBlank()) {
                var hintValue = it.value.replace("\$hint", hint)
                hintValue = hintValue.replace("{", "")
                hintValue = hintValue.replace("}", "")
                return promptContent.replace(it.value, hintValue)
            } else {
                return promptContent.replace(it.value, "")
            }
        }
        return promptContent.replace("{hint}", hint.orEmpty())
    }

    fun commonBranch(changes: List<Change>, project: Project): String {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        var branch = changes.map {
            repositoryManager.getRepositoryForFileQuick(it.virtualFile)?.currentBranchName
        }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

        if (branch == null) {
            sendNotification(Notification.noCommonBranch())
            // hardcoded fallback branch
            branch = "main"
        }
        return branch
    }

    fun computeDiff(
        includedChanges: List<Change>,
        reversePatch: Boolean,
        project: Project
    ): String {

        val gitRepositoryManager = GitRepositoryManager.getInstance(project)

        // go through included changes, create a map of repository to changes and discard nulls
        val changesByRepository = includedChanges
            .filter {
                it.virtualFile?.path?.let { path ->
                    isPathExcluded(path, project)
                } ?: false
            }
            .mapNotNull { change ->
                change.virtualFile?.let { file ->
                    gitRepositoryManager.getRepositoryForFileQuick(
                        file
                    ) to change
                }
            }
            .groupBy({ it.first }, { it.second })


        // compute diff for each repository
        return changesByRepository
            .map { (repository, changes) ->
                repository?.let {
                    val filePatches = IdeaTextPatchBuilder.buildPatch(
                        project,
                        changes,
                        repository.root.toNioPath(), reversePatch, true
                    )

                    val stringWriter = StringWriter()
                    stringWriter.write("Repository: ${repository.root.path}\n")
                    UnifiedDiffWriter.write(project, filePatches, stringWriter, "\n", null)
                    stringWriter.toString()
                }
            }
            .joinToString("\n")
    }

    suspend fun retrieveToken(title: String): OneTimeString? {
        val credentialAttributes = getCredentialAttributes(title)
        val credentials = withContext(Dispatchers.IO) {
            PasswordSafe.instance.get(credentialAttributes)
        }
        return credentials?.password
    }

    fun getCredentialAttributes(title: String): CredentialAttributes {
        return CredentialAttributes(
            title,
            null,
            this.javaClass,
            false
        )
    }
}

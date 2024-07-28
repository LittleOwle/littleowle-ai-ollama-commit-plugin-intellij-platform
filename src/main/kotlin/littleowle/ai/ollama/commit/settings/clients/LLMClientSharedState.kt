package littleowle.ai.ollama.commit.settings.clients

interface LLMClientSharedState {

    val hosts: MutableSet<String>

    val modelIds: MutableSet<String>
}

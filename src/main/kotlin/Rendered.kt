package games.soloscribe.markdown

data class Rendered(
    val html: String,
    val metadata: Map<String, List<String>>?,
) {
    operator fun get(key: String) = metadata?.get(key)
}
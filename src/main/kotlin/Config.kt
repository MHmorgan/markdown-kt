package games.soloscribe.markdown

/**
 * Configuration of Markdown extensions.
 */
data class Config(
    var enableAutolink: Boolean = false,
    var enableStrikethrough: Boolean = false,
    var enableTables: Boolean = false,
    var enableFootnotes: Boolean = false,
    var enableHeadingAnchors: Boolean = false,
    var enableIns: Boolean = false,
    var enableYamlFrontMatter: Boolean = false,
    var enableImageAttributes: Boolean = false,
    var enableTaskList: Boolean = false,
) {
    fun enableAllExtensions() {
        enableAutolink = true
        enableStrikethrough = true
        enableTables = true
        enableFootnotes = true
        enableHeadingAnchors = true
        enableIns = true
        enableYamlFrontMatter = true
        enableImageAttributes = true
        enableTaskList = true
    }
}
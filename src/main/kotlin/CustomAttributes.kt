package games.soloscribe.markdown

import org.commonmark.node.Node

@MarkdownDsl
class CustomAttributes(
    val node: Node,
    val tag: String,
    val attrs: MutableMap<String, String>,
)
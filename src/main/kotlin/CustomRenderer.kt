package games.soloscribe.markdown

import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlWriter

@MarkdownDsl
class CustomRenderer<T: Node>(val node: T, val ctx: HtmlNodeRendererContext) {
    val html: HtmlWriter = ctx.writer

    fun renderChildren(node: Node = this.node) {
        var child = node.firstChild
        while (child != null) {
            ctx.render(child)
            child = child.next
        }
    }
}
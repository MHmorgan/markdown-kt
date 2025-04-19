package games.soloscribe.markdown

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Heading
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text
import org.junit.jupiter.api.Test

class MarkdownTest {
    @Test
    fun hello() {
        val md = """
            # Hello, Markdown!
            
            This is a simple markdown document.
            Let's see how it renders.
        """.trimIndent()

        var count = 0
        val res = renderMarkdown(md) {
            render(SoftLineBreak::class) {
                html.tag("br")
                html.line()
            }

            attributes<Heading> {
                attrs.put("class", "my-text")
            }

            visitor(object : AbstractVisitor() {
                override fun visit(node: Text) {
                    count++
                }
            })

            paragraph {
                html.line()
                html.tag("div")
                renderChildren()
                html.tag("/div")
                html.line()
            }
        }

        println("Count: $count")
        println(res)
    }
}

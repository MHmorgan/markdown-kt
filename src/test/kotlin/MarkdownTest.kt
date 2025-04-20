package games.soloscribe.markdown

import org.assertj.core.api.Assertions.assertThat
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Heading
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.junit.jupiter.api.Test

class MarkdownTest {
    @Test
    fun `Soft line breaks`() {
        val md = """
            # Hello, Markdown!
            
            This is a simple markdown document.
            Let's see how it renders.
        """.trimIndent()

        val res = renderMarkdown(md) {
            render(SoftLineBreak::class) {
                html.tag("br/")
                html.line()
            }

            paragraph {
                html.tag("div")
                renderChildren()
                html.tag("/div")
            }
        }

        val expect = """
            <h1>Hello, Markdown!</h1>
            <div>This is a simple markdown document.<br/>
            Let's see how it renders.</div>
        """.trimIndent()
        assertThat(res.html.trim()).isEqualTo(expect)
    }

    @Test
    fun visitor() {
        val md = """
            # Hello, Markdown!
            
            ## Heading 2
            
            Paragraph 1
            
            ### Heading 3
            
            Paragraph 2
        """.trimIndent()

        var headings = 0
        var paragraphs = 0
        val res = renderMarkdown(md) {
            visitor(object : AbstractVisitor() {
                override fun visit(node: Heading) {
                    headings++
                }

                override fun visit(node: Paragraph) {
                    paragraphs++
                }
            })
        }

        assertThat(headings).isEqualTo(3)
        assertThat(paragraphs).isEqualTo(2)
    }

    @Test
    fun attributes() {
        val md = "# Hello"
        val res = renderMarkdown(md) {
            attributes<Heading> {
                attrs.put("class", "my-text")
            }
        }

        assertThat(res.html.trim())
            .isEqualTo("""<h1 class="my-text">Hello</h1>""")
    }
}

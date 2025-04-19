package games.soloscribe.markdown

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.footnotes.FootnoteDefinition
import org.commonmark.ext.footnotes.FootnoteReference
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.*
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.ext.image.attributes.ImageAttributes
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.Ins
import org.commonmark.ext.ins.InsExtension
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.AttributeProvider
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.reflect.KClass

/**
 * Render the Markdown string [md], using the given [block] to configure
 * the [commonmark](https://github.com/commonmark/commonmark-java) rendering.
 *
 * @see Markdown Implementation of the Markdown DSL.
 */
fun renderMarkdown(md: String, block: Markdown.() -> Unit): Rendered {
    return Markdown()
        .apply(block)
        .render(md)
}

@DslMarker
internal annotation class MarkdownDsl

/**
 * A DSL for customizing the rendering of Markdown with CommonMark.
 *
 * Uses the [commonmark-java](https://github.com/commonmark/commonmark-java)
 * library to parse and render Markdown.
 *
 * See the CommonMark [specification](https://spec.commonmark.org/current/)
 * for details on the Markdown syntax, and the different node types.
 */
@MarkdownDsl
class Markdown {
    private val config = Config()
    private val parser = Parser.builder()!!
    private val renderer = HtmlRenderer.builder()!!
    private val visitors = mutableListOf<Visitor>()

    internal fun render(md: String): Rendered {
        val exts = buildList {
            if (config.enableAutolink)
                add(AutolinkExtension.create())
            if (config.enableStrikethrough)
                add(StrikethroughExtension.create())
            if (config.enableTables)
                add(TablesExtension.create())
            if (config.enableFootnotes)
                add(FootnotesExtension.create())
            if (config.enableYamlFrontMatter)
                add(YamlFrontMatterExtension.create())
            if (config.enableIns)
                add(InsExtension.create())
            if (config.enableImageAttributes)
                add(ImageAttributesExtension.create())
            if (config.enableTaskList)
                add(TaskListItemsExtension.create())
            if (config.enableHeadingAnchors)
                add(HeadingAnchorExtension.create())
        }
        val par = parser
            .extensions(exts)
            .build()
        val doc = par.parse(md)

        for (vis in visitors) doc.accept(vis)

        val meta = if (config.enableYamlFrontMatter) {
            val vis = YamlFrontMatterVisitor()
            doc.accept(vis)
            vis.data.mapValues { (_, list) ->
                list.map { it.toString() }
            }
        } else null

        val rend = renderer
            .extensions(exts)
            .build()
        val html = rend.render(doc)

        return Rendered(html, meta)
    }

    /**
     * Configure the Markdown parser and renderer.
     */
    fun config(block: Config.() -> Unit) {
        config.block()
    }

    /**
     * Customize the Markdown parser.
     */
    fun parser(block: Parser.Builder.() -> Unit) {
        parser.block()
    }

    /**
     * Customize the Markdown renderer.
     */
    fun renderer(block: HtmlRenderer.Builder.() -> Unit) {
        renderer.block()
    }

    /**
     * Add a custom visitor.
     */
    fun visitor(visitor: Visitor) {
        visitors.add(visitor)
    }

    /**
     * Add a custom attribute provider for the given node type(s).
     */
    fun <T: Node> attributes(
        vararg clazz: KClass<T>,
        block: CustomAttributes.() -> Unit
    ) {
        renderer.attributeProviderFactory { ctx ->
            AttributeProvider { node, tag, attributes ->
                if (node::class in clazz) {
                    CustomAttributes(node, tag, attributes).block()
                }
            }
        }
    }

    /**
     * Add a custom attribute provider for the given node type.
     */
    inline fun <reified T: Node> attributes(
        crossinline block: CustomAttributes.() -> Unit
    ) = attributes(T::class) { block() }

    /**
     * Add a custom renderer for the given node type(s).
     */
    fun <T : Node> render(
        vararg clazz: KClass<T>,
        block: CustomRenderer<Node>.() -> Unit
    ) {
        val types = clazz.map { it.java }.toSet()
        renderer.nodeRendererFactory { ctx ->
            object : NodeRenderer {
                override fun getNodeTypes() = types

                override fun render(node: Node) {
                    CustomRenderer(node, ctx).block()
                }
            }
        }
    }

    private inline fun <reified T : Node> render(
        crossinline block: CustomRenderer<T>.() -> Unit
    ) {
        renderer.nodeRendererFactory { ctx ->
            object : NodeRenderer {
                override fun getNodeTypes() = setOf(T::class.java)

                override fun render(node: Node) {
                    CustomRenderer(node as T, ctx).block()
                }
            }
        }
    }

    /**
     * Add a custom renderer for `Block` nodes.
     */
    fun block(block: CustomRenderer<Block>.() -> Unit) =
        render<Block>(block)

    /**
     * Add a custom renderer for `Code` nodes.
     */
    fun code(block: CustomRenderer<Code>.() -> Unit) =
        render<Code>(block)

    /**
     * Add a custom renderer for `Emphasis` node.
     */
    fun emphasis(block: CustomRenderer<Emphasis>.() -> Unit) =
        render<Emphasis>(block)

    /**
     * Add a custom renderer for `HardLineBreak` node.
     */
    fun hardLineBreak(block: CustomRenderer<HardLineBreak>.() -> Unit) =
        render<HardLineBreak>(block)

    /**
     * Add a custom renderer for `HtmlBlock` node.
     */
    fun htmlInline(block: CustomRenderer<HtmlInline>.() -> Unit) =
        render<HtmlInline>(block)

    /**
     * Add a custom renderer for `HtmlInline` node.
     */
    fun image(block: CustomRenderer<Image>.() -> Unit) =
        render<Image>(block)

    /**
     * Add a custom renderer for `Link` node.
     */
    fun link(block: CustomRenderer<Link>.() -> Unit) =
        render<Link>(block)

    /**
     * Add a custom renderer for `SoftLineBreak` node.
     */
    fun softLineBreak(block: CustomRenderer<SoftLineBreak>.() -> Unit) =
        render<SoftLineBreak>(block)

    /**
     * Add a custom renderer for `StrongEmphasis` node.
     */
    fun strongEmphasis(block: CustomRenderer<StrongEmphasis>.() -> Unit) =
        render<StrongEmphasis>(block)

    /**
     * Add a custom renderer for `Text` node.
     */
    fun text(block: CustomRenderer<Text>.() -> Unit) =
        render<Text>(block)

    /**
     * Add a custom renderer for `BlockQuote` node.
     */
    fun blockQuote(block: CustomRenderer<BlockQuote>.() -> Unit) =
        render<BlockQuote>(block)

    /**
     * Add a custom renderer for `Document` node.
     */
    fun document(block: CustomRenderer<Document>.() -> Unit) =
        render<Document>(block)

    /**
     * Add a custom renderer for `FencedCodeBlock` node.
     */
    fun fencedCodeBlock(block: CustomRenderer<FencedCodeBlock>.() -> Unit) =
        render<FencedCodeBlock>(block)

    /**
     * Add a custom renderer for `Heading` node.
     */
    fun heading(block: CustomRenderer<Heading>.() -> Unit) =
        render<Heading>(block)

    /**
     * Add a custom renderer for `HtmlBlock` node.
     */
    fun htmlBlock(block: CustomRenderer<HtmlBlock>.() -> Unit) =
        render<HtmlBlock>(block)

    /**
     * Add a custom renderer for `HtmlInline` node.
     */
    fun indentedCodeBlock(block: CustomRenderer<IndentedCodeBlock>.() -> Unit) =
        render<IndentedCodeBlock>(block)

    /**
     * Add a custom renderer for `LinkReferenceDefinition` node.
     */
    fun linkReferenceDefinition(block: CustomRenderer<LinkReferenceDefinition>.() -> Unit) =
        render<LinkReferenceDefinition>(block)

    /**
     * Add a custom renderer for `ListBlock` node.
     */
    fun listBlock(block: CustomRenderer<ListBlock>.() -> Unit) =
        render<ListBlock>(block)

    /**
     * Add a custom renderer for `ListItem` node.
     */
    fun listItem(block: CustomRenderer<ListItem>.() -> Unit) =
        render<ListItem>(block)

    /**
     * Add a custom renderer for `Paragraph` node.
     */
    fun paragraph(block: CustomRenderer<Paragraph>.() -> Unit) =
        render<Paragraph>(block)

    /**
     * Add a custom renderer for `ThematicBreak` node.
     */
    fun thematicBreak(block: CustomRenderer<ThematicBreak>.() -> Unit) =
        render<ThematicBreak>(block)

    /**
     * Add a custom renderer for `BulletList` node.
     */
    fun bulletList(block: CustomRenderer<BulletList>.() -> Unit) =
        render<BulletList>(block)

    /**
     * Add a custom renderer for `OrderedList` node.
     */
    fun orderedList(block: CustomRenderer<OrderedList>.() -> Unit) =
        render<OrderedList>(block)

    /**
     * Add a custom renderer for `Strikethrough` node.
     *
     * Strikethrough is an extension of CommonMark.
     */
    fun strikethrough(block: CustomRenderer<Strikethrough>.() -> Unit) =
        render<Strikethrough>(block)

    /**
     * Add a custom renderer for `TableRow` node.
     *
     * Tables are an extension of CommonMark.
     */
    fun tableRow(block: CustomRenderer<TableRow>.() -> Unit) =
        render<TableRow>(block)

    /**
     * Add a custom renderer for `TableBody` node.
     *
     * Tables are an extension of CommonMark.
     */
    fun tableBody(block: CustomRenderer<TableBody>.() -> Unit) =
        render<TableBody>(block)

    /**
     * Add a custom renderer for `TableCell` node.
     *
     * Tables are an extension of CommonMark.
     */
    fun tableCell(block: CustomRenderer<TableCell>.() -> Unit) =
        render<TableCell>(block)

    /**
     * Add a custom renderer for `TableBlock` node.
     *
     * Tables are an extension of CommonMark.
     */
    fun tableBlock(block: CustomRenderer<TableBlock>.() -> Unit) =
        render<TableBlock>(block)

    /**
     * Add a custom renderer for `TableHead` node.
     *
     * Tables are an extension of CommonMark.
     */
    fun tableHead(block: CustomRenderer<TableHead>.() -> Unit) =
        render<TableHead>(block)

    /**
     * Add a custom renderer for `FootnoteReference` node.
     *
     * Footnotes are an extension of CommonMark.
     */
    fun footnoteReference(block: CustomRenderer<FootnoteReference>.() -> Unit) =
        render<FootnoteReference>(block)

    /**
     * Add a custom renderer for `FootnoteDefinition` node.
     *
     * Footnotes are an extension of CommonMark.
     */
    fun footnoteDefinition(block: CustomRenderer<FootnoteDefinition>.() -> Unit) =
        render<FootnoteDefinition>(block)

    /**
     * Add a custom renderer for `Ins` node.
     *
     * Ins is an extension of CommonMark.
     */
    fun ins(block: CustomRenderer<Ins>.() -> Unit) = render<Ins>(block)

    /**
     * Add a custom renderer for `YamlFrontMatterNode` node.
     *
     * YamlFrontMatter is an extension of CommonMark.
     */
    fun yamlFrontMatterNode(block: CustomRenderer<YamlFrontMatterNode>.() -> Unit) =
        render<YamlFrontMatterNode>(block)

    /**
     * Add a custom renderer for `YamlFrontMatterBlock` node.
     *
     * YamlFrontMatter is an extension of CommonMark.
     */
    fun yamlFrontMatterBlock(block: CustomRenderer<YamlFrontMatterBlock>.() -> Unit) =
        render<YamlFrontMatterBlock>(block)

    /**
     * Add a custom renderer for `ImageAttributes` node.
     *
     * ImageAttributes is an extension of CommonMark.
     */
    fun imageAttributes(block: CustomRenderer<ImageAttributes>.() -> Unit) =
        render<ImageAttributes>(block)

    /**
     * Add a custom renderer for `TaskListItemMarker` node.
     *
     * TaskList is an extension of CommonMark.
     */
    fun taskListItemMarker(block: CustomRenderer<TaskListItemMarker>.() -> Unit) =
        render<TaskListItemMarker>(block)
}

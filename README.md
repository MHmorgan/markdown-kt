📝 Markdown-KT
==============

[Documentation](https://mhmorgan.github.io/markdown-kt/)

A powerful Kotlin wrapper around [commonmark-java](https://github.com/commonmark/commonmark-java) that provides an idiomatic DSL for rendering markdown to HTML.

## ✨ Features

🚀 **Core Features:**
- 📄 Basic markdown rendering to HTML
- 🎨 Custom renderers for any markdown element
- 🏷️ Custom HTML attributes for elements
- 🌳 AST visitors for document processing
- 📊 YAML front matter extraction

🔧 **Extensions Support:**
- 🔗 Autolink URLs
- ~~Strikethrough~~ text
- 📋 GitHub Flavored Markdown tables
- 📝 Footnotes
- ⚓ Heading anchors
- <ins>Insert/underline</ins> text
- 🖼️ Image attributes
- ☑️ Task lists

## 🚀 Quick Start

```kotlin
import dev.hirth.markdown.renderMarkdown

fun main() {
    val markdown = """
        # Hello, World! 🌍
        
        This is **bold** and *italic* text.
        
        Visit [GitHub](https://github.com) for more info.
    """.trimIndent()

    val result = renderMarkdown(markdown) {}
    println(result.html)
}
```

## 📖 Usage Examples

### 🎯 Basic Rendering

```kotlin
import dev.hirth.markdown.renderMarkdown

val markdown = """
    # My Document
    
    This is a paragraph with **bold** and *italic* text.
    
    - Item 1
    - Item 2
    - Item 3
""".trimIndent()

val result = renderMarkdown(markdown)
println(result.html)
```

### 🔧 Enabling Extensions

```kotlin
val result = renderMarkdown(markdown) {
    config {
        // Enable all extensions at once
        enableAllExtensions()
        
        // Or enable specific extensions
        enableTables = true
        enableStrikethrough = true
        enableFootnotes = true
        enableTaskList = true
        enableYamlFrontMatter = true
    }
}
```

### 📋 Tables Example

```kotlin
val tableMarkdown = """
    | Name | Age | City |
    |------|-----|------|
    | John | 25  | NYC  |
    | Jane | 30  | LA   |
""".trimIndent()

val result = renderMarkdown(tableMarkdown) {
    config {
        enableTables = true
    }
}
```

### ☑️ Task Lists

```kotlin
val taskMarkdown = """
    ## Todo List
    
    - [x] Write documentation
    - [ ] Add more examples
    - [x] Test the code
""".trimIndent()

val result = renderMarkdown(taskMarkdown) {
    config {
        enableTaskList = true
    }
}
```

### 📝 Footnotes

```kotlin
val footnoteMarkdown = """
    This text has a footnote[^1].
    
    [^1]: This is the footnote content.
""".trimIndent()

val result = renderMarkdown(footnoteMarkdown) {
    config {
        enableFootnotes = true
    }
}
```

### 🎨 Custom Rendering

Customize how specific markdown elements are rendered:

```kotlin
val result = renderMarkdown(markdown) {
    // Custom soft line break rendering
    softLineBreak {
        html.tag("br/")
        html.line()
    }
    
    // Custom heading rendering
    heading {
        html.tag("h${node.level}")
        html.attribute("class", "custom-heading")
        renderChildren()
        html.tag("/h${node.level}")
    }
    
    // Custom paragraph rendering
    paragraph {
        html.tag("div")
        html.attribute("class", "paragraph")
        renderChildren()
        html.tag("/div")
    }
    
    // Custom emphasis rendering
    emphasis {
        html.tag("em")
        html.attribute("class", "italic")
        renderChildren()
        html.tag("/em")
    }
}
```

### 🏷️ Custom Attributes

Add custom HTML attributes to any element:

```kotlin
val result = renderMarkdown(markdown) {
    // Add CSS classes to headings
    attributes<Heading> {
        val heading = node as Heading
        attrs["class"] = "heading-${heading.level}"
        attrs["id"] = "heading-${System.currentTimeMillis()}"
    }
    
    // Add classes to paragraphs
    attributes<Paragraph> {
        attrs["class"] = "content-paragraph"
    }
    
    // Style links
    attributes<Link> {
        attrs["class"] = "external-link"
        attrs["target"] = "_blank"
    }
}
```

### 🌳 Document Visitors

Process the document AST with custom visitors:

```kotlin
import org.commonmark.node.*

var headingCount = 0
var paragraphCount = 0

val result = renderMarkdown(markdown) {
    visitor(object : AbstractVisitor() {
        override fun visit(heading: Heading) {
            headingCount++
            println("Found heading level ${heading.level}")
        }
        
        override fun visit(paragraph: Paragraph) {
            paragraphCount++
        }
        
        override fun visit(link: Link) {
            println("Found link: ${link.destination}")
        }
    })
}

println("Document has $headingCount headings and $paragraphCount paragraphs")
```

### 📊 YAML Front Matter

Extract metadata from YAML front matter:

```kotlin
val markdownWithFrontMatter = """
    ---
    title: My Blog Post
    author: John Doe
    tags: [kotlin, markdown, documentation]
    published: true
    ---
    
    # My Blog Post
    
    Content goes here...
""".trimIndent()

val result = renderMarkdown(markdownWithFrontMatter) {
    config {
        enableYamlFrontMatter = true
    }
}

// Access metadata
val title = result["title"]?.firstOrNull()
val author = result["author"]?.firstOrNull()
val tags = result["tags"] // List of tags

println("Title: $title")
println("Author: $author")
println("HTML: ${result.html}")
```

### 🎭 Advanced Custom Rendering

```kotlin
val result = renderMarkdown(markdown) {
    // Custom code block with syntax highlighting class
    fencedCodeBlock {
        val language = node.info ?: "text"
        html.tag("pre")
        html.tag("code")
        html.attribute("class", "language-$language")
        html.text(node.literal)
        html.tag("/code")
        html.tag("/pre")
    }
    
    // Custom blockquote with citation
    blockQuote {
        html.tag("blockquote")
        html.attribute("class", "quote")
        renderChildren()
        html.tag("cite")
        html.text("— Unknown")
        html.tag("/cite")
        html.tag("/blockquote")
    }
    
    // Custom image with figure wrapper
    image {
        html.tag("figure")
        html.tag("img")
        html.attribute("src", node.destination)
        html.attribute("alt", node.title ?: "")
        html.attribute("class", "responsive-image")
        if (!node.title.isNullOrEmpty()) {
            html.tag("figcaption")
            html.text(node.title)
            html.tag("/figcaption")
        }
        html.tag("/figure")
    }
}

## 📦 Installation

### 🔗 Submodule Dependency

Use [git submodule](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
to include this library in your project:

```shell
git submodule add git@github.com:MHmorgan/markdown-kt.git libs/markdown-kt
```

Add the following to your `settings.gradle.kts`:

```kotlin
includeBuild("libs/markdown-kt")
```

Then add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("games.soloscribe:markdown")
}
```

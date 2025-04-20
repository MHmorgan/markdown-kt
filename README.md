Markdown
========

[Documentation](https://mhmorgan.github.io/markdown-kt/)

Kotlin wrapper around [commonmark-java](https://github.com/commonmark/commonmark-java).

## Usage

Use git submodule to include this library in your project:

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

Example usage:

```kotlin
import games.soloscribe.markdown.renderMarkdown

fun main() {
    val md = """
        # Hello, world!
        
        Lorum ipsum dolor sit amet,
        consectetur adipiscing elit.
    """.trimIndent()

    val rendered = renderMarkdown(md) {
        // Add newline to soft line break
        softLineBreak {
            html.tag("br/")
            html.line()
        }
    }
    
    println(rendered.html)
}
```

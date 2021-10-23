package com.yogeshpaliyal.marky

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.AndroidUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.commonmark.node.*
import org.commonmark.node.Paragraph

/**
 * These functions will render a tree of Markdown nodes parsed with CommonMark.
 * Images will be rendered using Chris Banes Accompanist library (which uses Coil)
 *
 * To use this, you need the following two dependencies:
 * implementation "com.atlassian.commonmark:commonmark:0.15.2"
 * implementation "dev.chrisbanes.accompanist:accompanist-coil:0.2.0"
 *
 * The following is an example of how to use this:
 * ```
 * val parser = Parser.builder().build()
 * val root = parser.parse(MIXED_MD) as Document
 * val markdownComposer = MarkdownComposer()
 *
 * MarkdownComposerTheme {
 *    MDDocument(root)
 * }
 * ```
 */
private const val TAG_URL = "url"
private const val TAG_IMAGE_URL = "imageUrl"

@Composable
fun MDDocument(document: Document) {
    MDBlockChildren(document)
}

@Composable
fun MDHeading(heading: Heading) {
    val style = when (heading.level) {
        1 -> MaterialTheme.typography.h1
        2 -> MaterialTheme.typography.h2
        3 -> MaterialTheme.typography.h3
        4 -> MaterialTheme.typography.h4
        5 -> MaterialTheme.typography.h5
        6 -> MaterialTheme.typography.h6
        else -> {
            // Invalid header...
            MDBlockChildren(heading)
            return
        }
    }

    val padding = if (heading.parent is Document) 8.dp else 0.dp
    Box(modifier = Modifier.padding(bottom = padding)) {
        val text = buildAnnotatedString {
            inlineChildren(heading, this, MaterialTheme.colors)
        }
        MarkdownText(text, style)
    }
}

@Composable
fun MDParagraph(paragraph: Paragraph) {
    if (paragraph.firstChild is Image && paragraph.firstChild == paragraph.lastChild) {
        // Paragraph with single image
        MDImage(paragraph.firstChild as Image)
    } else {
        val padding = if (paragraph.parent is Document) 8.dp else 0.dp
        Box(modifier = Modifier.padding(bottom = padding)) {
            val styledText = buildAnnotatedString {
                pushStyle(MaterialTheme.typography.body1.toSpanStyle())
                inlineChildren(paragraph, this, MaterialTheme.colors)
                pop()
            }
            MarkdownText(styledText, MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun MDImage(image: Image) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Image(rememberImagePainter(image.destination), "")
    }
}

@Composable
fun MDBulletList(bulletList: BulletList) {
    val marker = bulletList.bulletMarker
    MDListItems(bulletList) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("$marker ")
            inlineChildren(it, this, MaterialTheme.colors)
            pop()
        }
        MarkdownText(text, MaterialTheme.typography.body1)
    }
}

@Composable
fun MDOrderedList(orderedList: OrderedList) {
    var number = orderedList.startNumber
    val delimiter = orderedList.delimiter
    MDListItems(orderedList) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("${number++}$delimiter ")
            inlineChildren(it, this, MaterialTheme.colors)
            pop()
        }
        MarkdownText(text, MaterialTheme.typography.body1)
    }
}

@Composable
fun MDListItems(listBlock: ListBlock, item: @Composable (node: Node) -> Unit) {
    val bottom = if (listBlock.parent is Document) 8.dp else 0.dp
    val start = if (listBlock.parent is Document) 0.dp else 8.dp
    Column(modifier = Modifier.padding(bottom = bottom, start = start)) {
        var listItem = listBlock.firstChild
        while (listItem != null) {
            var child = listItem.firstChild
            while (child != null) {
                when (child) {
                    is BulletList -> MDBulletList(child)
                    is OrderedList -> MDOrderedList(child)
                    else -> item(child)
                }
                child = child.next
            }
            listItem = listItem.next
        }
    }
}

@Composable
fun MDBlockQuote(blockQuote: BlockQuote) {
    val color = MaterialTheme.colors.onBackground
    Box(modifier = Modifier
        .drawBehind {
            drawLine(
                color = color,
                strokeWidth = 2f,
                start = Offset(12.dp.value, 0f),
                end = Offset(12.dp.value, size.height)
            )
        }
        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        val text = buildAnnotatedString {
            pushStyle(
                MaterialTheme.typography.body1.toSpanStyle()
                    .plus(SpanStyle(fontStyle = FontStyle.Italic))
            )
            inlineChildren(blockQuote, this, MaterialTheme.colors)
            pop()
        }
        Text(text)
    }
}

@Composable
fun MDFencedCodeBlock(fencedCodeBlock: FencedCodeBlock) {
    val padding = if (fencedCodeBlock.parent is Document) 8.dp else 0.dp
    Box(modifier = Modifier.padding(bottom = padding, start = 8.dp)) {
        Text(
            text = fencedCodeBlock.literal,
            style = TextStyle(fontFamily = FontFamily.Monospace)
        )
    }
}

@Composable
fun MDIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock) {
    // Ignored
}

@Composable
fun MDThematicBreak(thematicBreak: ThematicBreak) {
    //Ignored
}

@Composable
fun MDBlockChildren(parent: Node) {
    Column() {
        var child = parent.firstChild
        while (child != null) {
            when (child) {
                is Document -> MDDocument(child)
                is BlockQuote -> MDBlockQuote(child)
                is ThematicBreak -> MDThematicBreak(child)
                is Heading -> MDHeading(child)
                is Paragraph -> MDParagraph(child)
                is FencedCodeBlock -> MDFencedCodeBlock(child)
                is IndentedCodeBlock -> MDIndentedCodeBlock(child)
                is Image -> MDImage(child)
                is BulletList -> MDBulletList(child)
                is OrderedList -> MDOrderedList(child)
            }
            child = child.next
        }
    }

}

fun inlineChildren(
    parent: Node,
    annotatedString: AnnotatedString.Builder,
    colors: Colors
) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> inlineChildren(
                child,
                annotatedString,
                colors
            )
            is Text -> annotatedString.append(child.literal)
            is Image -> {
                annotatedString.appendInlineContent(TAG_IMAGE_URL, child.destination)
            }
            is Emphasis -> {
                annotatedString.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                inlineChildren(
                    child,
                    annotatedString,
                    colors
                )
                annotatedString.pop()
            }
            is StrongEmphasis -> {
                annotatedString.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                inlineChildren(
                    child,
                    annotatedString,
                    colors
                )
                annotatedString.pop()
            }
            is Code -> {
                annotatedString.pushStyle(TextStyle(fontFamily = FontFamily.Monospace).toSpanStyle())
                annotatedString.append(child.literal)
                annotatedString.pop()
            }
            is HardLineBreak -> {
                annotatedString.pushStyle(TextStyle(fontFamily = FontFamily.Monospace).toSpanStyle())
                annotatedString.append("\n")
                annotatedString.pop()
            }
            is Link -> {
                annotatedString.pushStyle(
                    SpanStyle(
                        color = colors.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
                annotatedString.pushStringAnnotation(TAG_URL, child.destination)
                inlineChildren(
                    child,
                    annotatedString,
                    colors
                )
                annotatedString.pop()
                annotatedString.pop()
            }
        }
        child = child.next

    }
}

@Composable
fun MarkdownText(text: AnnotatedString, style: TextStyle) {
    val uriHandler = AndroidUriHandler(LocalContext.current)
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(text = text,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    val position = layoutResult.getOffsetForPosition(pos)
                    text.getStringAnnotations(position, position)
                        .firstOrNull()
                        ?.let { sa ->
                            if (sa.tag == TAG_URL) {
                                uriHandler.openUri(sa.item)
                            }
                        }
                }
            }
        },
        style = style,
        inlineContent = mapOf(
            TAG_IMAGE_URL to InlineTextContent(
                Placeholder(
                    width = style.fontSize,
                    height = style.fontSize,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Bottom
                )
            ) {
                Image(rememberImagePainter(it), "", alignment = Alignment.Center)
            }
        ),
        onTextLayout = { layoutResult.value = it }
    )
}
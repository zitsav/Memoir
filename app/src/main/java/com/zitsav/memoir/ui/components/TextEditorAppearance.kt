package com.zitsav.memoir.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

class TextEditorAppearance : VisualTransformation {

    private val aiPattern = Regex("/ai\\{([\\s\\S]*?)\\}")

    private data class AiBlock(
        val originalRange: IntRange,
        val transformedRange: IntRange,
        val content: String
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val aiBlocks = mutableListOf<AiBlock>()
        val builder = AnnotatedString.Builder()
        var lastIndex = 0

        aiPattern.findAll(originalText).forEach { matchResult ->
            val content = matchResult.groupValues[1]
            builder.append(originalText.substring(lastIndex, matchResult.range.first))

            val transformedStart = builder.length
            builder.withStyle(SpanStyle(color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)) {
                append(content)
            }
            val transformedEnd = builder.length

            aiBlocks.add(
                AiBlock(
                    originalRange = matchResult.range,
                    transformedRange = transformedStart until transformedEnd,
                    content = content
                )
            )
            lastIndex = matchResult.range.last + 1
        }
        builder.append(originalText.substring(lastIndex))

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var consumedOriginalLength = 0
                var consumedTransformedLength = 0

                for (block in aiBlocks) {
                    if (offset <= block.originalRange.first) {
                        return offset - consumedOriginalLength + consumedTransformedLength
                    }
                    if (offset <= block.originalRange.last + 1) {
                        return block.transformedRange.last
                    }
                    consumedOriginalLength += block.originalRange.last - block.originalRange.first + 1
                    consumedTransformedLength += block.content.length
                }
                return offset - consumedOriginalLength + consumedTransformedLength
            }

            override fun transformedToOriginal(offset: Int): Int {
                var consumedOriginalLength = 0
                var consumedTransformedLength = 0

                for (block in aiBlocks) {
                    if (offset <= block.transformedRange.first) {
                        return offset - consumedTransformedLength + consumedOriginalLength
                    }
                    if (offset <= block.transformedRange.last) {
                        return block.originalRange.last + 1
                    }
                    consumedOriginalLength += block.originalRange.last - block.originalRange.first + 1
                    consumedTransformedLength += block.content.length
                }
                return offset - consumedTransformedLength + consumedOriginalLength
            }
        }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }
}
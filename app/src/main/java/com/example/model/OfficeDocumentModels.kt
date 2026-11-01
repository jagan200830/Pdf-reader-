package com.example.model

data class ExcelWorkbook(
    val fileName: String,
    val sheets: List<ExcelSheet>
)

data class ExcelSheet(
    val name: String,
    val headers: List<String>,
    val rows: List<List<String>>
)

data class WordDocument(
    val fileName: String,
    val elements: List<WordElement>
)

sealed class WordElement {
    data class Paragraph(val text: String, val isHeading: Boolean = false, val isBullet: Boolean = false) : WordElement()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : WordElement()
}

data class PptPresentation(
    val fileName: String,
    val slides: List<PptSlide>
)

data class PptSlide(
    val slideNumber: Int,
    val title: String,
    val contentLines: List<String>
)

data class TextDocument(
    val fileName: String,
    val text: String,
    val lines: List<String>,
    val lineCount: Int,
    val wordCount: Int,
    val charCount: Int
)

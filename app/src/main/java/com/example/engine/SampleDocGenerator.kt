package com.example.engine

import android.content.Context
import com.example.model.DocumentItem
import java.io.File

object SampleDocGenerator {

    fun ensureSampleDocuments(context: Context): List<DocumentItem> {
        val docsDir = File(context.filesDir, "device_documents").apply { mkdirs() }
        val generatedItems = mutableListOf<DocumentItem>()

        // 1. PowerPoint Presentation (.pptx)
        try {
            val pptxFile = File(docsDir, "NextGen_AI_Strategy_2026.pptx")
            if (!pptxFile.exists() || pptxFile.length() < 100) {
                val slidesXml = mapOf(
                    "ppt/slides/slide1.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:sp><p:txBody>
    <p><t>DocMaster Next-Gen Productivity</t></p>
    <p><t>Private on-device document processing workstation</t></p>
    <p><t>Zero latency native Android conversion engines</t></p>
    <p><t>Built for security, speed and complete privacy</t></p>
  </p:txBody></p:sp></p:spTree></p:cSld>
</p:sld>""",
                    "ppt/slides/slide2.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:sp><p:txBody>
    <p><t>Universal Document Support</t></p>
    <p><t>Full-screen interactive PDF viewer with page jump and zoom</t></p>
    <p><t>Rich PowerPoint slide presentation engine with full-screen deck mode</t></p>
    <p><t>Word DOCX document viewer with formatted sections</t></p>
    <p><t>Interactive Excel multi-sheet grid viewer</t></p>
  </p:txBody></p:sp></p:spTree></p:cSld>
</p:sld>""",
                    "ppt/slides/slide3.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:sp><p:txBody>
    <p><t>Security Architecture & Privacy</t></p>
    <p><t>100% on-device local execution</t></p>
    <p><t>No files sent to external cloud servers</t></p>
    <p><t>Automatic post-conversion input file cleanup</t></p>
    <p><t>Persistent Room history for offline access</t></p>
  </p:txBody></p:sp></p:spTree></p:cSld>
</p:sld>""",
                    "ppt/slides/slide4.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:sp><p:txBody>
    <p><t>Roadmap & Future Horizons</t></p>
    <p><t>Batch OCR document text extraction</t></p>
    <p><t>High-precision PDF digital signature stamping</t></p>
    <p><t>Seamless device files synchronization</t></p>
  </p:txBody></p:sp></p:spTree></p:cSld>
</p:sld>"""
                )
                ZipEngine.createZipFromEntries(slidesXml, pptxFile)
            }
            if (pptxFile.exists()) {
                generatedItems.add(
                    DocumentItem(
                        name = pptxFile.name,
                        sizeBytes = pptxFile.length(),
                        mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        pageCount = 4,
                        tempFilePath = pptxFile.absolutePath
                    )
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 2. Multi-Page PDF Document (.pdf)
        val pdfFile = File(docsDir, "DocMaster_System_Manual.pdf")
        if (!pdfFile.exists() || pdfFile.length() < 100) {
            try {
                val pdfContent = """%PDF-1.4
1 0 obj
<< /Type /Catalog /Pages 2 0 R >>
endobj
2 0 obj
<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 2 >>
endobj
3 0 obj
<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 5 0 R /Resources << /Font << /F1 7 0 R >> >> >>
endobj
4 0 obj
<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 6 0 R /Resources << /Font << /F1 7 0 R >> >> >>
endobj
5 0 obj
<< /Length 260 >>
stream
BT
/F1 22 Tf
50 780 Td
(DocMaster Workstation Manual) Tj
/F1 12 Tf
0 -36 Td
(1. Full-Screen Document Viewing: Open PDF, PPTX, Word and Excel files.) Tj
0 -26 Td
(2. Privacy & On-Device Security: 100% private processing on CPU/GPU.) Tj
0 -26 Td
(3. High-Speed Conversion: Local native transformations without cloud servers.) Tj
ET
endstream
endobj
6 0 obj
<< /Length 240 >>
stream
BT
/F1 22 Tf
50 780 Td
(DocMaster Security & Architecture) Tj
/F1 12 Tf
0 -36 Td
(Zero external network requirements. Files are processed entirely local.) Tj
0 -26 Td
(All temporary input files are automatically wiped after conversion.) Tj
ET
endstream
endobj
7 0 obj
<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>
endobj
xref
0 8
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000230 00000 n 
0000000345 00000 n 
0000000657 00000 n 
0000000949 00000 n 
trailer
<< /Size 8 /Root 1 0 R >>
startxref
1016
%%EOF
"""
                pdfFile.writeText(pdfContent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        if (pdfFile.exists() && pdfFile.length() > 100) {
            generatedItems.add(
                DocumentItem(
                    name = pdfFile.name,
                    sizeBytes = pdfFile.length(),
                    mimeType = "application/pdf",
                    pageCount = 2,
                    tempFilePath = pdfFile.absolutePath
                )
            )
        }

        // 3. Word Document (.docx)
        try {
            val docxFile = File(docsDir, "Product_Roadmap_Specification.docx")
            if (!docxFile.exists() || docxFile.length() < 100) {
                val docxXml = mapOf(
                    "word/document.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p><w:r><w:t>DocMaster Product Specification &amp; Design Document</w:t></w:r></w:p>
    <w:p><w:r><w:t>Project Objective: Provide a futuristic, cosmic-styled mobile workstation that eliminates reliance on heavy cloud converters.</w:t></w:r></w:p>
    <w:p><w:r><w:t>Section 1: User Experience</w:t></w:r></w:p>
    <w:p><w:r><w:t>The interface adopts deep cosmic violet tones with luminous magenta and cyan neon borders. Floating glassmorphic cards display real-time status and file details.</w:t></w:r></w:p>
    <w:p><w:r><w:t>Section 2: Full Screen Canvas</w:t></w:r></w:p>
    <w:p><w:r><w:t>Documents expand to fit the entire display viewport. Unnecessary status indicators or outer white padding are suppressed for an immersive reading experience.</w:t></w:r></w:p>
    <w:p><w:r><w:t>Section 3: Device Library Integration</w:t></w:r></w:p>
    <w:p><w:r><w:t>Users can view any presentation, spreadsheet, document or PDF directly from their device storage without prior conversion.</w:t></w:r></w:p>
  </w:body>
</w:document>"""
                )
                ZipEngine.createZipFromEntries(docxXml, docxFile)
            }
            if (docxFile.exists()) {
                generatedItems.add(
                    DocumentItem(
                        name = docxFile.name,
                        sizeBytes = docxFile.length(),
                        mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        pageCount = 1,
                        tempFilePath = docxFile.absolutePath
                    )
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 4. Excel Spreadsheet (.xlsx / .csv)
        try {
            val xlsxFile = File(docsDir, "Global_Financial_Model.csv")
            if (!xlsxFile.exists() || xlsxFile.length() < 50) {
                val csvContent = """Quarter,Revenue,Operations,Gross Margin,Net Profit,Status
Q1 2026,$1,450,000,$820,000,76.8%,$630,000,Completed
Q2 2026,$1,890,000,$940,000,101.0%,$950,000,Target Reached
Q3 2026,$2,350,000,$1,080,000,117.5%,$1,270,000,On Track
Q4 2026,$3,100,000,$1,250,000,148.0%,$1,850,000,Projected
Annual Total,$8,790,000,$4,090,000,114.9%,$4,700,000,Audited"""
                xlsxFile.writeText(csvContent)
            }
            if (xlsxFile.exists()) {
                generatedItems.add(
                    DocumentItem(
                        name = xlsxFile.name,
                        sizeBytes = xlsxFile.length(),
                        mimeType = "text/csv",
                        pageCount = 1,
                        tempFilePath = xlsxFile.absolutePath
                    )
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 5. Release Notes (.txt)
        try {
            val txtFile = File(docsDir, "Release_Notes_v2.0.txt")
            if (!txtFile.exists() || txtFile.length() < 20) {
                txtFile.writeText(
                    """DocMaster v2.0 - Release Notes & Changelog
===========================================
- New Feature: View all device documents (PDF, PPTX, Word, Excel) without conversion.
- Feature: Automatic deletion of uploaded input files upon conversion completion.
- Feature: Immersive zero-whitespace full-screen document viewing mode.
- Feature: High-speed search bar in History & All Documents screens.
- UI: Deep cosmic nebula sci-fi theme with frosted glassmorphic card styling.
"""
                )
            }
            if (txtFile.exists()) {
                generatedItems.add(
                    DocumentItem(
                        name = txtFile.name,
                        sizeBytes = txtFile.length(),
                        mimeType = "text/plain",
                        pageCount = 1,
                        tempFilePath = txtFile.absolutePath
                    )
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return generatedItems
    }
}

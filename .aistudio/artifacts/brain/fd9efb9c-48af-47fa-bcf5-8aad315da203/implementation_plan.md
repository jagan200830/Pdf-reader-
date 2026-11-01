# DocMaster – All-in-One File Converter & Document Manager

An all-in-one Android document and file toolkit built with Kotlin and Jetpack Compose. DocMaster provides 100% on-device, privacy-preserving file conversion, editing, compression, viewing, and batch processing for PDFs, Office documents (DOCX, XLSX, PPTX), images (JPG, PNG, WEBP), and plain text.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1:
> - **Workspace Mode**: Starting clean with an empty workspace, ready for user document uploads via Android System Photo & Document Picker (SAF) and file drag-and-drop.
> - **Aesthetic & Theme**: Modern Indigo and Violet SaaS aesthetic with sleek elevated cards, subtle gradients, fluid micro-interactions, and dark/light mode support.
> - **Processing Engine**: 100% on-device local processing using Android's native `PdfRenderer`, `PdfDocument`, Bitmap rendering pipeline, and native XML/ZIP Office document parsers—guaranteeing zero data leakage, instant offline speed, and complete privacy.

---

## 1. Overview & Core Concept

DocMaster eliminates the need for multiple single-purpose document apps or privacy-compromising online converters. Users can process, convert, compress, view, and organize files directly on their Android device:

- **PDF Power Suite**: Merge, split, compress, rotate, extract, delete/reorder pages, watermark, add page numbers, protect/unlock, add images, and convert to/from images and documents.
- **Image Processing Hub**: Convert formats (JPG, PNG, WEBP, PDF), bulk image-to-PDF merging, resizing, compression with live size comparison, rotation, and aspect ratio cropping.
- **Office & Document Suite**:
  - **Excel (XLSX/XLS)**: Multi-sheet spreadsheet viewer with sortable columns, text search, filtering, and export to CSV or PDF.
  - **Word (DOCX/DOC)**: Structured document viewer with text/table rendering and export to PDF.
  - **PowerPoint (PPTX/PPT)**: Interactive slide deck viewer with carousel navigation, fullscreen presentation mode, and export to PDF.
  - **Text / Code (TXT)**: Formatted text viewer with line numbers and search.
- **Batch Processing & ZIP Archival**: Convert multiple items concurrently, compare before-and-after compression ratios, and export individual files or bundle them into a ZIP archive.
- **Recent Files Activity**: Persistent Room database tracking processed files with quick preview, re-share, and deletion.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard Discovery**: The user opens DocMaster to a clean SaaS hero section with an active Drag & Drop / File Browser dropzone, followed by categorized tool cards (PDF Tools, Image Tools, Office Tools, Viewers) and a collapsible Recent Files tray.
2. **File Ingestion**: Tapping any tool or dropping a file opens the Android Storage Access Framework (SAF) document or photo picker (supporting single or multi-file selection).
3. **Configuration & Live Preview**: The tool screen displays the loaded file(s), file size, visual thumbnails or page counts, and interactive controls (e.g., compression sliders, rotation buttons, watermark text input).
4. **Processing & Feedback**: Tapping the action button triggers an asynchronous Coroutine with smooth animated progress indicators.
5. **Result & Export**: A success card shows original vs. output file size, savings percentage, a live preview button, and direct actions to **Save to Device**, **Share via Android Sheet**, or **Export as ZIP**.

### Visual Identity & Theme

- **Aesthetic Direction**: High-end SaaS dashboard inspired by modern productivity tools (Linear, Smallpdf, Acrobat). Minimalist, clean, and distraction-free with rounded corners (16–24dp).
- **Color Tokens**:
  - *Primary*: Royal Indigo (`#4F46E5` / `#6366F1`)
  - *Secondary / Accent*: Electric Violet (`#8B5CF6` / `#A855F7`)
  - *Surface Light*: Pure White (`#FFFFFF`) with Soft Gray backdrop (`#F8FAFC`)
  - *Surface Dark*: Deep Midnight (`#0F172A`) with Slate container (`#1E293B`)
  - *Success / Savings*: Emerald (`#10B981`) for compression stats and download badges
  - *Warning / Error*: Rose (`#EF4444`) for delete actions and file errors
- **Typography & Hierarchy**:
  - *Display / Titles*: Bold sans-serif with tight letter spacing for hero headlines and section headers.
  - *Body / Meta*: Crisp neutral typography with tabular figures (`tnum`) for file sizes, page numbers, and percentages.
- **Layout & Responsiveness**:
  - Dynamic grid adapting from single column on compact phone screens to multi-column staggered grids on foldables and tablets.
  - Minimum touch targets strictly adhering to 48dp with tactile ripple effects.

---

## 3. Key Product Decisions & Trade-Offs

- **On-Device Native Document Pipeline vs. External Cloud APIs**:
  - *Chosen Approach*: 100% on-device processing via Android `PdfRenderer`, `PdfDocument`, `BitmapFactory`, and custom ZIP/XML streaming parsers.
  - *Rationale*: Zero server costs, zero network latency, and complete security compliance. User documents never leave the device.
  - *Trade-off*: Advanced proprietary binary formats like legacy `.doc` or complex Word macros are handled via text/structure extraction rather than full desktop rendering engines.
- **Direct XML/ZIP Office Parsing vs. Bulky External SDKs**:
  - *Chosen Approach*: Native Java/Kotlin `ZipInputStream` and streaming XML parsing for `.docx`, `.xlsx`, and `.pptx` files.
  - *Rationale*: Standard Office Open XML (.docx, .xlsx, .pptx) files are zipped XML archives. Custom streaming parsers keep the APK ultra-lightweight (<25MB) while providing accurate sheet, slide, and paragraph extraction.
- **Persistence Strategy**:
  - *Chosen Approach*: Room Database for local recent history (metadata, file paths, sizes, dates, tool types) coupled with internal app storage caching for processed outputs.
  - *Rationale*: Instant app launches, zero cloud setup requirements, and user privacy.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          DocMaster UI Layer                            │
├────────────────────────────────────────────────────────────────────────┤
│  TopAppBar (Theme Toggle, Search) │ Hero Upload Zone (SAF Picker)      │
│  Tool Categorized Grid (PDF, Images, Office, Converter, Compression)   │
│  Recent Files Drawer & History Screen                                  │
│  Active Tool View (Config, Preview, Progress, Result Comparison)       │
│  Multi-Format Document Viewer (PDF, Excel, Word, PPTX, Text)           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ MVI / StateFlow
┌───────────────────────────────────▼────────────────────────────────────┐
│                       DocMasterViewModel & State                       │
│  - ActiveToolState, UploadedFilesState, ProcessingState, ResultsState  │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
┌───────────────────▼─────────────┐    ┌─────────────▼───────────────────┐
│     Local History Repository    │    │      Processing Engines         │
│  - Room Database (HistoryDao)   │    │  - PdfEngine (Merge/Split/Water)│
│  - App Cache / Storage Manager  │    │  - ImageEngine (Resize/Compress)│
│                                 │    │  - OfficeEngine (DOCX/XLSX/PPTX)│
│                                 │    │  - ZipEngine (Batch Archiving)  │
└─────────────────────────────────┘    └─────────────────────────────────┘
```

### Core Data Models

- `DocumentItem`: Represents an uploaded or processed file (`id`, `name`, `uri`, `mimeType`, `sizeBytes`, `pageCount`, `timestamp`, `thumbnailBitmap`).
- `ToolCategory` & `DocTool`: Definitions for tools (`id`, `title`, `description`, `icon`, `category`, `acceptedMimeTypes`).
- `ProcessingResult`: Output of an operation (`success`, `originalSizeBytes`, `processedSizeBytes`, `outputUris`, `compressionRatio`, `message`).
- `ProcessedFileEntity`: Room entity caching historical operations for the Recent Files dashboard.

### Interactive Component & State Mapping

- `DashboardScreen`: Renders the category tabs, tool grid, quick-launch buttons, and empty-state upload dropzone.
- `ToolExecutionScreen`: Dynamic workbench displaying file chips, parameter adjusters (sliders, inputs, switches), conversion CTA, live progress bar, and result comparison card.
- `DocumentViewerScreen`: Multi-engine viewer with zoom/pan gesture support for PDFs, grid table viewer for spreadsheets, slide carousel for presentations, and formatted reader for documents.
- `HistoryScreen`: Searchable, filterable list of previous conversions with re-download, share, and clear-history actions.

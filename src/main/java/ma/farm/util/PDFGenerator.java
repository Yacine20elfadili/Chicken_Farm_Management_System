package ma.farm.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * PDFGenerator - Utility class for generating PDF documents using iText7.
 * 
 * LICENSE WARNING:
 * ================
 * iText7 uses the AGPL (Affero General Public License).
 * 
 * If you plan to SELL or DISTRIBUTE this application commercially, you MUST:
 * 1. Purchase a commercial iText license (~€1000+), OR
 * 2. Switch to Apache PDFBox (Apache 2.0 license - 100% free for commercial
 * use)
 * 
 * For open-source/personal use, iText7 is FREE under AGPL.
 * 
 * See: https://itextpdf.com/products/itext-7/community
 */
public class PDFGenerator {

    // Directory to save all generated PDFs
    public static final String DOCUMENTS_DIR = System.getProperty("user.dir") + File.separator + "Documents"
            + File.separator;

    // Colors
    private static final DeviceRgb HEADER_BG = new DeviceRgb(52, 73, 94); // Dark blue-gray
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(41, 128, 185); // Blue
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    // Date formatter
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Ensures the Documents directory exists, creates it if not.
     */
    public static void ensureDirectoryExists() {
        File dir = new File(DOCUMENTS_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created Documents directory: " + DOCUMENTS_DIR);
            } else {
                System.err.println("Failed to create Documents directory: " + DOCUMENTS_DIR);
            }
        }
    }

    /**
     * Create a new PDF document with standard A4 page size.
     * 
     * @param filename The filename (without path, e.g., "BC-2024-001.pdf")
     * @return Document object ready for adding content
     * @throws IOException If file creation fails
     */
    public static Document createDocument(String filename) throws IOException {
        ensureDirectoryExists();
        String fullPath = DOCUMENTS_DIR + filename;

        System.out.println("Attempting to create PDF at: " + fullPath);
        File file = new File(fullPath);

        try {
            // Using FileOutputStream explicitly to ensure file creation
            PdfWriter writer = new PdfWriter(new java.io.FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36); // 0.5 inch margins

            System.out.println("✅ PDF Writer initialized successfully for: " + file.getAbsolutePath());
            return document;
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR: Failed to create PDF writer: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to initialize PDF writer", e);
        }
    }

    /**
     * Add a document header (Title + Document Number + Date).
     */
    public static void addDocumentHeader(Document doc, String title, String docNumber, LocalDate date)
            throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Title
        Paragraph titlePara = new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(24)
                .setFontColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        doc.add(titlePara);

        // Document number and date
        Table infoTable = new Table(2).useAllAvailableWidth();
        infoTable.addCell(createCell("N°: " + docNumber, regularFont, 12, TextAlignment.LEFT));
        infoTable.addCell(createCell("Date: " + date.format(DATE_FORMAT), regularFont, 12, TextAlignment.RIGHT));
        doc.add(infoTable);

        // Separator line
        doc.add(new Paragraph().setBorderBottom(new SolidBorder(ACCENT_COLOR, 2)).setMarginBottom(15));
    }

    /**
     * Add a party information block (Issuer/Supplier/Customer info).
     */
    public static void addPartyInfo(Document doc, String sectionTitle,
            String companyName, String ice, String rc,
            String address, String phone, String email) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Section title
        Paragraph sectionHeader = new Paragraph(sectionTitle)
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(HEADER_BG)
                .setPadding(5)
                .setMarginTop(10);
        doc.add(sectionHeader);

        // Info table
        Table infoTable = new Table(2).useAllAvailableWidth()
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(5);

        infoTable.addCell(createInfoCell("Société:", companyName, boldFont, regularFont));
        infoTable.addCell(createInfoCell("ICE:", ice, boldFont, regularFont));
        infoTable.addCell(createInfoCell("RC:", rc != null ? rc : "-", boldFont, regularFont));
        infoTable.addCell(createInfoCell("Tél:", phone != null ? phone : "-", boldFont, regularFont));
        infoTable.addCell(createInfoCell("Adresse:", address != null ? address : "-", boldFont, regularFont));
        infoTable.addCell(createInfoCell("Email:", email != null ? email : "-", boldFont, regularFont));

        doc.add(infoTable);
    }

    /**
     * Add an items table (for order lines, invoice lines, etc.).
     */
    public static Table createItemsTable(String[] headers, float[] columnWidths) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth()
                .setMarginTop(15);

        // Header row
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setFont(boldFont).setFontSize(10))
                    .setBackgroundColor(HEADER_BG)
                    .setFontColor(ColorConstants.WHITE)
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(headerCell);
        }

        return table;
    }

    /**
     * Add a row to an items table.
     */
    public static void addTableRow(Table table, String[] values) throws IOException {
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        for (int i = 0; i < values.length; i++) {
            Cell cell = new Cell()
                    .add(new Paragraph(values[i]).setFont(regularFont).setFontSize(9))
                    .setPadding(5)
                    .setTextAlignment(i == 0 ? TextAlignment.LEFT : TextAlignment.CENTER);

            // Alternate row colors
            if (table.getNumberOfRows() % 2 == 0) {
                cell.setBackgroundColor(LIGHT_GRAY);
            }

            table.addCell(cell);
        }
    }

    /**
     * Add financial summary (Total HT, TVA, Total TTC).
     */
    public static void addFinancialSummary(Document doc, double totalHT, double tvaRate, double totalTTC)
            throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        double tvaAmount = totalHT * tvaRate / 100;

        Table summaryTable = new Table(2)
                .setWidth(UnitValue.createPercentValue(40))
                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                .setMarginTop(15);

        summaryTable.addCell(createSummaryCell("Total HT:", regularFont));
        summaryTable.addCell(createSummaryCell(String.format("%.2f DH", totalHT), regularFont));

        summaryTable.addCell(createSummaryCell("TVA (" + (int) tvaRate + "%):", regularFont));
        summaryTable.addCell(createSummaryCell(String.format("%.2f DH", tvaAmount), regularFont));

        summaryTable.addCell(createSummaryCell("Total TTC:", boldFont).setBackgroundColor(ACCENT_COLOR)
                .setFontColor(ColorConstants.WHITE));
        summaryTable.addCell(createSummaryCell(String.format("%.2f DH", totalTTC), boldFont)
                .setBackgroundColor(ACCENT_COLOR).setFontColor(ColorConstants.WHITE));

        doc.add(summaryTable);
    }

    /**
     * Add signature section at the bottom.
     */
    public static void addSignatureSection(Document doc, String leftLabel, String rightLabel) throws IOException {
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        Table sigTable = new Table(2).useAllAvailableWidth().setMarginTop(40);

        Cell leftCell = new Cell()
                .add(new Paragraph(leftLabel).setFont(regularFont).setFontSize(10))
                .add(new Paragraph("\n\n\n_______________________").setFont(regularFont))
                .add(new Paragraph("Signature + Cachet").setFont(regularFont).setFontSize(8))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        Cell rightCell = new Cell()
                .add(new Paragraph(rightLabel).setFont(regularFont).setFontSize(10))
                .add(new Paragraph("\n\n\n_______________________").setFont(regularFont))
                .add(new Paragraph("Signature + Cachet").setFont(regularFont).setFontSize(8))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        sigTable.addCell(leftCell);
        sigTable.addCell(rightCell);
        doc.add(sigTable);
    }

    /**
     * Add footer text.
     */
    public static void addFooter(Document doc, String footerText) throws IOException {
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        Paragraph footer = new Paragraph(footerText)
                .setFont(regularFont)
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
        doc.add(footer);
    }

    // --- Helper Methods ---

    private static Cell createCell(String text, PdfFont font, int fontSize, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(fontSize))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(alignment);
    }

    private static Cell createInfoCell(String label, String value, PdfFont labelFont, PdfFont valueFont) {
        return new Cell()
                .add(new Paragraph()
                        .add(new Text(label + " ").setFont(labelFont).setFontSize(9))
                        .add(new Text(value).setFont(valueFont).setFontSize(9)))
                .setBorder(Border.NO_BORDER)
                .setPadding(3);
    }

    private static Cell createSummaryCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT);
    }

    /**
     * Generate a sequential document number.
     * Format: PREFIX-YEAR-SEQUENCE (e.g., BC-2024-001)
     */
    public static String generateDocNumber(String prefix, int sequence) {
        int year = LocalDate.now().getYear();
        return String.format("%s-%d-%03d", prefix, year, sequence);
    }
}

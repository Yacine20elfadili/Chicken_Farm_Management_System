package ma.farm.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * Utility to generate identity card PNG images from an HTML template.
 * Uses OpenHTMLToPDF to render HTML -> PDF, then PDFBox to rasterize PDF -> PNG.
 */
public class IdentityCardGenerator {

    private static final String TEMPLATE_PATH = "/templates/identity-card.html";
    private static final int RENDER_DPI = 300;

    public byte[] generateAsPng(String name, String role, String barcodeData) throws IOException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (role == null) role = "";
        if (barcodeData == null) barcodeData = "";

        // 1) Generate QR image (PNG byte[])
        byte[] qrPng = generateBarcodePng(barcodeData);

        // Save QR image under src/main/resources/images/qrcodes and compute an absolute file URI for the template
        Path qrcodePath = saveQrcodeToResources(qrPng, barcodeData); // returns the path to the saved file
        String qrcodeFileUri = qrcodePath.toUri().toString();

        // 2) Load HTML template and replace placeholders
        String html = loadTemplate();
        html = html.replace("{{name}}", escapeHtml(name))
                   .replace("{{role}}", escapeHtml(role))
                   .replace("{{barcodeDataImg}}", qrcodeFileUri);

        // 3) Render HTML to PDF using OpenHTMLToPDF
        // Sanitize HTML (remove any characters before the first '<' to avoid XML parser errors)
        int firstLt = html.indexOf('<');
        if (firstLt > 0) {
            html = html.substring(firstLt);
        }

        // Write debug copy of HTML to target/identity-debug.html to help diagnose parsing issues
        try {
            Path debug = Path.of("target", "identity-debug.html");
            Files.createDirectories(debug.getParent());
            Files.writeString(debug, html, StandardCharsets.UTF_8);
        } catch (Exception ignore) {
            // best-effort
        }

        ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        URL templateUrl = IdentityCardGenerator.class.getResource(TEMPLATE_PATH);
        String baseUri = null;
        if (templateUrl != null) {
            String t = templateUrl.toExternalForm();
            int idx = t.lastIndexOf('/');
            baseUri = idx >= 0 ? t.substring(0, idx + 1) : t;
        }
        builder.withHtmlContent(html, baseUri);
        builder.toStream(pdfBaos);
        try {
            builder.run();
        } catch (Exception e) {
            // ensure we attempt to delete the temporary QR file if present
            if (qrcodePath != null) {
                try { Files.deleteIfExists(qrcodePath); } catch (Exception ignore) {}
            }
            throw new IOException("Failed to render HTML to PDF: " + e.getMessage(), e);
        }

        // 4) Rasterize first page to PNG using PDFBox
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdfBaos.toByteArray()))) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImageWithDPI(0, RENDER_DPI, ImageType.RGB);

            // Overlay a high-resolution QR directly onto the rendered image only if the template did NOT include an image
            boolean templateHasQr = html != null && html.contains("{{barcodeDataImg}}") == false && html.contains("qrcodes/") ;
            if (!templateHasQr && barcodeData != null && !barcodeData.isBlank()) {
                try {
                    // create a large square QR BitMatrix and scale down to the top-center area
                    int requestedSide = Math.max(1200, Math.min(image.getWidth() / 2, image.getHeight() / 2));
                    Map<EncodeHintType, Object> hints = new HashMap<>();
                    hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
                    hints.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
                    hints.put(com.google.zxing.EncodeHintType.MARGIN, 1);

                    BitMatrix matrix = new MultiFormatWriter().encode(barcodeData, BarcodeFormat.QR_CODE, requestedSide, requestedSide, hints);
                    BufferedImage barcodeImg = MatrixToImageWriter.toBufferedImage(matrix);

                    // Compute destination rectangle at the TOP CENTER area (portrait layout)
                    int imgW = image.getWidth();
                    int imgH = image.getHeight();
                    int padding = Math.max(24, imgW/40);
                    int areaW = imgW - padding*2;
                    int maxH = imgH / 2; // top half reserved for QR and caption

                    double scale = Math.min((double) areaW / barcodeImg.getWidth(), (double) maxH / barcodeImg.getHeight());
                    int finalW = (int) Math.round(barcodeImg.getWidth() * scale);
                    int finalH = (int) Math.round(barcodeImg.getHeight() * scale);
                    int x = (imgW - finalW) / 2;
                    int y = padding;

                    java.awt.Graphics2D g = image.createGraphics();
                    g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                    // draw subtle drop shadow rectangle behind QR (slightly offset)
                    int shadowOffset = Math.max(6, image.getWidth() / 200);
                    int pad = Math.max(12, image.getWidth() / 80);
                    int rectX = Math.max(0, x - pad);
                    int rectY = Math.max(0, y - pad);
                    int rectW = Math.min(image.getWidth(), finalW + pad * 2);
                    int rectH = Math.min(image.getHeight(), finalH + pad * 2);

                    // shadow (semi-transparent rounded rect)
                    g.setColor(new java.awt.Color(0, 0, 0, 30));
                    g.fillRoundRect(rectX + shadowOffset, rectY + shadowOffset, rectW, rectH, 16, 16);

                    // white rounded panel
                    g.setColor(java.awt.Color.WHITE);
                    g.fillRoundRect(rectX, rectY, rectW, rectH, 16, 16);
                    g.setColor(new java.awt.Color(0,0,0,20));
                    g.drawRoundRect(rectX, rectY, rectW, rectH, 16, 16);

                    // finally draw the QR centered in that panel
                    g.drawImage(barcodeImg, x, y, finalW, finalH, null);
                    g.dispose();
                } catch (Exception e) {
                    // If overlay fails, ignore and continue with originally embedded image
                    System.err.println("Warning: failed to overlay QR code: " + e.getMessage());
                }
            }

            ByteArrayOutputStream pngBaos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngBaos);

            // delete the temporary QR file now that the card image has been produced
            if (qrcodePath != null) {
                try { Files.deleteIfExists(qrcodePath); } catch (Exception ignore) { System.err.println("Warning: failed to delete temporary qrcode: " + ignore.getMessage()); }
            }

            return pngBaos.toByteArray();
        }
    }

    public void saveAsPng(String name, String role, String barcodeData, Path dest) throws IOException {
        byte[] png = generateAsPng(name, role, barcodeData);
        Files.write(dest, png);
    }

    private byte[] generateBarcodePng(String data) throws IOException {
        if (data == null) data = "";
        // Generate a QR code (square) with high error correction and UTF-8 charset
        Map<com.google.zxing.EncodeHintType, Object> hints = new HashMap<>();
        hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(com.google.zxing.EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
        hints.put(com.google.zxing.EncodeHintType.MARGIN, 1);
        int size = 800; // square size (px)
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    private String loadTemplate() throws IOException {
        // Load HTML template
        InputStream in = IdentityCardGenerator.class.getResourceAsStream(TEMPLATE_PATH);
        if (in == null) throw new FileNotFoundException("Template not found: " + TEMPLATE_PATH);
        String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        // Inline CSS (replace <link href="identity-card.css"> with <style>...</style>)
        try (InputStream cssIn = IdentityCardGenerator.class.getResourceAsStream("/templates/identity-card.css")) {
            if (cssIn != null) {
                String css = new String(cssIn.readAllBytes(), StandardCharsets.UTF_8);
                html = html.replaceFirst("<link[^>]*href=[\"']identity-card.css[\"'][^>]*>", "<style>" + css + "</style>");
            }
        }

        // Inline logo file as data URI so OpenHTMLToPDF doesn't need to resolve jar paths
        try (InputStream logoIn = IdentityCardGenerator.class.getResourceAsStream("/images/id-card/logo.svg")) {
            if (logoIn != null) {
                String svg = new String(logoIn.readAllBytes(), StandardCharsets.UTF_8);
                String svgBase64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
                String dataUri = "data:image/svg+xml;base64," + svgBase64;
                html = html.replace("../images/id-card/logo.svg", dataUri);
            }
        }

        return html;
    }

    private String escapeHtml(String input) {
        return input.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;");
    }

    /**
     * Save QR PNG bytes to src/main/resources/images/qrcodes and return the Path used.
     * Uses a sanitized timestamp-based filename to avoid collisions.
     */
    private Path saveQrcodeToResources(byte[] pngBytes, String contentHint) throws IOException {
        Path imagesDir = Path.of("src", "main", "resources", "images", "qrcodes");
        Files.createDirectories(imagesDir);
        // use timestamp + short hash of content to avoid collisions
        String safe = (contentHint == null || contentHint.isBlank()) ? "qr" : contentHint.replaceAll("[^a-zA-Z0-9-_]","_");
        String filename = String.format("qrcode_%d_%s.png", System.currentTimeMillis(), safe.length() > 20 ? safe.substring(0,20) : safe);
        Path out = imagesDir.resolve(filename);
        Files.write(out, pngBytes);
        // return the Path so callers can convert to a URI safely on all platforms
        return out;
    }
}

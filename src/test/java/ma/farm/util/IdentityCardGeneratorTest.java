package ma.farm.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

class IdentityCardGeneratorTest {

    @Test
    void generatePng_returnsPngHeaderAndNonEmpty() throws Exception {
        IdentityCardGenerator g = new IdentityCardGenerator();
        byte[] png = g.generateAsPng("Mohamed Hassan", "Ouvrier Agricole", "EMP-0001");
        assertNotNull(png);
        assertTrue(png.length > 1000, "PNG appears too small");
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        assertEquals((byte)0x89, png[0]);
        assertEquals((byte)0x50, png[1]);
        assertEquals((byte)0x4E, png[2]);
        assertEquals((byte)0x47, png[3]);
    }

    @Test
    void generatePng_includesBarcode_decodable() throws Exception {
        IdentityCardGenerator g = new IdentityCardGenerator();
        byte[] png = g.generateAsPng("Test User", "Role", "EMP-0001");
        ByteArrayInputStream bais = new ByteArrayInputStream(png);
        BufferedImage img = ImageIO.read(bais);
        assertNotNull(img, "Failed to read generated PNG image");

        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        assertNotNull(result);
        assertEquals("EMP-0001", result.getText());
    }
}

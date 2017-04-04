package utils;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;

public class ImageUtil {

    public final ByteBuffer image;
    public int w;
    public int h;
    public int comp;

    public ImageUtil(String imagePath) {
        ByteBuffer imageBuffer;
        try {
            imageBuffer = IOUtil.ioResourceToByteBuffer(imagePath, 8 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);

        // Use info to read image metadata without decoding the entire image.
        // We don't need this for this demo, just testing the API.
        if (!stbi_info_from_memory(imageBuffer, w, h, comp))
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

        System.out.println("ImageUtil width: " + w.get(0));
        System.out.println("ImageUtil height: " + h.get(0));
        System.out.println("ImageUtil components: " + comp.get(0));
        System.out.println("ImageUtil HDR: " + stbi_is_hdr_from_memory(imageBuffer));

        // Decode the image
        image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
        if (image == null)
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

        this.w = w.get(0);
        this.h = h.get(0);
        this.comp = comp.get(0);
    }
}
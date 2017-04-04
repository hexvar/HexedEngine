package gui;


import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class DrawText {

    static boolean antiAlias = true;

    static Font awtFont = new Font("Times New Roman", Font.BOLD, 14);
    //static TrueTypeFont font = new TrueTypeFont(awtFont, antiAlias);

    public static ByteBuffer drawText(int x, int y, String text, Color c) {
        int sz = 256; //Take whatever size suits you.
        BufferedImage b = new BufferedImage(sz, sz, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = b.createGraphics();
        g.drawString(text, 0, 0);

        int co = b.getColorModel().getNumComponents();

        byte[] data = new byte[co * sz * sz];
        b.getRaster().getDataElements(0, 0, sz, sz, data);

        ByteBuffer pixels = BufferUtils.createByteBuffer(data.length);
        pixels.put(data);
        pixels.rewind();

        return pixels;
    }
}

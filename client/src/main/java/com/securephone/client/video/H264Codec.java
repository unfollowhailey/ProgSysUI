package com.securephone.client.video;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class H264Codec {

    public byte[] encode(BufferedImage image) throws IOException {
        if (image == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    public BufferedImage decode(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        return ImageIO.read(in);
    }
}

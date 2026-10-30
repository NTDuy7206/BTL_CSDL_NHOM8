package com.ntd.csdl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class RoomSamplePhotoService {
    private final JdbcTemplate jdbc;
    private static final String KEY = "room-sample-photo-20261030-v1";

    @Transactional
    public int seed() {
        jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES (?,false) ON DUPLICATE KEY UPDATE seed_key=seed_key", KEY);
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key=? FOR UPDATE", Boolean.class, KEY))) return 0;
        byte[] original, thumbnail;
        try (var input = new ClassPathResource("sample-rooms/studio.png").getInputStream()) {
            var image = ImageIO.read(input);
            if (image == null) throw new IOException("Invalid sample room image");
            original = jpeg(image, 1200, 800);
            thumbnail = jpeg(image, 640, 480);
        } catch (IOException e) { throw new IllegalStateException("Cannot load sample room image", e); }
        int count = 0;
        for (var room : jdbc.queryForList("SELECT r.room_id FROM rooms r JOIN room_landlords l ON l.room_id=r.room_id ORDER BY r.room_id")) {
            String id = room.get("room_id").toString();
            jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE", id);
            if (jdbc.queryForObject("SELECT COUNT(*) FROM room_photos WHERE room_id=?", Integer.class, id) != 0) continue;
            String photoId = "demo-" + UUID.randomUUID().toString().replace("-", "").substring(0,31);
            count += jdbc.update("INSERT INTO room_photos(id,room_id,mime_type,image_data,thumbnail_data) VALUES (?,?,?,?,?)", photoId,id,"image/jpeg",original,thumbnail);
        }
        jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key=?", KEY);
        return count;
    }

    private static byte[] jpeg(BufferedImage source, int width, int height) throws IOException {
        double scale = Math.min(1, Math.min((double)width/source.getWidth(), (double)height/source.getHeight()));
        var image = new BufferedImage(Math.max(1,(int)(source.getWidth()*scale)),Math.max(1,(int)(source.getHeight()*scale)),BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE); graphics.fillRect(0,0,image.getWidth(),image.getHeight());
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source,0,0,image.getWidth(),image.getHeight(),null);
        } finally { graphics.dispose(); }
        var output = new ByteArrayOutputStream();
        if (!ImageIO.write(image,"jpg",output)) throw new IOException("JPEG encoder unavailable");
        return output.toByteArray();
    }
}

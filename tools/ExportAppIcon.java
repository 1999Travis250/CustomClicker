import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/** Exports the existing sidebar artwork using the application's real font and CSS. */
public class ExportAppIcon {
    private static final int[] SIZES = {16, 20, 24, 32, 40, 48, 64, 128, 256};

    // Starts JavaFX only for rendering; no application window or user settings are loaded.
    public static void main(String[] args) throws Exception {
        Path resources = Path.of("src/main/resources").toAbsolutePath();
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> {
            try {
                export(resources);
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                finished.countDown();
            }
        });
        finished.await();
        Platform.exit();
        if (failure.get() != null) {
            throw new IllegalStateException("Unable to export application icons.", failure.get());
        }
    }

    // Renders each resolution independently so small Windows icons retain sharp edges.
    private static void export(Path resources) throws Exception {
        Path output = resources.resolve("icons");
        Files.createDirectories(output);

        FontIcon pointer = new FontIcon("fth-mouse-pointer");
        pointer.setIconSize(52);
        pointer.setIconColor(Color.WHITE);
        StackPane artwork = new StackPane(pointer);
        artwork.getStyleClass().add("about-app-icon");
        artwork.setStyle("-fx-accent: #16e889;");
        Scene scene = new Scene(artwork, 105, 105, Color.TRANSPARENT);
        scene.getStylesheets().add(resources.resolve("css/styles.css").toUri().toString());
        artwork.applyCss();
        artwork.resize(105, 105);
        artwork.layout();

        List<byte[]> frames = new ArrayList<>();
        for (int size : SIZES) {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            // Include the existing shadow inside transparent padding around the tile.
            double scale = size / 125.0;
            parameters.setTransform(new Scale(scale, scale));
            parameters.setViewport(new Rectangle2D(-10 * scale, -10 * scale, size, size));
            WritableImage snapshot = artwork.snapshot(parameters, new WritableImage(size, size));
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
                }
            }
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(image, "png", png);
            frames.add(png.toByteArray());
            Files.write(output.resolve("customclicker-" + size + ".png"), png.toByteArray());
        }
        writeWindowsIcon(output.resolve("customclicker.ico"), frames);
        System.out.println("Exported " + SIZES.length + " PNG sizes and customclicker.ico to " + output);
    }

    // Packages PNG frames in the Windows ICO directory format (256 is encoded as zero).
    private static void writeWindowsIcon(Path file, List<byte[]> frames) throws Exception {
        int offset = 6 + 16 * frames.size();
        int length = offset + frames.stream().mapToInt(frame -> frame.length).sum();
        ByteBuffer icon = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        icon.putShort((short) 0).putShort((short) 1).putShort((short) frames.size());
        for (int index = 0; index < frames.size(); index++) {
            int size = SIZES[index];
            byte[] frame = frames.get(index);
            icon.put((byte) size).put((byte) size).put((byte) 0).put((byte) 0);
            icon.putShort((short) 1).putShort((short) 32);
            icon.putInt(frame.length).putInt(offset);
            offset += frame.length;
        }
        frames.forEach(icon::put);
        Files.write(file, icon.array());
    }
}

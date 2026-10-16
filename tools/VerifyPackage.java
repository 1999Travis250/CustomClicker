import com.github.kwhat.jnativehook.GlobalScreen;
import com.travis.customclicker.App;
import com.travis.customclicker.controller.MainController;
import com.travis.customclicker.service.GlobalHotkeyService;
import com.travis.customclicker.service.TrayService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/** Checks the packaged runtime and resources without showing a window or generating input. */
public final class VerifyPackage {

    // Run with APPDATA pointing to a disposable directory, never the user's real settings.
    public static void main(String[] args) throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> {
            MainController controller = null;
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/views/main-view.fxml"));
                Parent content = loader.load();
                controller = loader.getController();
                Stage stage = new Stage();
                Group root = new Group(content);
                stage.setScene(new Scene(root, 1200, 800, Color.TRANSPARENT));
                controller.applyInitialUiScale();
                controller.applyInitialAlwaysOnTop();
                Method windowIcons = App.class.getDeclaredMethod("setWindowIcons", Stage.class);
                windowIcons.setAccessible(true);
                windowIcons.invoke(new App(), stage);
                if (stage.getIcons().size() != 4) {
                    throw new IllegalStateException("Packaged window icons are missing.");
                }
                if (!GlobalScreen.isNativeHookRegistered()) {
                    throw new IllegalStateException("Packaged native hotkey library did not start.");
                }
                Field engine = MainController.class.getDeclaredField("autoClickService");
                engine.setAccessible(true);
                if (engine.get(controller) == null) {
                    throw new IllegalStateException("Packaged mouse engine did not initialize.");
                }
                Method trayImage = TrayService.class.getDeclaredMethod("createTrayImage");
                trayImage.setAccessible(true);
                Image image = (Image) trayImage.invoke(new TrayService());
                if (image.getWidth(null) <= 0) {
                    throw new IllegalStateException("Packaged tray artwork did not load.");
                }
                root.applyCss();
                root.layout();
                WritableImage snapshot = root.snapshot(new SnapshotParameters(), new WritableImage(1200, 800));
                BufferedImage preview = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < 800; y++) {
                    for (int x = 0; x < 1200; x++) {
                        preview.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
                    }
                }
                ImageIO.write(preview, "png", Path.of(args[0]).toFile());
                stage.close();
                System.out.println("PASS: packaged FXML, CSS, icon font, window/tray images, mouse engine, and native hook loaded.");
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                try {
                    if (controller != null) {
                        Field hotkeys = MainController.class.getDeclaredField("globalHotkeyService");
                        hotkeys.setAccessible(true);
                        GlobalHotkeyService service = (GlobalHotkeyService) hotkeys.get(controller);
                        if (service != null) { service.stop(); }
                    }
                } catch (Throwable error) {
                    failure.compareAndSet(null, error);
                }
                finished.countDown();
            }
        });
        finished.await();
        Platform.exit();
        if (failure.get() != null) { throw new IllegalStateException("Packaged smoke test failed.", failure.get()); }
    }
}

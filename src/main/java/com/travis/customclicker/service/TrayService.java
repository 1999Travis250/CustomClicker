
package com.travis.customclicker.service;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BaseMultiResolutionImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class TrayService {

    private TrayIcon trayIcon;

    // Installs the existing tray menu and double-click action once.
    public void install(Runnable onOpen, Runnable onExit) throws AWTException {
        if (trayIcon != null) return;

        if (!SystemTray.isSupported())
            throw new AWTException("System tray is not supported.");

        PopupMenu menu = new PopupMenu();

        MenuItem openItem = new MenuItem("Open CustomClicker");
        openItem.addActionListener(event -> onOpen.run());

        MenuItem exitItem = new MenuItem("Exit CustomClicker");
        exitItem.addActionListener(event -> onExit.run());

        menu.add(openItem);
        menu.addSeparator();
        menu.add(exitItem);

        TrayIcon icon = new TrayIcon(createTrayImage(), "CustomClicker", menu);
        icon.setImageAutoSize(true);

        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2
                        && event.getButton() == MouseEvent.BUTTON1)
                    onOpen.run();
            }
        });

        SystemTray.getSystemTray().add(icon);
        trayIcon = icon;
    }

    // Uses separately rendered sizes to keep the shared artwork sharp at different Windows scales.
    private Image createTrayImage() throws AWTException {
        int[] sizes = {16, 20, 24, 32, 40, 48, 64, 128, 256};
        Image[] images = new Image[sizes.length];
        try {
            for (int index = 0; index < sizes.length; index++) {
                String path = "/icons/customclicker-" + sizes[index] + ".png";
                URL resource = TrayService.class.getResource(path);
                if (resource == null) {
                    throw new IOException("Missing icon: " + path);
                }
                images[index] = ImageIO.read(resource);
                if (images[index] == null) {
                    throw new IOException("Unable to decode icon: " + path);
                }
            }
        } catch (IOException exception) {
            AWTException trayException = new AWTException("Unable to load the system tray icon.");
            trayException.initCause(exception);
            throw trayException;
        }
        return new BaseMultiResolutionImage(images);
    }

    // Reports whether this service has installed a tray icon.
    public boolean isInstalled() {
        return trayIcon != null;
    }

    // Removes the installed icon, if present.
    public void remove() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }
}

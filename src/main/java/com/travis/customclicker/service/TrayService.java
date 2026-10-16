
package com.travis.customclicker.service;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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

    // Draws the existing tray artwork and always releases its graphics context.
    private BufferedImage createTrayImage() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(25, 185, 115));
            graphics.fillRoundRect(0, 0, 32, 32, 8, 8);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("SansSerif", Font.BOLD, 22));
            graphics.drawString("C", 7, 24);
        } finally {
            graphics.dispose();
        }
        return image;
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

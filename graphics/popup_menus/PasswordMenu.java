package graphics.popup_menus;

import graphics.GraphicsSettings;
import users.User;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.function.Function;

public class PasswordMenu extends JPopupMenu {

    public PasswordMenu(User user, JFrame parent, Function<Void, Void> updater) {
        JMenuItem item = new JMenuItem("Сменить");
        item.setFont(GraphicsSettings.popupMenuFont);
        item.addActionListener(evt -> {
            String newPassword;
            try {
                newPassword = JOptionPane.showInputDialog(parent, "Введите новый пароль",
                        "Новый пароль", JOptionPane.PLAIN_MESSAGE);
            } catch (RuntimeException e) {
                return;
            }
            user.setPassword(newPassword);
        });
        add(item);
        item = new JMenuItem("Копировать");
        item.setFont(GraphicsSettings.popupMenuFont);
        item.addActionListener(evt -> {
            Clipboard clip = parent.getToolkit().getSystemClipboard();
            clip.setContents(new StringSelection(user.getPassword()), null);
        });
        add(item);
    }
}

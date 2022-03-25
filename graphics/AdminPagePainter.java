package graphics;

import graphics.popup_menus.LoginMenu;
import graphics.popup_menus.PasswordMenu;
import users.Session;
import users.User;
import users.UserSystem;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

class AdminPagePainter implements Painter {

    JFrame page;
    JPanel contentPanel;

    public AdminPagePainter() {
    }

    @Override
    public void paint(JFrame page, User user) {
        if (user.getType() != User.Type.ADMIN) {
            throw new IllegalArgumentException("У вас недостаточно прав для этого");
        }
        this.page = page;
        page.setLayout(new BorderLayout());
        JLabel label = new JLabel("Средства администрирования");
        label.setFont(GraphicsSettings.titleFont);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        page.add(label, BorderLayout.NORTH);
        contentPanel = new JPanel();
        updater = v -> {
            contentPanel.revalidate();
            contentPanel.remove(0);
            contentPanel.add(createNew());
            return v;
        };
        contentPanel.add(createNew());
        page.add(contentPanel, BorderLayout.CENTER);
        JPanel down = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton update = new JButton("Обновить");
        update.setFont(GraphicsSettings.userFont);
        update.addActionListener(evt -> updater.apply(null));
        update.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        down.add(update);
        page.add(down, BorderLayout.SOUTH);
    }

    private JComponent createNew() {
        String[] names = UserSystem.getInstance().getUserNames();
        JPanel panel = new JPanel(new GridLayout(names.length + 1, 4));
        JLabel title = new JLabel("Логин");
        title.setFont(GraphicsSettings.userFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        title = new JLabel("Тип");
        title.setFont(GraphicsSettings.userFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        title = new JLabel("Пароль");
        title.setFont(GraphicsSettings.userFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        title = new JLabel("Последнее обновление");
        title.setFont(GraphicsSettings.userFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        for (String s : names) {
            User target = UserSystem.getInstance().privateAccess(s);
            JLabel name = new JLabel(target.getName());
            name.setFont(GraphicsSettings.userFont);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(name);
            JLabel type = new JLabel(target.getType().getRussianName());
            type.setFont(GraphicsSettings.userFont);
            type.setHorizontalAlignment(SwingConstants.CENTER);
            type.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(type);
            JPasswordField password = new JPasswordField(target.getPassword());
            password.setFont(GraphicsSettings.userFont);
            password.setHorizontalAlignment(SwingConstants.CENTER);
            password.setBorder(new LineBorder(Color.BLACK, 1));
            password.setEditable(false);
            panel.add(password);
            JLabel lastMod = new JLabel(SimpleDateFormat.getDateTimeInstance().format(
                    new Date(target.getLastMod())));
            lastMod.setFont(GraphicsSettings.userFont);
            lastMod.setHorizontalAlignment(SwingConstants.CENTER);
            lastMod.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(lastMod);
            if (target == Session.currentSession().getUser() || target.getType().getPermLevel() != 3) {
                name.setComponentPopupMenu(new LoginMenu(target, page, updater));
                password.setComponentPopupMenu(new PasswordMenu(target, page, updater));
            }
        }
        JPanel parent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        parent.add(panel);
        return new JScrollPane(parent);
    }

    private Function<Void, Void> updater;
}

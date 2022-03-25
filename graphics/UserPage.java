package graphics;

import users.User;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserPage extends JFrame {

    private final User user;
    private final Painter painter;

    public UserPage(User user) {
        super(MainMenu.APP_NAME + " - " + user.getName());
        this.user = user;
        setSize(getToolkit().getScreenSize());
        setLocationRelativeTo(null);
        if (user.getType() != User.Type.ADMIN) {
            setIconImage(Assets.USER_PAGE_ICON);
        } else {
            setIconImage(Assets.ADMIN_PAGE_ICON);
        }
        switch (user.getType()) {
            case QUEST -> painter = new QuestPagePainter();
            case PUPIL -> painter = new PupilPagePainter();
            case TEACHER -> painter = new TeacherPagePainter();
            case ADMIN -> painter = new AdminPagePainter();
            default -> painter = null;
        }
        if (painter == null) {
            throw new RuntimeException("Неизвестный тип пользователя");
        }
        if (user.getType() != User.Type.QUEST) {
            setJMenuBar(createMenu());
        }
        painter.paint(this, user);
    }

    private JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu menu = new JMenu("Тесты");
        menu.setFont(GraphicsSettings.menuFont);
        JMenuItem item = new JMenuItem("Мои тесты");
        item.setFont(GraphicsSettings.menuItemFont);
        item.setIcon(new ImageIcon(Assets.MY_TESTS_MENU_ICON));
        item.addActionListener(evt -> {
            UserTests ut = new UserTests(user);
            ut.setSize(getSize());
            ut.setLocationRelativeTo(this);
            ut.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(true);
                }
            });
            ut.setVisible(true);
            setVisible(false);
        });
        menu.add(item);
        bar.add(menu);

        menu = new JMenu("Профиль");
        menu.setFont(GraphicsSettings.menuFont);
        item = new JMenuItem("Сменить логин");
        item.setFont(GraphicsSettings.menuItemFont);
        item.addActionListener(evt -> {
            OptionPanels.renameUser(this);
            update();
            setTitle(MainMenu.APP_NAME + " - " + user.getName());
        });
        menu.add(item);
        item = new JMenuItem("Изменить пароль");
        item.setFont(GraphicsSettings.menuItemFont);
        item.addActionListener(evt -> OptionPanels.changePassword(this));
        menu.add(item);
        JCheckBoxMenuItem bItem = new JCheckBoxMenuItem("Сохранять пароль");
        bItem.setFont(GraphicsSettings.menuItemFont);
        bItem.setSelected(user.isSaveInSystem());
        bItem.addActionListener(evt -> user.setSaveInSystem(bItem.isSelected()));
        menu.add(bItem);
        bar.add(menu);

        return bar;
    }

    public void update() {
        removeAll();
        setJMenuBar(createMenu());
        painter.paint(this, user);
    }
}

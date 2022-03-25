package graphics.popup_menus;

import graphics.Assets;
import graphics.GraphicsSettings;
import graphics.UserPage;
import users.Pupil;
import users.Session;
import users.Teacher;
import users.User;
import users.Class;
import users.ClassManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class LoginMenu extends JPopupMenu {

    private JTextField postfix;

    public LoginMenu(User user, JFrame parent, Function<Void, Void> updater) {
        JMenuItem item = new JMenuItem("Просмотр");
        item.setFont(GraphicsSettings.popupMenuFont);
        item.addActionListener(evt -> {
            UserPage page = new UserPage(user);
            Session session = Session.currentSession();
            if (session != null) {
                session.adminView(page);
            }
            page.addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    parent.setVisible(false);
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    parent.setVisible(true);
                }
            });
            page.setVisible(true);
        });
        add(item);
        item = new JMenuItem("Переименовать");
        item.setFont(GraphicsSettings.popupMenuFont);
        item.addActionListener(evt -> {
            String newName = null;
            try {
                try {
                    newName = JOptionPane.showInputDialog(parent, "Введите новый логин",
                            "Переименовать", JOptionPane.PLAIN_MESSAGE);
                } catch (RuntimeException e) {
                    //ignore
                }
            } catch (RuntimeException e) {
                return;
            }
            if (newName != null) {
                user.setName(newName);
                updater.apply(null);
            }
        });
        add(item);
        if (user.getType() == User.Type.PUPIL) {
            item = new JMenuItem("Изменить класс");
            item.setFont(GraphicsSettings.popupMenuFont);
            item.addActionListener(evt -> {
                JPanel message = new JPanel(new FlowLayout(FlowLayout.CENTER));
                Class cl;
                try {
                    cl = ClassManager.getForName(((Pupil) user).getClassName());
                } catch (NoSuchElementException e) {
                    cl = null;
                }
                int value = cl != null ? cl.getNumber() : 1;
                String letter = (cl != null ? cl.getPostfix() : "") + "";
                SpinnerNumberModel model = new SpinnerNumberModel(value, 1, 11, 1);
                JSpinner choose = new JSpinner();
                choose.setModel(model);
                choose.setFont(GraphicsSettings.popupMenuFont);
                message.add(choose);
                postfix = new JTextField() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (postfix.getText().equals("")) {
                            g.setColor(GraphicsSettings.hintColor);
                            g.drawString("Буква", 4, 19);
                        }
                    }
                };
                postfix.setPreferredSize(new Dimension(100, 30));
                postfix.setFont(GraphicsSettings.popupMenuFont);
                postfix.setText(letter);
                message.add(postfix);
                int r = JOptionPane.showConfirmDialog(parent, message, "Укажите новый класс",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (r == JOptionPane.OK_OPTION) {
                    String className = choose.getValue() + " \"" + postfix.getText() + '"';
                    Class clazz = ClassManager.getForName(className);
                    clazz.addPupil((Pupil) user);
                }
            });
            add(item);
        }
        if (user.getType() == User.Type.TEACHER) {
            item = new JMenuItem("Изменить классы");
            item.setFont(GraphicsSettings.menuItemFont);
            item.addActionListener(evt -> {
                JPanel panel = new JPanel(new BorderLayout());

                JList<String> classes = new JList<>();
                DefaultListModel<String> model = new DefaultListModel<>();
                ((Teacher) user).forEachClassName(model::addElement);
                classes.setModel(model);
                classes.setDragEnabled(false);
                panel.add(new JScrollPane(classes), BorderLayout.CENTER);

                JPanel down = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton button = new JButton("Добавить");
                button.setFont(GraphicsSettings.menuItemFont);
                button.addActionListener(clEvt -> {
                    if (Session.currentSession() == null) {
                        return;
                    }
                    String str;
                    try {
                        str = JOptionPane.showInputDialog(Session.currentSession().getWindow(),
                                "Напишите название класса", "Добавить класс", JOptionPane.PLAIN_MESSAGE);
                    } catch (RuntimeException e) {
                        return;
                    }
                    if (ClassManager.exists(str)) {
                        model.addElement(str);
                        classes.repaint();
                    } else {
                        JOptionPane.showMessageDialog(Session.currentSession().getWindow(),
                                "Такого класса не существует", "Ошибка", JOptionPane.PLAIN_MESSAGE);
                    }
                });
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                down.add(button);
                button = new JButton("Удалить");
                button.setFont(GraphicsSettings.menuItemFont);
                button.addActionListener(clEvt -> {
                    if (!classes.isSelectionEmpty()) {
                        model.remove(classes.getSelectedIndex());
                    }
                });
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                down.add(button);
                panel.add(down, BorderLayout.SOUTH);
                int r = JOptionPane.showConfirmDialog(Session.currentSession().getWindow(), panel,
                        "Классы", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (r == JOptionPane.OK_OPTION) {
                    Object[] copy = model.toArray();
                    String[] array = new String[copy.length];
                    System.arraycopy(copy, 0, array, 0, array.length);
                    ((Teacher) user).compareWith(List.of(array));
                }
            });
            add(item);
        }
        item = new JMenuItem("Удалить");
        item.setFont(GraphicsSettings.popupMenuFont);
        item.addActionListener(evt -> {
            int r = JOptionPane.showConfirmDialog(parent, "Вы действительно хотите" +
                    " удалить пользователя " + user.getName() + '?', "Предупреждение",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(
                    Assets.QUESTION_ICON));
            if (r == JOptionPane.OK_OPTION) {
                user.delete();
                updater.apply(null);
            }
        });
        add(item);
    }
}

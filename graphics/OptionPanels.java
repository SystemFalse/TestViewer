package graphics;

import test.Test;
import test.TestManager;
import users.*;
import users.Class;
import users.ClassManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class OptionPanels {

    private OptionPanels() {}

    private static JTextField login;
    private static JTextField password;
    private static JTextField postfix;

    public synchronized static User newUser(JFrame parent) {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEADING));
        login = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (login.getText().equals("")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Логин", 4, 19);
                }
            }
        };
        login.setFont(GraphicsSettings.registerFont);
        login.setPreferredSize(new Dimension(200, 30));
        form.add(login);
        password = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (password.getText().equals("")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Пароль", 4, 19);
                }
            }
        };
        password.setPreferredSize(new Dimension(200, 30));
        password.setFont(GraphicsSettings.registerFont);
        form.add(password);
        JComboBox<String> tp = new JComboBox<>(new String[]{"Ученик", "Учитель", "Администратор"});
        tp.setFont(GraphicsSettings.registerFont);
        tp.setPreferredSize(new Dimension(200, 30));
        form.add(tp);
        int r = JOptionPane.showConfirmDialog(parent, form, "Новый пользователь",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            String strType = (String) tp.getSelectedItem();
            //noinspection ConstantConditions
            User.Type type = switch (strType) {
                case "Ученик" -> User.Type.PUPIL;
                case "Учитель" -> User.Type.TEACHER;
                default -> User.Type.ADMIN;
            };
            String name = login.getText();
            String pw = password.getText();
            int save = JOptionPane.showConfirmDialog(parent, "Сохранить ваш пароль в системе?",
                    "Новый пользователь", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(Assets.QUESTION_ICON));
            boolean saveInSystem = save == JOptionPane.OK_OPTION;
            User result;
            try {
                result = switch (type) {
                    case PUPIL -> Pupil.getInstance(name, pw, saveInSystem);
                    case TEACHER -> Teacher.getInstance(name, pw, saveInSystem);
                    case ADMIN -> Administrator.getInstance(name, pw, saveInSystem);
                    default -> null;
                };
            } catch (RuntimeException e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                return null;
            }
            if (type == User.Type.PUPIL) {
                JPanel message = new JPanel(new FlowLayout(FlowLayout.CENTER));
                SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 11, 1);
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
                message.add(postfix);
                r = JOptionPane.showConfirmDialog(parent, message, "Укажите свой класс",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (r == JOptionPane.OK_OPTION) {
                    String cl = choose.getValue() + " \"" + postfix.getText() + '"';
                    Class clazz = ClassManager.getForName(cl);
                    clazz.addPupil((Pupil) result);
                }
            }
            return result;
        } else return null;
    }

    public static boolean createUser(User user, JFrame parent) {
        int c = JOptionPane.showConfirmDialog(parent, "Создать нового пользователя?\n" + format(user),
                "Новый пользователь", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(Assets.QUESTION_ICON));
        if (c == JOptionPane.OK_OPTION) {
            UserSystem.getInstance().regin(user);
            return true;
        } else return false;
    }

    private static String format(User user) {
        return String.format("Тип записи: %s\nЛогин: %s\nПароль: %s\nСохранить пароль: %s",
                user.getType().getRussianName(), user.getName(), user.getPassword(),
                user.isSaveInSystem() ? "да" : "нет");
    }

    public static void userDelete(User user) {
        JOptionPane.showMessageDialog(null,
                String.format("Пользователь \"%s\" был удалён", user.getName()),
                "Информация", JOptionPane.PLAIN_MESSAGE);
    }

    public static void showTestResults(Test test, int[] results, JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(results.length + 1, 3));
        JLabel title = new JLabel("Вопрос");
        title.setFont(GraphicsSettings.statisticFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        title = new JLabel("Полученные баллы");
        title.setFont(GraphicsSettings.statisticFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        title = new JLabel("Максимальные баллы баллы");
        title.setFont(GraphicsSettings.statisticFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(title);
        for (int i = 0; i < results.length; i++) {
            JLabel label = new JLabel((i + 1) + "");
            label.setFont(GraphicsSettings.statisticFont);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);
            label = new JLabel(results[i] + "");
            label.setFont(GraphicsSettings.statisticFont);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);
            label = new JLabel(test.get(i).getMaxPoints() + "");
            label.setFont(GraphicsSettings.statisticFont);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);
        }
        JOptionPane.showMessageDialog(parent, new JScrollPane(panel), "Статистика", JOptionPane.PLAIN_MESSAGE);
    }

    public static void showFullTestResults(Test test, JFrame parent) {
        TestInfo info;
        if (TestManager.hasInformation(test.getTitle())) {
            info = TestManager.getInfoByTitle(test.getTitle());
        } else {
            JOptionPane.showMessageDialog(parent, "Ещё никто не прошёл данный тест",
                    "Статистика", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        JPanel table = new JPanel(new GridLayout(info.getNumberOfPassed() + 1, test.questions() + 2));
        JPanel empty = new JPanel();
        empty.setBorder(new LineBorder(Color.BLACK));
        table.add(empty);
        for (int i = 0 ; i < test.questions(); i++) {
            JLabel q = new JLabel("№" + (i + 1));
            q.setBorder(new LineBorder(Color.BLACK));
            q.setFont(GraphicsSettings.statisticFont);
            table.add(q);
        }
        JLabel total = new JLabel("Всего");
        total.setFont(GraphicsSettings.statisticFont.deriveFont(Font.BOLD));
        total.setBorder(new LineBorder(Color.BLACK));
        table.add(total);
        String[] passedUsers = info.getPassedUsers();
        for (String user : passedUsers) {
            int[] results = info.getResultsOf(user);
            JLabel name = new JLabel(user);
            name.setFont(GraphicsSettings.statisticFont);
            name.setBorder(new LineBorder(Color.BLACK));
            table.add(name);
            for (int result : results) {
                JLabel label = new JLabel(result + "");
                label.setFont(GraphicsSettings.statisticFont);
                label.setBorder(new LineBorder(Color.BLACK));
                table.add(label);
            }
            int totalScore = info.getTotalScore(user);
            JLabel label = new JLabel(totalScore + "");
            label.setFont(GraphicsSettings.statisticFont.deriveFont(Font.BOLD));
            label.setBorder(new LineBorder(Color.BLACK));
            table.add(label);
        }
        JOptionPane.showMessageDialog(parent, table, "Статистика", JOptionPane.PLAIN_MESSAGE);
    }

    public static void renameUser(JFrame parent) {
        Session session = Session.currentSession();
        if (session != null) {
            User curUser = session.getUser();
            String newName;
            try {
                newName = JOptionPane.showInputDialog(parent, "Введите новый логин",
                        "Смена логина", JOptionPane.PLAIN_MESSAGE);
            } catch (RuntimeException e) {
                return;
            }
            if (UserSystem.getInstance().hasName(newName)) {
                JOptionPane.showMessageDialog(parent, "Этот логин уже занят",
                        "Предупреждение", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            if (curUser.getType().getPermLevel() == 1) {
                String password;
                try {
                    password = JOptionPane.showInputDialog(parent, "У вас недостаточно прав для" +
                            " самостоятельного совершения это операции.\n" +
                            "Введите пароль учителя или администратора, чтобы продолжить",
                            "Предупреждение", JOptionPane.PLAIN_MESSAGE);
                } catch (RuntimeException e) {
                    return;
                }
                if (UserSystem.getInstance().hasAccess((Pupil) curUser, password)) {
                    try {
                        curUser.setName0(newName);
                    } catch (RuntimeException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                e);
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, "Пароль неверный",
                            "Предупреждение", JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
    }

    public static void changePassword(JFrame parent) {
        Session session = Session.currentSession();
        if (session != null) {
            User curUser = session.getUser();
            String newPassword;
            try {
                newPassword = JOptionPane.showInputDialog(parent, "Введите новый пароль",
                        "Смена пароля", JOptionPane.PLAIN_MESSAGE);
            } catch (RuntimeException e) {
                return;
            }
            curUser.setPassword(newPassword);
        }
    }
}

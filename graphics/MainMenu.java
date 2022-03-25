package graphics;

import test.Test;
import test.TestFrame;
import test.TestManager;
import users.Quest;
import users.Session;
import users.User;
import users.UserSystem;
import users.ClassManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;

import java.awt.event.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MainMenu extends JFrame {

    public static final String APP_NAME = "TestViewer";

    private static final FileFilter tests = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName(), extension;
            if (name.contains(".")) {
                extension = name.substring(name.lastIndexOf('.'));
            } else {
                extension = "";
            }
            return extension.equals(".test") || extension.equals(".json");
        }

        @Override
        public String getDescription() {
            return "Файлы тестов (*.test *.json)";
        }
    };
    private static final FileFilter allFiles = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName(), extension;
            if (name.contains(".")) {
                extension = name.substring(name.lastIndexOf('.'));
            } else {
                extension = "";
            }
            return extension.equals(".test") || extension.equals(".json") || extension.equals(".info");
        }

        @Override
        public String getDescription() {
            return "Все поддерживаемые форматы";
        }
    };
    private static final FileFilter users = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            return name.equals("users.bin");
        }

        @Override
        public String getDescription() {
            return "Файлы пользователей (users.bin)";
        }
    };

    private final JTextField login;
    private final JPasswordField password;

    private final JFileChooser fc;

    public MainMenu() {
        super(APP_NAME + "     ೱ");
        setSize(720, 480);
        setLocationRelativeTo(null);
        setIconImage(Assets.MAIN_ICON);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel label = new JLabel("Вход");
        label.setFont(GraphicsSettings.labelFont);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(710, 40));
        add(label);

        login = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (Objects.equals(login.getText(), "")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Логин", 4, 26);
                }
            }
        };
        password = new JPasswordField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (Objects.equals(new String(password.getPassword()), "")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Пароль", 4, 26);
                }
            }
        };
        login.setFont(GraphicsSettings.inputFont);
        login.setPreferredSize(new Dimension(400, 40));
        login.setEditable(true);
        add(login);

        password.setPreferredSize(new Dimension(400, 40));
        password.setFont(GraphicsSettings.inputFont);
        add(password);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton enter = new JButton("Войти");
        enter.setFont(GraphicsSettings.startButtonFont);
        enter.addActionListener(evt -> login());
        enter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(enter);
        panel.setPreferredSize(new Dimension(400, 60));
        add(panel);

        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(710, 209));
        JButton register = new JButton("Зарегистрироваться");
        register.setFont(GraphicsSettings.registerFont);
        register.addActionListener(evt -> regin());
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel1.add(register);
        panel.add(panel1, BorderLayout.SOUTH);
        add(panel);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                UserSystem.writeFile();
                ClassManager.writeFile();
                TestManager.writeFiles();
            }
        });
        setJMenuBar(createMenus());
        setResizable(false);

        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileHidingEnabled(true);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(allFiles);
    }

    private String lastSafe;

    private JMenuBar createMenus() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Файл");
        file.setFont(GraphicsSettings.menuFont);
        JMenuItem load = new JMenuItem("Загрузить тест");
        load.setFont(GraphicsSettings.menuItemFont);
        load.setIcon(new ImageIcon(Assets.LOAD_MENU_ICON));
        load.addActionListener(evt -> {
            fc.setDialogTitle("Выберете файл теста или информации о нём");
            int r = fc.showDialog(this, "Загрузить");
            if (r == JFileChooser.APPROVE_OPTION) {
                File choose = fc.getSelectedFile();
                int del = JOptionPane.showConfirmDialog(this,
                        "Удалить исходный файл после добавления?", "", JOptionPane.YES_NO_OPTION);
                TestManager.readFile(choose.getAbsolutePath(),
                        del == JOptionPane.OK_OPTION, true, false);
            }
        });
        file.add(load);
        JMenuItem testsPackage = new JMenuItem("Папка с тестами");
        testsPackage.setFont(GraphicsSettings.menuItemFont);
        testsPackage.setIcon(new ImageIcon(Assets.PACKAGE_MENU_ICON));
        testsPackage.addActionListener(evt -> {
            File tests = new File("tests");
            if (tests.exists() && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(tests);
                } catch (IOException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Не удалось открыть папку с тестами", e));
                }
            }
        });
        file.add(testsPackage);
        JMenuItem preview = new JMenuItem("Предпросмотр");
        preview.setFont(GraphicsSettings.menuItemFont);
        preview.setIcon(new ImageIcon(Assets.PREVIEW_MENU_ICON));
        preview.addActionListener(evt -> {
            fc.setDialogTitle("Выберете файл теста, который хотите просмотреть без загрузки");
            fc.setFileFilter(tests);
            int r = fc.showDialog(this, "Предпросмотр");
            fc.setFileFilter(allFiles);
            if (r == JFileChooser.APPROVE_OPTION) {
                File choose = fc.getSelectedFile();
                Test test;
                try {
                    test = TestManager.readTest(choose);
                } catch (RuntimeException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            e);
                    return;
                }
                if (TestManager.isLoaded(test)) {
                    if (!choose.getAbsolutePath().equals(lastSafe)) {
                        String safe = JOptionPane.showInputDialog(this,
                                "Введите пароль автора теста или администратора, чтобы продолжить",
                                "Вы пытаетесь просмотреть уже загруженный тест", JOptionPane.PLAIN_MESSAGE);
                        if (UserSystem.getInstance().hasAccess(test, safe)) {
                            lastSafe = choose.getAbsolutePath();
                        } else {
                            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                    new RuntimeException("У вас нет прав на эту операцию"));
                            return;
                        }
                    }
                }
                r = JOptionPane.showConfirmDialog(this, "Показывать верные варианты ответов?",
                        "Вопрос", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        new ImageIcon(Assets.QUESTION_ICON));
                TestFrame frame = new TestFrame(test, r == JOptionPane.OK_OPTION, true);
                frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        setVisible(true);
                    }
                });
                frame.setSize(1200, 800);
                frame.setLocationRelativeTo(this);
                frame.setVisible(true);
                setVisible(false);
            }
        });
        file.add(preview);
        JMenuItem compareUsers = new JMenuItem("Совместить пользователей");
        compareUsers.setFont(GraphicsSettings.menuItemFont);
        compareUsers.setIcon(new ImageIcon(Assets.COMPARE_USERS_MENU_ICON));
        compareUsers.addActionListener(evt -> {
            fc.setDialogTitle("Выберете файл с пользователями");
            fc.setFileFilter(users);
            int r = fc.showDialog(this, "Совместить");
            fc.setFileFilter(allFiles);
            if (r == JFileChooser.APPROVE_OPTION) {
                File choose = fc.getSelectedFile();
                try {
                    UserSystem.compareWith(choose);
                } catch (RuntimeException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException(e));
                }
            }
        });
        file.add(compareUsers);
        bar.add(file);

        JMenu about = new JMenu("О программе");
        about.setFont(GraphicsSettings.menuFont);
        JMenuItem help = new JMenuItem("Помощь");
        help.setFont(GraphicsSettings.menuItemFont);
        help.setIcon(new ImageIcon(Assets.HELP_MENU_ICON));
        help.addActionListener(evt -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    InputStream is = getClass().getClassLoader().getResourceAsStream("help/instruction.html");
                    File out = File.createTempFile("instruction", ".html");
                    out.deleteOnExit();
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        is.transferTo(fos);
                    }
                    desktop.open(out);
                } catch (IOException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Не удалось открыть инструкцию", e));
                }
            }
        });
        about.add(help);
        JMenuItem creating = new JMenuItem("Как делать тесты");
        creating.setFont(GraphicsSettings.menuItemFont);
        creating.setIcon(new ImageIcon(Assets.CREATING_MENU_ICON));
        creating.addActionListener(evt -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    InputStream is = getClass().getClassLoader().getResourceAsStream("help/creating_test.txt");
                    File out = File.createTempFile("creating_tests", ".txt");
                    out.deleteOnExit();
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        is.transferTo(fos);
                    }
                    desktop.open(out);
                } catch (IOException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Не удалось открыть инструкцию", e));
                }
            }
        });
        about.add(creating);
        JMenuItem author = new JMenuItem("Создатель");
        author.setFont(GraphicsSettings.menuItemFont);
        author.setIcon(new ImageIcon(Assets.CREATOR_MENU_ICON));
        author.addActionListener(evt -> JOptionPane.showMessageDialog(this,
                "Создатель: Гудченко Антон, 10 класс 2022г.", "Информация", JOptionPane.PLAIN_MESSAGE));
        about.add(author);
        bar.add(about);

        return bar;
    }

    private void login() {
        String l = login.getText();
        String p = new String(password.getPassword());
        if (l.equals("") && p.equals("")) {
            Quest quest = new Quest();
            openSession(quest);
            return;
        }
        User user;
        try {
            if (p.equals("")) user = UserSystem.getInstance().saveAccess(l);
            else user = UserSystem.getInstance().access(l, p);
        } catch (RuntimeException e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            return;
        }
        openSession(user);
    }

    private synchronized void regin() {
        Thread thread = new Thread(() -> {
            User newUser = OptionPanels.newUser(this);
            if (newUser == null) {
                return;
            }
            if (OptionPanels.createUser(newUser, this)) {
                openSession(newUser);
            }
        }, "registration");
        thread.start();
    }

    private void openSession(User user) {
        Session session = new Session(user);
        UserPage page = session.getWindow();
        page.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                login.setText("");
                password.setText("");
                setVisible(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(true);
                session.terminate();
            }
        });
        session.start();
    }
}

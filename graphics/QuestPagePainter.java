package graphics;

import test.Test;
import test.TestFrame;
import test.TestManager;
import users.Quest;
import users.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QuestPagePainter implements Painter {

    private JFrame page;

    private JList<Test> available;
    private JTextField availableSearch;

    public QuestPagePainter() {

    }

    @Override
    public void paint(JFrame page, User user) {
        if (user.getType() != User.Type.QUEST) {
            throw new IllegalArgumentException("Неверный тип пользователя");
        }
        Quest quest = (Quest) user;
        this.page = page;
        page.setLayout(new BorderLayout());

        JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        JLabel title = new JLabel("Все тесты");
        title.setFont(GraphicsSettings.labelFont);
        upperPanel.add(title);
        availableSearch = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (availableSearch.getText().equals("")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Поиск", 4, 23);
                }
            }
        };
        availableSearch.setPreferredSize(new Dimension(200, 30));
        availableSearch.setFont(GraphicsSettings.labelFont);
        availableSearch.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                update();
            }
        });
        upperPanel.add(availableSearch);
        page.add(upperPanel, BorderLayout.NORTH);

        available = new JList<>();
        available.setFont(GraphicsSettings.testChooseFont);
        available.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        available.setOpaque(false);
        available.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    Test choose = available.getSelectedValue();
                    available.clearSelection();
                    openTest(choose);
                }
            }
        });
        available.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Test choose = available.getSelectedValue();
                    available.clearSelection();
                    openTest(choose);
                }
            }
        });
        page.add(new JScrollPane(available), BorderLayout.CENTER);
        update();
    }

    private void openTest(Test test) {
        TestFrame frame = new TestFrame(test, false, true);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                page.setVisible(true);
            }
        });
        frame.setSize(1200, 800);
        frame.setUpdater(this::update);
        frame.setLocationRelativeTo(page);
        frame.setVisible(true);
        page.setVisible(false);
    }

    private void update() {
        Runnable action = () -> {
            Test[] allTests = TestManager.getTestsByRule(test -> true);
            ArrayList<Test> tests = new ArrayList<>(Arrays.asList(allTests));
            tests = searchTest(tests, convertToPattern(availableSearch.getText().toLowerCase(Locale.ROOT)));
            DefaultListModel<Test> dlm1 = new DefaultListModel<>();
            dlm1.addAll(tests);
            available.setModel(dlm1);
        };
        Thread thread = new Thread(action, "search");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private String convertToPattern(String search) {
        StringBuilder sb = new StringBuilder();
        for (char l : search.toCharArray()) {
            if (l == '.' || l == '*' || l == '?' || l == '+' || l == '\\' || l == '('
                    || l == ')' || l == '[' || l == ']' || l == '$' || l == '^') {
                sb.append('\\');
            }
            sb.append(l);
        }
        return sb.toString();
    }

    private ArrayList<Test> searchTest(ArrayList<Test> source, String rule) {
        ArrayList<Test> result = new ArrayList<>();
        if (rule.isBlank()) {
            result.addAll(source);
            return result;
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(rule);
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
        for (Test test : source) {
            String title = test.getTitle().toLowerCase(Locale.ROOT);
            String author = test.getAuthor().toLowerCase(Locale.ROOT);
            Matcher m = pattern.matcher(title);
            if (m.find()) {
                result.add(test);
                continue;
            }
            m = pattern.matcher(author);
            if (m.find()) {
                result.add(test);
            }
        }
        return result;
    }
}

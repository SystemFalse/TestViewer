package graphics;

import test.Test;
import test.TestManager;
import users.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserTests extends JFrame {

    private final User user;

    private final JTextField testSearch;
    private final JList<Test> userTests;

    public UserTests(User user) {
        this.user = user;
        setTitle(MainMenu.APP_NAME + " - тесты " + user.getName());
        setLayout(new BorderLayout());

        JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        JLabel title = new JLabel("Ваши тесты");
        title.setFont(GraphicsSettings.labelFont);
        upperPanel.add(title);
        testSearch = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (testSearch.getText().equals("")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Поиск", 4, 23);
                }
            }
        };
        testSearch.setPreferredSize(new Dimension(200, 30));
        testSearch.setFont(GraphicsSettings.labelFont);
        testSearch.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                update();
            }
        });
        upperPanel.add(testSearch);
        add(upperPanel, BorderLayout.NORTH);

        userTests = new JList<>();
        userTests.setFont(GraphicsSettings.testChooseFont);
        userTests.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTests.setOpaque(false);
        userTests.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    Test choose = userTests.getSelectedValue();
                    userTests.clearSelection();
                    openStat(choose);
                }
            }
        });
        userTests.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Test choose = userTests.getSelectedValue();
                    userTests.clearSelection();
                    openStat(choose);
                }
            }
        });
        add(new JScrollPane(userTests), BorderLayout.CENTER);
        JPanel down = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton delete = new JButton("Удалить");
        delete.setFont(GraphicsSettings.popupMenuFont);
        delete.addActionListener(evt -> {
            if (!userTests.isSelectionEmpty()) {
                TestManager.deleteTest(userTests.getSelectedValue());
                update();
            }
        });
        down.add(delete);
        add(down, BorderLayout.SOUTH);
        update();
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
            return result;
        }
        for (Test test : source) {
            String title = test.getTitle().toLowerCase(Locale.ROOT);
            Matcher m = pattern.matcher(title);
            if (m.find()) {
                result.add(test);
            }
        }
        return result;
    }

    private void update() {
        Runnable action = () -> {
            Test[] allTests = TestManager.getTestsByRule(test -> test.getAuthor() != null && test.getAuthor().
                    equals(user.getName()));
            ArrayList<Test> tests = new ArrayList<>(Arrays.asList(allTests));
            tests = searchTest(tests, convertToPattern(testSearch.getText().toLowerCase(Locale.ROOT)));
            DefaultListModel<Test> dlm = new DefaultListModel<>();
            dlm.addAll(tests);
            userTests.setModel(dlm);
        };
        Thread thread = new Thread(action, "search");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void openStat(Test test) {
        OptionPanels.showFullTestResults(test, this);
    }
}

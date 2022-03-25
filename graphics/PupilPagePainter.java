package graphics;

import test.Test;
import test.TestFrame;
import test.TestManager;
import users.Pupil;
import users.TestInfo;
import users.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PupilPagePainter implements Painter {

    private record PassedTest(Test test, int lastResult, int maxResult, Date passedTime, boolean interrupted) {

        @Override
        public String toString() {
            DateFormat df = DateFormat.getDateTimeInstance();
            return test + " (" + lastResult + '/' + maxResult + "; " +
                    df.format(passedTime) + (interrupted ? "; был прерван" : "") + ')';
        }
    }

    private JFrame page;
    private JList<Test> available;
    private JList<PassedTest> passed;
    private Pupil pupil;

    private JTextField availableSearch;
    private JTextField passedSearch;

    public PupilPagePainter() {

    }

    @Override
    public void paint(JFrame page, User user) {
        if (user.getType() != User.Type.PUPIL) {
            throw new IllegalArgumentException("Неверный тип пользователя");
        }
        Pupil pupil = (Pupil) user;
        this.page = page;
        this.pupil = pupil;

        page.setLayout(new GridLayout(2, 1));
        JPanel up = new JPanel(new BorderLayout());

        JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        JLabel title = new JLabel("Непройденные тесты");
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
        up.add(upperPanel, BorderLayout.NORTH);

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
                    openTest(choose, false);
                }
            }
        });
        available.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Test choose = available.getSelectedValue();
                    available.clearSelection();
                    openTest(choose, e.isAltDown() && e.isControlDown() && e.isShiftDown());
                }
            }
        });
        up.add(new JScrollPane(available), BorderLayout.CENTER);
        page.add(up);

        JPanel center = new JPanel(new BorderLayout());
        upperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        title = new JLabel("Пройденные тесты");
        title.setFont(GraphicsSettings.labelFont);
        upperPanel.add(title);
        passedSearch = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (passedSearch.getText().equals("")) {
                    g.setColor(GraphicsSettings.hintColor);
                    g.drawString("Поиск", 4, 23);
                }
            }
        };
        passedSearch.setPreferredSize(new Dimension(200, 30));
        passedSearch.setFont(GraphicsSettings.labelFont);
        passedSearch.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                update();
            }
        });
        upperPanel.add(passedSearch);
        center.add(upperPanel, BorderLayout.NORTH);

        passed = new JList<>();
        passed.setFont(GraphicsSettings.testChooseFont);
        passed.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passed.setOpaque(false);
        passed.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    Test choose = passed.getSelectedValue().test;
                    passed.clearSelection();
                    openStat(choose);
                }
            }
        });
        passed.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Test choose = passed.getSelectedValue().test;
                    passed.clearSelection();
                    openStat(choose);
                }
            }
        });
        center.add(new JScrollPane(passed), BorderLayout.CENTER);
        page.add(center);
        update();
    }

    private void openTest(Test test, boolean showRight) {
        TestFrame frame = new TestFrame(test, showRight);
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

    private void openStat(Test test) {
        TestInfo info = TestManager.getInfoByTitle(test.getTitle());
        int[] results = info.getResultsOf(pupil.getName());
        OptionPanels.showTestResults(test, results, page);
    }

    private void update() {
        Runnable action = () -> {
            Test[] allTests = TestManager.getTestsByRule(test -> test.getClassName().equals(pupil.getClassName()));
            ArrayList<PassedTest> passedTests = new ArrayList<>();
            ArrayList<Test> otherTests = new ArrayList<>();
            for (Test test : allTests) {
                if (TestManager.hasInformation(test.getTitle())) {
                    TestInfo info = TestManager.getInfoByTitle(test.getTitle());
                    if (info.hasResultOf(pupil.getName())) {
                        passedTests.add(new PassedTest(test, info.getTotalScore(pupil.getName()),
                                test.getMaxPoints(), info.getDateOf(pupil.getName()),
                                info.getInterrupted(pupil.getName())));
                        continue;
                    }
                }
                otherTests.add(test);
            }
            otherTests = searchTest(otherTests, convertToPattern(availableSearch.getText().toLowerCase(Locale.ROOT)));
            passedTests = searchInfo(passedTests, convertToPattern(passedSearch.getText().toLowerCase(Locale.ROOT)));
            DefaultListModel<Test> dlm1 = new DefaultListModel<>();
            dlm1.addAll(otherTests);
            available.setModel(dlm1);
            DefaultListModel<PassedTest> dlm2 = new DefaultListModel<>();
            dlm2.addAll(passedTests);
            passed.setModel(dlm2);
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

    private ArrayList<PassedTest> searchInfo(ArrayList<PassedTest> source, String rule) {
        ArrayList<PassedTest> result = new ArrayList<>();
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
        for (PassedTest info : source) {
            String title = info.test.getTitle().toLowerCase(Locale.ROOT);
            String author = info.test.getAuthor().toLowerCase(Locale.ROOT);
            Matcher m = pattern.matcher(title);
            if (m.find()) {
                result.add(info);
                continue;
            }
            m = pattern.matcher(author);
            if (m.find()) {
                result.add(info);
            }
        }
        return result;
    }
}

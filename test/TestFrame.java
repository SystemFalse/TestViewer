package test;

import graphics.Assets;
import graphics.GraphicsSettings;
import users.Session;
import users.TestInfo;
import users.User;

import javax.swing.*;

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import java.awt.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.text.DateFormat;

import java.util.Timer;
import java.util.TimerTask;

public class TestFrame extends JFrame {

    private final Test test;

    private int lastResult = -1;
    private final JLabel result;
    private final JPanel statisticPanel;

    private final CardLayout switcher;
    private final CardLayout questionSwitcher;

    private final JPanel content;
    private final Instance[] questionManagers;

    private final JButton nextButton;
    private final JButton previousButton;

    private final boolean preview;

    public TestFrame(Test test) {
        this(test, false, false);
    }

    public TestFrame(Test test, boolean showRight) {
        this(test, showRight, false);
    }

    public TestFrame(Test test, boolean showRight, boolean preview) {
        if (test == null || test.questions() == 0) {
            throw new IllegalArgumentException("Неверный тест");
        }
        this.test = test;
        setTitle(test.getTitle());
        setIconImage(Assets.MAIN_ICON);
        switcher = new CardLayout();
        setLayout(switcher);

        //creating main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel(test.getTitle());
        title.setFont(GraphicsSettings.titleFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JTextArea description = new JTextArea();
        description.setText(test.getDescription());
        description.setFont(GraphicsSettings.descriptionFont);
        description.setTabSize(4);
        description.setEditable(false);
        description.setWrapStyleWord(true);
        description.setRows(5);
        description.setBorder(new BevelBorder(BevelBorder.LOWERED));
        center.add(new JScrollPane(description));
        JButton startButton = new JButton("Пройти");
        startButton.setFont(GraphicsSettings.startButtonFont);
        startButton.addActionListener(evt -> start());
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel rdp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rdp.add(startButton);
        center.add(rdp);
        mainPanel.add(center, BorderLayout.CENTER);
        JPanel down = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (test.getMaxTime() != null) {
            JLabel time = new JLabel("Время тестирования: " +
                    DateFormat.getTimeInstance().format(test.getMaxTime()));
            time.setFont(GraphicsSettings.authorFont);
            time.setBorder(new BevelBorder(BevelBorder.RAISED));
            down.add(time);
        }
        JLabel author = new JLabel(test.getAuthor());
        author.setFont(GraphicsSettings.authorFont);
        author.setBorder(new BevelBorder(BevelBorder.RAISED));
        down.add(author);
        mainPanel.add(down, BorderLayout.SOUTH);
        add("main", mainPanel);

        JPanel questionPanel = new JPanel(new BorderLayout());
        content = new JPanel(questionSwitcher = new CardLayout());
        questionManagers = new Instance[test.questions()];
        for (int i = 0; i < test.questions(); i++) {
            Question<?> q = test.get(i);
            JPanel qp = new JPanel(new BorderLayout());
            JTextArea qTitle = new JTextArea(q.getTitle());
            qTitle.setFont(GraphicsSettings.titleFont);
            qTitle.setEditable(false);
            qTitle.setLineWrap(true);
            qTitle.setWrapStyleWord(true);
            qTitle.setRows(3);
            qTitle.setPreferredSize(new Dimension(getWidth() - 10, 70));
            qTitle.setOpaque(false);
            qTitle.setFocusable(false);
            qp.add(new JScrollPane(qTitle), BorderLayout.NORTH);
            JPanel questionContent = new JPanel();
            questionManagers[i] = q.createFrameManager(questionContent, showRight);
            qp.add(questionContent, BorderLayout.CENTER);
            content.add("question-" + i, qp);
        }
        questionPanel.add(content, BorderLayout.CENTER);
        JPanel op = new JPanel(new GridLayout(1, 3));
        previousButton = new JButton("Назад");
        previousButton.setFont(GraphicsSettings.actionButtonsFont);
        previousButton.addActionListener(evt -> previousQuestion());
        previousButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        op.add(previousButton);
        nextButton = new JButton("Вперёд");
        nextButton.setFont(GraphicsSettings.actionButtonsFont);
        nextButton.addActionListener(evt -> nextQuestion());
        nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        op.add(nextButton);
        JButton checkButton = new JButton("Проверить");
        checkButton.setFont(GraphicsSettings.actionButtonsFont);
        checkButton.addActionListener(evt -> end(false));
        checkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        op.add(checkButton);
        questionPanel.add(op, BorderLayout.SOUTH);
        add("questions", questionPanel);
        //creating result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        center = new JPanel();
        center.setLayout(new FlowLayout(FlowLayout.CENTER));
        result = new JLabel();
        result.setFont(GraphicsSettings.resultTextFont);
        result.setHorizontalAlignment(SwingConstants.CENTER);
        resultPanel.add(result, BorderLayout.NORTH);
        JButton showStatistics = new JButton("Подробнее");
        showStatistics.setFont(GraphicsSettings.resultTextFont);
        showStatistics.addActionListener(evt -> switchTo(STATISTICS_PANEL));
        showStatistics.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        center.add(showStatistics);
        resultPanel.add(center, BorderLayout.CENTER);
        add("result", resultPanel);

        //creating statistics panel
        statisticPanel = new JPanel(new BorderLayout());
        JPanel up = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel st = new JLabel("Статистика");
        st.setFont(GraphicsSettings.statisticsTextFont);
        up.add(st);
        statisticPanel.add(up, BorderLayout.NORTH);

        add("statistics", statisticPanel);
        switcher.first(getContentPane());
        this.preview = preview;
        if (test.isControlTest()) {
            addSecurity();
        }
    }

    private JPanel createTable() {
        final int count = test.questions();
        GridLayout gl = new GridLayout(count + 1, 4);
        JPanel panel = new JPanel(gl);
        JLabel label = new JLabel("Номер вопроса");
        label.setFont(GraphicsSettings.statisticFont);
        label.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(label);
        label = new JLabel("Ваш ответ");
        label.setFont(GraphicsSettings.statisticFont);
        label.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(label);
        label = new JLabel("Полученные баллы");
        label.setFont(GraphicsSettings.statisticFont);
        label.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(label);
        label = new JLabel("Возможные баллы");
        label.setFont(GraphicsSettings.statisticFont);
        label.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(label);
        for (int i = 0; i < count; i++) {
            label = new JLabel((i + 1) + "");
            label.setFont(GraphicsSettings.statisticFont);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);

            label = new JLabel(questionManagers[i].getUserAnswer());
            label.setFont(GraphicsSettings.statisticFont);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);

            label = new JLabel(questionManagers[i].check() + "");
            label.setFont(GraphicsSettings.statisticFont);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);

            label = new JLabel(test.get(i).getMaxPoints() + "");
            label.setFont(GraphicsSettings.statisticFont);
            label.setBorder(new LineBorder(Color.BLACK, 1));
            panel.add(label);
        }
        JPanel parent = new JPanel(new FlowLayout());
        parent.add(panel);
        return parent;
    }

    private WindowFocusListener wfl;
    private Timer violationTimer = new Timer();
    private boolean isViolation;
    private int mistakes;

    private void addSecurity() {
        wfl = new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (isViolation) {
                    violationTimer.cancel();
                    violationTimer = new Timer();
                    isViolation = false;
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                isViolation = true;
                if (++mistakes == test.getMistakes()) {
                    JOptionPane.showMessageDialog(TestFrame.this,
                            "Вы слишком часто теряли окно из фокуса. Тест завершён принудительно",
                            "Тест остановлен", JOptionPane.PLAIN_MESSAGE);
                    end(true);
                    TestFrame.this.removeWindowFocusListener(wfl);
                    return;
                }
                violationTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(TestFrame.this,
                                "Вы нарушили правила проведения контрольного теста",
                                "Тест остановлен", JOptionPane.PLAIN_MESSAGE);
                        end(true);
                        TestFrame.this.removeWindowFocusListener(wfl);
                    }
                }, 20000);
                JOptionPane.showMessageDialog(TestFrame.this,
                        "Сейчас это окно находится вне фокуса\n" +
                                "Если вы не вернётесь к решению, через 20 секунд тест будет принудительно завершён.",
                        "Внимание", JOptionPane.PLAIN_MESSAGE);
            }
        };
        addWindowFocusListener(wfl);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                violationTimer.cancel();
            }
        });
    }

    Timer timer;
    private boolean testing;

    public void start() {
        if (testing) {
            return;
        }
        switchTo(QUESTIONS_PANEL);
        switchQuestion(0);
        testing = true;
        timer = new Timer();
        if (test.getMaxTime() != null) {
            String[] str = DateFormat.getTimeInstance().format(test.getMaxTime()).split(":");
            long delay = 0;
            delay += Byte.parseByte(str[0]) * 3_600_000;
            delay += Byte.parseByte(str[1]) * 60_000;
            delay += Byte.parseByte(str[2]) * 1000;
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(TestFrame.this, "Время вышло",
                            "Напоминание", JOptionPane.INFORMATION_MESSAGE);
                    end(false);
                }
            }, delay);
        }
    }

    public void nextQuestion() {
        if (qd + 1 < test.questions()) {
            switchQuestion(++qd);
        } else {
            getToolkit().beep();
        }
    }

    public void previousQuestion() {
        if (qd > 0) {
            switchQuestion(--qd);
        } else {
            getToolkit().beep();
        }
    }

    public void end(boolean interrupted) {
        if (!testing) {
            return;
        }
        testing = false;
        timer.cancel();
        showResult(interrupted);
        switchQuestion(0);
    }

    private JPanel lastTable;
    private int[] lastAnswers;

    public int checkAll() {
        int points = 0;
        lastAnswers = new int[questionManagers.length];
        for (int i = 0; i < lastAnswers.length; i++) {
            int answer = questionManagers[i].check();
            points += answer;
            lastAnswers[i] = answer;
        }
        return points;
    }

    private void showResult(boolean interrupted) {
        if (testing) {
            end(interrupted);
            return;
        }
        final int points = checkAll(), max = test.getMaxPoints();
        lastResult = points;
        result.setText(String.format("Результат: %d из %d (%.2f%%)",
                points, max, (float) points / (max / 100F)));
        if (lastTable != null) {
            statisticPanel.remove(lastTable);
        }
        statisticPanel.add(lastTable = createTable(), BorderLayout.CENTER);
        switchTo(RESULT_PANEL);

        if (!preview && TestManager.hasTest(test.getTitle())) {
            TestInfo info;
            if (TestManager.hasInformation(test.getTitle())) {
                info = TestManager.getInfoByTitle(test.getTitle());
            } else {
                info = new TestInfo(test);
                TestManager.loadInformation(info, false, false);
            }
            Session curSession = Session.currentSession();
            if (curSession != null) {
                User user = curSession.getUser();
                info.setResult(user, lastAnswers, interrupted);
                user.update();
            }
        }
        if (updater != null) {
            updater.run();
        }
    }

    public int getLastResult() {
        return lastResult;
    }

    public void reset() {
        for (Instance inst : questionManagers) {
            inst.reset();
        }
    }

    public final int MAIN_PANEL = 0;
    public final int QUESTIONS_PANEL = 1;
    public final int RESULT_PANEL = 2;
    public final int STATISTICS_PANEL = 3;

    private int qd = 0;

    private void switchQuestion(int index) {
        questionSwitcher.show(content, "question-" + index);
        previousButton.setEnabled(index != 0);
        nextButton.setEnabled(index != test.questions() - 1);
    }

    private void switchTo(int destination) {
        switch (destination) {
            case MAIN_PANEL -> switcher.show(getContentPane(), "main");
            case QUESTIONS_PANEL -> switcher.show(getContentPane(), "questions");
            case RESULT_PANEL -> switcher.show(getContentPane(), "result");
            case STATISTICS_PANEL -> switcher.show(getContentPane(), "statistics");
        }
    }

    private Runnable updater;

    public void setUpdater(Runnable updater) {
        this.updater = updater;
    }
}

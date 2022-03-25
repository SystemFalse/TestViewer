package graphics;

import test.Test;
import test.TestManager;
import users.Pupil;
import users.Teacher;
import users.TestInfo;
import users.User;
import users.Class;
import users.ClassManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Date;

class TeacherPagePainter implements Painter {

    private record PassedTest(Test test, int lastResult, int maxResult, Date passedTime, boolean interrupted) {

        @Override
        public String toString() {
            DateFormat df = DateFormat.getDateTimeInstance();
            return test + " (" + lastResult + '/' + maxResult + "; " +
                    df.format(passedTime)  + (interrupted ? "; был прерван" : "") + ')';
        }
    }

    private JFrame page;

    private JList<String> classes;
    private JList<Pupil> pupils;
    private JList<PassedTest> tests;

    private Class curClazz;
    private Pupil curPupil;

    public TeacherPagePainter() {
    }

    @Override
    public void paint(JFrame page, User user) {
        if (user.getType() != User.Type.TEACHER) {
            throw new IllegalArgumentException("Неверный тип пользователя");
        }
        Teacher teacher = (Teacher) user;
        this.page = page;
        page.setLayout(new GridLayout(3, 1));
        classes = new JList<>();
        pupils = new JList<>();
        tests = new JList<>();

        JPanel up = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Классы");
        title.setFont(GraphicsSettings.labelFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        up.add(title, BorderLayout.NORTH);
        classes.setOpaque(false);
        classes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classes.setFont(GraphicsSettings.testChooseFont);
        classes.setBorder(new LineBorder(Color.BLACK, 1));
        classes.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (classes.getModel().getSize() != 0) {
                    try {
                        String clName = classes.getModel().getElementAt(classes.getSelectedIndex());
                        Class clazz = ClassManager.getForName(clName);
                        showClass(clazz);
                        tests.setModel(new DefaultListModel<>());
                    } catch (RuntimeException ex) {
                        //ignore
                    }
                }
            }
        });
        classes.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (classes.getModel().getSize() != 0) {
                        try {
                            String clName = classes.getModel().getElementAt(classes.getSelectedIndex());
                            Class clazz = ClassManager.getForName(clName);
                            showClass(clazz);
                            tests.setModel(new DefaultListModel<>());
                        } catch (RuntimeException ex) {
                            //ignore
                        }
                    }
                }
            }
        });
        DefaultListModel<String> classModel = new DefaultListModel<>();
        teacher.forEachClassName(classModel::addElement);
        classes.setModel(classModel);
        JPanel container = new JPanel(new FlowLayout());
        container.add(classes);
        up.add(container, BorderLayout.CENTER);
        page.add(up);

        JPanel center = new JPanel(new BorderLayout());
        title = new JLabel("Ученики");
        title.setFont(GraphicsSettings.labelFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(title, BorderLayout.NORTH);
        pupils.setOpaque(false);
        pupils.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pupils.setFont(GraphicsSettings.testChooseFont);
        pupils.setBorder(new LineBorder(Color.BLACK, 1));
        pupils.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Pupil pupil = pupils.getModel().getElementAt(pupils.getSelectedIndex());
                    showPupil(pupil);
                } catch (RuntimeException ex) {
                    //ignore
                }
            }
        });
        pupils.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        Pupil pupil = pupils.getModel().getElementAt(pupils.getSelectedIndex());
                        showPupil(pupil);
                    } catch (RuntimeException ex) {
                        //ignore
                    }
                }
            }
        });
        center.add(pupils, BorderLayout.CENTER);
        page.add(center);

        JPanel down = new JPanel(new BorderLayout());
        title = new JLabel("Пройденные тесты");
        title.setFont(GraphicsSettings.labelFont);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        down.add(title, BorderLayout.NORTH);
        tests.setOpaque(false);
        tests.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tests.setFont(GraphicsSettings.testChooseFont);
        tests.setBorder(new LineBorder(Color.BLACK, 1));
        tests.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (tests.getModel().getSize() != 0) {
                        try {
                            PassedTest pt = tests.getModel().getElementAt(tests.getSelectedIndex());
                            openStat(pt.test);
                        } catch (RuntimeException ex) {
                            //ignore
                        }
                    }
                }
            }
        });
        tests.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (tests.getModel().getSize() != 0) {
                        try {
                            PassedTest pt = tests.getModel().getElementAt(tests.getSelectedIndex());
                            openStat(pt.test);
                        } catch (RuntimeException ex) {
                            //ignore
                        }
                    }
                }
            }
        });
        tests.setComponentPopupMenu(createPopup());
        down.add(tests, BorderLayout.CENTER);
        page.add(down);
    }

    private void openStat(Test test) {
        TestInfo info = TestManager.getInfoByTitle(test.getTitle());
        int[] results = info.getResultsOf(curPupil.getName());
        OptionPanels.showTestResults(test, results, page);
    }

    private void showClass(Class clazz) {
        DefaultListModel<Pupil> model = new DefaultListModel<>();
        int count = clazz.getPupilCount();
        for (int i = 0; i < count; i++) {
            model.addElement(clazz.getPupil(i));
        }
        pupils.setModel(model);
        curClazz = clazz;
    }

    private void showPupil(Pupil pupil) {
        DefaultListModel<PassedTest> model = new DefaultListModel<>();
        Test[] passed = TestManager.getTestsByRule(test -> test.getClassName().equals(curClazz.getName()) &&
                TestManager.hasInformation(test.getTitle()) &&
                TestManager.getInfoByTitle(test.getTitle()).hasResultOf(pupil.getName()));
        for (Test test : passed) {
            TestInfo info = TestManager.getInfoByTitle(test.getTitle());
            model.addElement(new PassedTest(test, info.getTotalScore(pupil.getName()),
                    test.getMaxPoints(), info.getDateOf(pupil.getName()), info.getInterrupted(pupil.getName())));
        }
        tests.setModel(model);
        curPupil = pupil;
    }

    private JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Просмотреть статистику");
        item.setFont(GraphicsSettings.menuItemFont);
        item.addActionListener(evt -> {
            if (tests.getModel().getSize() != 0) {
                PassedTest pt = tests.getModel().getElementAt(tests.getSelectedIndex());
                openStat(pt.test);
            }
        });
        menu.add(item);
        item = new JMenuItem("Признать недействительным");
        item.setFont(GraphicsSettings.menuItemFont);
        item.addActionListener(evt -> {
            if (tests.getModel().getSize() != 0) {
                int r = JOptionPane.showConfirmDialog(page, "Вы действительно хотите признать" +
                        " результаты теста недействительными?", "Предупреждение", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.OK_OPTION) {
                    PassedTest pt = tests.getModel().getElementAt(tests.getSelectedIndex());
                    TestInfo info = TestManager.getInfoByTitle(pt.test.getTitle());
                    info.removeResult(curPupil);
                    showPupil(curPupil);
                }
            }
        });
        menu.add(item);
        return menu;
    }
}

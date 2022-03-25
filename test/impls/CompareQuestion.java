package test.impls;

import graphics.GraphicsSettings;
import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;
import org.data_transfer.util.Array;
import org.data_transfer.util.IntTransfer;
import org.data_transfer.util.StringTransfer;
import org.data_transfer.util.Transferor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import test.Instance;
import test.Question;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class CompareQuestion extends Question<CompareAnswer> {

    public static final int TYPE = 2;
    private CheckRule checkRule;

    public CompareQuestion(String title, CompareAnswer rightAnswer) {
        this(title, 1, CheckRule.accordingly(), rightAnswer);
    }

    public CompareQuestion(String title, int maxPoints, CompareAnswer rightAnswer) {
        this(title, maxPoints, CheckRule.accordingly(), rightAnswer);
    }

    public CompareQuestion(String title, int maxPoints, CheckRule checkRule, CompareAnswer rightAnswer) {
        super(title, maxPoints, rightAnswer);
        this.checkRule = checkRule;
        normalize();
    }

    private void normalize() {
        int[][] pares = rightAnswer.pares();
        for (int i = 1; i <= pares.length; i++) {
            int index = find(i, pares);
            if (index == -1) {
                throw new RuntimeException("отсутствуют пары ко всем элементам первого столбца");
            }
            if (i - 1 == index) {
                continue;
            }
            int[] el = pares[i - 1];
            int[] replacement = pares[index];
            pares[i - 1] = replacement;
            pares[index] = el;
        }
    }

    private int find(int beginIndex, int[][] array) {
        for (int i = beginIndex - 1; i < array.length; i++) {
            if (array[i][0] == beginIndex) {
                return i;
            }
        }
        return -1;
    }

    public CheckRule getCheckRule() {
        return checkRule;
    }

    public void setCheckRule(CheckRule checkRule) {
        this.checkRule = checkRule;
        update();
    }

    @Override
    public Transferor<Question<?>> transferor() {
        return new CQTransfer();
    }

    @Override
    public Instance createFrameManager(JPanel parent, boolean showRight) {
        return new Painter(parent, showRight);
    }

    @Override
    public <E extends Question<?>> boolean deepEquals(E another) {
        if (this == another) {
            return true;
        }
        if (!super.equals(another)) {
            return false;
        }
        if (!(another instanceof CompareQuestion question)) {
            return false;
        }
        if (maxPoints != question.maxPoints) {
            return false;
        }
        if (!(rightAnswer != null && rightAnswer.equals(question.rightAnswer))) {
            return false;
        }
        return checkRule.equals(question.checkRule);
    }

    private class Painter implements Instance {

        final CardLayout switcher = new CardLayout();
        boolean showingVariants = true;
        final JTable answer;

        public Painter(JPanel parent, boolean showRight) {
            parent.setLayout(new BorderLayout());
            JPanel contentPanel = new JPanel(switcher);

            //creating option panel
            JPanel options = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton button = new JButton("Ответ");
            button.setFont(GraphicsSettings.switchButtonFont);
            button.setPreferredSize(new Dimension(150, 40));
            button.addActionListener(evt -> {
                if (showingVariants) {
                    switcher.show(contentPanel, "ans");
                    showingVariants = false;
                    button.setText("Таблица");
                } else {
                    switcher.show(contentPanel, "vars");
                    showingVariants = true;
                    button.setText("Ответ");
                }
            });
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            options.add(button);
            parent.add(options, BorderLayout.NORTH);

            //creating table with variants
            JPanel variantPanel = new JPanel();
            variantPanel.setLayout(new BoxLayout(variantPanel, BoxLayout.Y_AXIS));
            JPanel h = new JPanel(new GridLayout(1, 2, 50, 0));
            JLabel title = new JLabel("Столбец А");
            title.setFont(GraphicsSettings.labelFont);
            h.add(title);
            title = new JLabel("Столбец Б");
            title.setFont(GraphicsSettings.labelFont);
            h.add(title);
            variantPanel.add(h);
            final int max = Math.max(rightAnswer.aGroup().length, rightAnswer.bGroup().length);
            for (int i = 0; i < max; i++) {
                JPanel row = new JPanel(new GridLayout(1, 2, 50, 0));
                JTextArea aElement = new JTextArea();
                if (i < rightAnswer.aGroup().length) {
                    aElement.setText(rightAnswer.aGroup()[i]);
                }
                aElement.setFont(GraphicsSettings.compareElementFont);
                aElement.setBorder(new LineBorder(Color.BLACK, 1));
                aElement.setEditable(false);
                aElement.setRows(3);
                aElement.setWrapStyleWord(true);
                row.add(new JScrollPane(aElement));
                JTextArea bElement = new JTextArea();
                if (i < rightAnswer.bGroup().length) {
                    bElement.setText(rightAnswer.bGroup()[i]);
                }
                bElement.setFont(GraphicsSettings.compareElementFont);
                bElement.setBorder(new LineBorder(Color.BLACK, 1));
                bElement.setEditable(false);
                bElement.setRows(3);
                bElement.setWrapStyleWord(true);
                row.add(new JScrollPane(bElement));
                variantPanel.add(row);
            }
            contentPanel.add("vars", variantPanel);

            //creating answer table
            JPanel answerPanel = new JPanel();
            answerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            String[][] data = new String[2][rightAnswer.aGroup().length + 1];
            data[0][0] = "А";
            data[1][0] = "Б";
            for (int i = 1; i < data[0].length; i++) {
                data[0][i] = "" + i;
                data[1][i] = showRight ? rightAnswer.bGroup()[rightAnswer.pares()[i - 1][1]] : "";
            }
            String[] header = new String[data[0].length];
            Arrays.fill(header, "");
            answer = new JTable(data, header);
            answer.setRowHeight(40);
            answer.setFont(GraphicsSettings.compareAnswerFont);
            answer.setEditingRow(1);
            answer.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    answer.setValueAt("А", 0, 0);
                    answer.setValueAt("Б", 1, 0);
                    for (int i = 1; i < data[0].length; i++) {
                        answer.setValueAt("" + i, 0, i);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_RIGHT ||
                            e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT) {
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_ENTER ||
                            e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        return;
                    }
                    if (Character.isDigit(e.getKeyChar())) {
                        return;
                    }
                    JOptionPane.showMessageDialog(parent, e.getKeyChar() +
                            " не является числом", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });
            answerPanel.add(answer);
            contentPanel.add("ans", answerPanel);

            switcher.first(contentPanel);
            parent.add(contentPanel, BorderLayout.CENTER);
        }

        private int[] getUserChoose() {
            int[] choose = new int[rightAnswer.aGroup().length];
            for (int i = 1; i <= choose.length; i++) {
                String userValue = (String) answer.getValueAt(1, i);
                int index = Integer.parseInt(userValue);
                choose[i - 1] = index;
            }
            return choose;
        }

        private int[] getRightChoose() {
            int[] choose = new int[rightAnswer.aGroup().length];
            for (int i = 0 ; i < rightAnswer.pares().length; i++) {
                choose[i] = rightAnswer.pares()[i][1];
            }
            return choose;
        }

        @Override
        public int check() {
            return checkRule.check(getUserChoose(), getRightChoose(), maxPoints);
        }

        @Override
        public String getUserAnswer() {
            int[] choose = getUserChoose();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < choose.length; i++) {
                sb.append(choose[i]);
                if (i < choose.length - 1) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }

        @Override
        public void reset() {
            for (int i = 1; i < answer.getColumnCount(); i++) {
                answer.setValueAt("", 1, i);
            }
        }
    }

    @Override
    public String readingString(int number) {
        StringBuilder sb = new StringBuilder(super.readingString(number));

        return sb.toString();
    }

    public static Question<?> parseJSON(JSONObject obj, String title, int maxPoints) {
        JSONArray column;
        if (obj.containsKey("column1")) {
            try {
                column = (JSONArray) obj.get("column-1");
            } catch (ClassCastException e) {
                throw new RuntimeException("тэг \"column1\" не является массивом");
            }
        } else {
            throw new RuntimeException("отсутствует тэг \"column1\"");
        }
        ArrayList<String> aGroup = new ArrayList<>();
        for (Object str : column) {
            try {
                aGroup.add((String) str);
            } catch (ClassCastException e){
                throw new RuntimeException("массив \"column1\" заполнен не строками");
            }
        }
        if (obj.containsKey("column2")) {
            try {
                column = (JSONArray) obj.get("column2");
            } catch (ClassCastException e) {
                throw new RuntimeException("тэг \"column2\" не является массивом");
            }
        } else {
            throw new RuntimeException("отсутствует тэг \"column2\"");
        }
        ArrayList<String> bGroup = new ArrayList<>();
        for (Object str : column) {
            try {
                bGroup.add((String) str);
            } catch (ClassCastException e) {
                throw new RuntimeException("массив \"column2\" заполнен не строками");
            }
        }
        JSONArray compares;
        if (obj.containsKey("compares")) {
            try {
                compares = (JSONArray) obj.get("compares");
            } catch (ClassCastException e) {
                throw new RuntimeException("тэг \"compares\" не является массивом");
            }
        } else {
            throw new RuntimeException("отсутствует тэг \"compares\"");
        }
        ArrayList<int[]> pares = new ArrayList<>();
        for (Object array : compares) {
            JSONArray pare;
            try {
                pare = (JSONArray) array;
            } catch (ClassCastException e) {
                throw new RuntimeException("массив \"compares\" заполнен не массивами");
            }
            int[] add = new int[2];
            try {
                add[0] = ((Number) pare.get(0)).intValue();
            } catch (ClassCastException e) {
                throw new RuntimeException("пара в массиве \"compares\" не является массивом чисел");
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("массив в массиве \"compares\" пуст");
            }
            try {
                add[1] = ((Number) pare.get(1)).intValue();
            } catch (ClassCastException e) {
                throw new RuntimeException("пара в массиве \"compares\" не является массивом чисел");
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("массив в массиве \"compares\" имеет только один элемент");
            }
            pares.add(add);
        }
        int[][] adds = new int[pares.size()][2];
        for (int i = 0; i < adds.length; i++) {
            adds[i] = pares.get(i);
        }
        CheckRule checkRule;
        if (obj.containsKey("check-rule")) {
            String rule;
            try {
                rule = (String) obj.get("check-rule");
            } catch (ClassCastException e) {
                throw new RuntimeException("тег \"check-rule\" не строка");
            }
            checkRule = switch (rule) {
                case "AAR" -> new AAR();
                case "ACC" -> new ACC();
                case "RIW" -> new RIW();
                default -> throw new RuntimeException(
                        "неизвестный тип проверки - " + rule);
            };
        } else {
            checkRule = new ACC();
        }
        return new CompareQuestion(title, maxPoints, checkRule, new CompareAnswer(aGroup.toArray(new String[0]),
                bGroup.toArray(new String[0]), adds));
    }
}

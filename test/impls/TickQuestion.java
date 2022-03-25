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

import java.util.ArrayList;
import java.util.Arrays;

public class TickQuestion extends Question<TickAnswer> {

    public static final int TYPE = 1;

    private CheckRule checkRule;

    public TickQuestion(String title, TickAnswer rightAnswer) {
        this(title, 1, CheckRule.ifAllAreRight(), rightAnswer);
    }

    public TickQuestion(String title, int maxPoints, TickAnswer rightAnswer) {
        this(title, maxPoints, CheckRule.ifAllAreRight(), rightAnswer);
    }

    public TickQuestion(String title, int maxPoints, CheckRule checkRule, TickAnswer rightAnswer) {
        super(title, maxPoints, rightAnswer);
        this.checkRule = checkRule;
        normalize();
    }

    private void normalize() {
        int[] rights = rightAnswer.rights();
        Arrays.sort(rights);
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
        return new TQTransfer();
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
        if (!(another instanceof TickQuestion question)) {
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

        final JCheckBox[] answers;
        final boolean showRight;

        public Painter(JPanel parent, boolean showRight) {
            this.showRight = showRight;
            parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
            String[] vars = rightAnswer.variants();
            answers = new JCheckBox[vars.length];
            for (int i = 0; i < vars.length; i++) {
                String var = vars[i];
                JCheckBox answer = new JCheckBox(var);
                answer.setFont(GraphicsSettings.checkBoxFont);
                if (showRight && isRight(i)) {
                    answer.setSelected(true);
                }
                parent.add(answer);
                answers[i] = answer;
            }
        }

        private boolean isRight(int var) {
            var = var + 1;
            for (int r : rightAnswer.rights()) {
                if (r == var) {
                    return true;
                }
            }
            return false;
        }

        private int[] getSelected() {
            ArrayList<Integer> select = new ArrayList<>();
            for (int i = 0; i < answers.length; i++) {
                if (answers[i].isSelected()) {
                    select.add(i);
                }
            }
            int[] selected = new int[select.size()];
            for (int i = 0; i < selected.length; i++) {
                selected[i] = select.get(i) + 1;
            }
            return selected;
        }

        @Override
        public int check() {
            return checkRule.check(getSelected(), rightAnswer.rights(), maxPoints);
        }

        @Override
        public String getUserAnswer() {
            int[] selected = getSelected();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selected.length; i++) {
                sb.append(selected[i]);
                if (i < selected.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        @Override
        public void reset() {
            for (JCheckBox answer : answers) {
                answer.setSelected(false);
            }
        }
    }

    @Override
    public String readingString(int number) {
        StringBuilder sb = new StringBuilder(super.readingString(number));
        for (int i = 0; i < rightAnswer.variants().length; i++) {
            sb.append(i + 1).append(") ");
            sb.append(rightAnswer.variants()[i]);
            if (i < rightAnswer.variants().length - 1) {
                sb.append(";\n");
            }
        }
        return sb.toString();
    }

    public static Question<?> parseJSON(JSONObject obj, String title, int maxPoints) {
        if (!obj.containsKey("variants")) {
            throw new RuntimeException("нет вариантов ответа");
        }
        JSONArray variants;
        try {
            variants = (JSONArray) obj.get("variants");
        } catch (ClassCastException e) {
            throw new RuntimeException("тег \"variants\" не массив");
        }
        String[] vars = new String[variants.size()];
        try {
            for (int i = 0; i < vars.length; i++) {
                vars[i] = (String) variants.get(i);
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("в вариантах ответа не строки");
        }
        if (!obj.containsKey("rights")) {
            throw new RuntimeException("нет правильных ответов");
        }
        JSONArray rights;
        try {
            rights = (JSONArray) obj.get("rights");
        } catch (ClassCastException e) {
            throw new RuntimeException("тег \"rights\" не массив");
        }
        int[] rs = new int[rights.size()];
        try {
            for (int i = 0; i < rs.length; i++) {
                rs[i] = ((Number) rights.get(i)).intValue();
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("в правильных ответах не числа");
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
            checkRule = new AAR();
        }
        return new TickQuestion(title, maxPoints, checkRule, new TickAnswer(vars, rs));
    }
}

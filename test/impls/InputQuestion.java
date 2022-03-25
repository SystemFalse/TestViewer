package test.impls;

import graphics.GraphicsSettings;

import org.data_transfer.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import test.Instance;
import test.Question;

import static test.impls.SmartTextField.InputType;

import javax.swing.*;

import java.awt.*;

import java.util.Arrays;

public class InputQuestion extends Question<String[]> {

    public static final int TYPE = 0;

    private InputType it;
    private String hintText;

    public InputQuestion(String title, String... rightAnswers) {
        this(title, 1, InputType.Text, rightAnswers);
    }

    public InputQuestion(String title, int maxPoints, String... rightAnswers) {
        this(title, maxPoints, InputType.Text, rightAnswers);
    }

    public InputQuestion(String title, int maxPoints, InputType it, String... rightAnswers) {
        super(title, maxPoints, rightAnswers);
        this.it = it;
        this.hintText = it.getHint();
    }

    public InputType getInputType() {
        return it;
    }

    public void setInputType(InputType it) {
        this.hintText = it.getHint();
        this.it = it;
        update();
    }

    @Override
    public Transferor<Question<?>> transferor() {
        return new IQTransfer();
    }

    @Override
    public Instance createFrameManager(JPanel parent, boolean showRight) {
        if (rightAnswer == null || rightAnswer.length == 0) {
            throw new IllegalArgumentException("Не указан вариант ответа");
        }
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
        if (!(another instanceof InputQuestion question)) {
            return false;
        }
        if (maxPoints != question.maxPoints) {
            return false;
        }
        if (!Arrays.deepEquals(rightAnswer, question.rightAnswer)) {
            return false;
        }
        return it.equals(question.it);
    }

    private class Painter implements Instance {

        final JTextField answer;
        final boolean showRight;

        public Painter(JPanel parent, boolean showRight) {
            answer = new SmartTextField(hintText, it);
            answer.setPreferredSize(new Dimension(500, 40));
            answer.setFont(GraphicsSettings.inputFont);
            this.showRight = showRight;
            if (showRight) {
                answer.setText(rightAnswer[(int) (Math.random() * (rightAnswer.length - 1))]);
            }
            parent.setLayout(new FlowLayout(FlowLayout.CENTER));
            parent.add(answer);
        }

        @Override
        public int check() {
            String answer = getUserAnswer();
            for (String a : rightAnswer) {
                if (answer.equals(a)) {
                    return maxPoints;
                }
            }
            return 0;
        }

        @Override
        public String getUserAnswer() {
            return answer.getText();
        }

        @Override
        public void reset() {
            answer.setText(showRight ? rightAnswer[0] : "");
        }
    }

    @Override
    public String readingString(int number) {
        return super.readingString(number) + "_".repeat(20) + '(' + it.getHint() + ')';
    }

    public static InputQuestion parseJSON(JSONObject obj, String title, int maxPoints) {
        if (!obj.containsKey("answers")) {
            throw new RuntimeException("в вопросе нет ответа");
        }
        JSONArray answers;
        try {
            answers = (JSONArray) obj.get("answers");
        } catch (ClassCastException e) {
            throw new RuntimeException("тег \"answers\" не является массивом");
        }
        if (answers.isEmpty()) {
            throw new RuntimeException("в вопросе не указан ни один ответ");
        }
        String[] rightAnswer = new String[answers.size()];
        try {
            for (int i = 0; i < rightAnswer.length; i++) {
                rightAnswer[i] = (String) answers.get(i);
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("верные ответы не строки");
        }
        InputType it;
        if (obj.containsKey("input-type")) {
            String name;
            try {
                name = (String) obj.get("input-type");
            } catch (ClassCastException e) {
                throw new RuntimeException("тег \"input-type\" не строка");
            }
            try {
                it = InputType.valueOf(name);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("неизвестный тип ввода - " + name);
            }
        } else {
            it = InputType.Text;
        }
        return new InputQuestion(title, maxPoints, it, rightAnswer);
    }
}

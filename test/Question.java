package test;

import org.data_transfer.util.Transferor;

import org.json.simple.JSONObject;

import test.impls.InputQuestion;
import test.impls.TickQuestion;
import test.impls.CompareQuestion;

import javax.swing.*;

public abstract class Question<T> {

    protected String title;
    protected int maxPoints;
    protected T rightAnswer;

    Runnable actionHappens;

    public Question(String title) {
        this(title, 1, null);
    }

    public Question(String title, int maxPoints) {
        this(title, maxPoints, null);
    }

    public Question(String title, int maxPoints, T rightAnswer) {
        this.title = title;
        this.maxPoints = maxPoints > 0 ? maxPoints : 1;
        this.rightAnswer = rightAnswer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        update();
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
        update();
    }

    public T getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(T rightAnswer) {
        this.rightAnswer = rightAnswer;
        update();
    }

    public abstract Transferor<Question<?>> transferor();

    public abstract Instance createFrameManager(JPanel parent, boolean showRight);

    public abstract <E extends Question<?>> boolean deepEquals(E another);

    public String readingString(int number) {
        return "№" + number + ". " + title + '\n';
    }

    protected final synchronized void update() {
        if (actionHappens != null) {
            actionHappens.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question<?> question)) return false;

        return getTitle().equals(question.getTitle());
    }

    @Override
    public int hashCode() {
        int result = getTitle().hashCode();
        result = 31 * result + getMaxPoints();
        return result;
    }

    public static Question<?> parseJSON(JSONObject obj) {
        if (!obj.containsKey("title")) {
            throw new RuntimeException("вопрос без заголовка");
        }
        String title;
        try {
            title = (String) obj.get("title");
        } catch (ClassCastException e) {
            throw new RuntimeException("тег \"title\" не является строкой");
        }
        int maxPoints;
        if (obj.containsKey("max-points")) {
            try {
                maxPoints = ((Number) obj.get("max-points")).intValue();
            } catch (ClassCastException e) {
                throw new RuntimeException("тег \"max-points\" не является числом");
            }
            if (maxPoints < 0) {
                throw new RuntimeException("неправильное количество баллов - число должно быть положительным");
            }
        } else {
            maxPoints = 1;
        }
        if (!obj.containsKey("type")) {
            throw new RuntimeException("не указан тип вопроса");
        }
        int type;
        try {
            type = ((Number) obj.get("type")).intValue();
        } catch (ClassCastException e) {
            throw new RuntimeException("тег \"type\" не является числом");
        }
        return switch (type) {
            case InputQuestion.TYPE -> InputQuestion.parseJSON(obj, title, maxPoints);
            case TickQuestion.TYPE -> TickQuestion.parseJSON(obj, title, maxPoints);
            case CompareQuestion.TYPE -> CompareQuestion.parseJSON(obj, title, maxPoints);
            default -> throw new RuntimeException("неизвестный тип вопроса - " + type);
        };
    }
}

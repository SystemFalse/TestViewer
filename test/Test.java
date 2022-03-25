package test;

import org.data_transfer.util.*;
import org.data_transfer.util.Package;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Test implements Iterable<Question<?>> {

    private String title;
    private String description;
    private final ArrayList<Question<?>> questions;
    private Date maxTime;
    private String author;
    private String className;
    private boolean isControlTest;
    private int mistakes;

    public Test() {
        this(null, null, null, null, null, false, 3);
    }

    public Test(String title) {
        this(title, null, null, null, null, false, 3);
    }

    public Test(String title, String description) {
        this(title, description, null, null, null, false, 3);
    }

    public Test(String title, String description, Date maxTime) {
        this(title, description, maxTime, null, null, false, 3);
    }

    public Test(String title, String description, Date maxTime, String author) {
        this(title, description, maxTime, author, null, false, 3);
    }

    public Test(String title, String description, Date maxTime, String author, String className) {
        this(title, description, maxTime, author, className, false, 3);
    }

    public Test(String title, String description, Date maxTime, String author, String className, boolean isControlTest) {
        this(title, description, maxTime, author, className, isControlTest, 3);
    }

    public Test(String title, String description, Date maxTime, String author, String className, boolean isControlTest, int mistakes) {
        questions = new ArrayList<>();
        this.title = title;
        this.description = description;
        this.maxTime = maxTime;
        this.author = author;
        this.className = className;
        this.isControlTest = isControlTest;
        this.mistakes = mistakes;
    }

    public String getTitle() {
        return title != null ? title : "Без названия";
    }

    public void setTitle(String title) {
        if (TestManager.hasTest(this.title)) {
            TestManager.update(this.title);
        }
        this.title = title;
        TestManager.addToRewrite(this);
    }

    public String getDescription() {
        return description != null ? description : "Без описания";
    }

    public void setDescription(String description) {
        this.description = description;
        TestManager.addToRewrite(this);
    }

    public void add(Question<?> q) {
        questions.add(q);
        q.actionHappens = () -> TestManager.addToRewrite(this);
        TestManager.addToRewrite(this);
    }

    public Question<?> get(int index) {
        return questions.get(index);
    }

    public void remove(Question<?> q) {
        questions.remove(q);
        q.actionHappens = null;
        TestManager.addToRewrite(this);
    }

    public int questions() {
        return questions.size();
    }

    public void clear() {
        while (!questions.isEmpty()) {
            Question<?> q = questions.remove(0);
            q.actionHappens = null;
        }
        TestManager.addToRewrite(this);
    }

    public int getMaxPoints() {
        int max = 0;
        for (Question<?> q : questions) {
            max += q.getMaxPoints();
        }
        return max;
    }

    public Date getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Date maxTime) {
        this.maxTime = maxTime;
        TestManager.addToRewrite(this);
    }

    public String getAuthor() {
        return author != null ? author : "без автора";
    }

    public void setAuthor(String author) {
        this.author = author;
        TestManager.addToRewrite(this);
    }

    public String getClassName() {
        return className != null ? className : "";
    }

    public void setClassName(String className) {
        this.className = className;
        TestManager.addToRewrite(this);
    }

    public boolean isControlTest() {
        return isControlTest;
    }

    public void setControlTest(boolean controlTest) {
        isControlTest = controlTest;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        if (mistakes < 1) {
            throw new RuntimeException("количество ошибок может быть не меньше 1");
        }
        this.mistakes = mistakes;
    }

    public boolean deepEquals(Test another) {
        if (this == another) {
            return true;
        }
        if (!(title != null && title.equals(another.title))) {
            return false;
        }
        if (!(description != null && description.equals(another.description))) {
            return false;
        }
        if (!(author != null && author.equals(another.author))) {
            return false;
        }
        if (!(className != null && className.equals(another.className))) {
            return false;
        }
        if (!(maxTime != null && maxTime.equals(another.maxTime))) {
            return false;
        }
        for (Question<?> q : questions) {
            boolean equals = false;
            for (Question<?> cq : another.questions) {
                if (q.deepEquals(cq)) {
                    equals = true;
                    break;
                }
            }
            if (!equals) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Question<?>> iterator() {
        return new Iterator<>() {

            int cur;

            @Override
            public boolean hasNext() {
                return cur < questions();
            }

            @Override
            public Question<?> next() {
                return get(cur++);
            }
        };
    }

    public String readingString() {
        StringBuilder sb = new StringBuilder();
        sb.append('\t').append(getTitle()).append('\n');
        sb.append(getDescription()).append('\n');
        for (int i = 0; i < questions.size(); i++) {
            sb.append(questions.get(i).readingString(i + 1)).append('\n');
        }
        sb.append('\n').append("Автор: ").append(getAuthor());
        return sb.toString();
    }

    public Package pack() {
        Package pack = new Package();
        pack.put("title", getTitle(), StringTransfer.INSTANCE);
        pack.put("description", getDescription(), StringTransfer.INSTANCE);
        if (getMaxTime() != null) {
            pack.put("time", getMaxTime().getTime(), LongTransfer.INSTANCE);
        }
        Array<Question<?>> questions = new Array<>();
        for (Question<?> question : this.questions) {
            questions.add(question, question.transferor());
        }
        pack.put("questions", questions, new ArrayTransfer<>());
        pack.put("author", author, StringTransfer.INSTANCE);
        pack.put("class", className, StringTransfer.INSTANCE);
        pack.put("isControlTest", isControlTest, BooleanTransfer.INSTANCE);
        pack.put("mistakes", mistakes, IntTransfer.INSTANCE);
        return pack;
    }

    @Override
    public String toString() {
        return getTitle() + ", - " + getAuthor();
    }

    public static Test unpack(Package root) {
        Test test = new Test(root.getString("title"), root.getString("description"),
                null, root.getString("author"), root.getString("class"));
        if (root.containsName("time")) {
            test.setMaxTime(new Date(root.getLong("time")));
        }
        Array<Question<?>> questions = root.getArray("questions");
        questions.forEach(test::add);
        test.isControlTest = root.getBoolean("isControlTest");
        test.mistakes = root.getInt("mistakes");
        return test;
    }

    public static Test parseJSON(JSONObject obj) {
        String title = null;
        if (obj.containsKey("title")) {
            try {
                title = (String) obj.get("title");
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"title\" не является строкой");
            }
        }
        String description = null;
        if (obj.containsKey("description")) {
            try {
                description = (String) obj.get("description");
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"description\" не является строкой");
            }
        }
        Date maxTime = null;
        if (obj.containsKey("time")) {
            String timeText;
            try {
                timeText = (String) obj.get("time");
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"time\" не является строкой");
            }
            try {
                maxTime = DateFormat.getInstance().parse(timeText);
            } catch (ParseException e) {
                throw new RuntimeException("Ошибка в чтении времени: " + timeText);
            }
        }
        Test test = new Test(title, description, maxTime);
        if (!obj.containsKey("questions")) {
            throw new RuntimeException("В тесте нет вопросов");
        }
        JSONArray questions;
        try {
            questions = (JSONArray) obj.get("questions");
        } catch (ClassCastException e) {
            throw new RuntimeException("Тег \"questions\" не является массивом");
        }
        if (questions.isEmpty()) {
            throw new RuntimeException("В тесте нет ни одного вопроса");
        }
        for (int i = 0; i < questions.size(); i++) {
            //exceptionPrefix
            final String ep = "Ошибка в чтении вопроса " + (i + 1) + ": ";
            JSONObject question;
            try {
                question = (JSONObject) questions.get(i);
            } catch (ClassCastException e) {
                throw new RuntimeException(ep + "вопрос не является объектом");
            }
            Question<?> parsed;
            try {
                parsed = Question.parseJSON(question);
            } catch (RuntimeException e) {
                throw new RuntimeException(ep + e.getMessage());
            }
            test.add(parsed);
        }
        String className = null;
        if (obj.containsKey("class")) {
            String clazz;
            try {
                clazz = (String) obj.get("class");
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"class\" не является строкой");
            }
            className = clazz;
        }
        test.setClassName(className);
        String author = null;
        if (obj.containsKey("author")) {
            String a;
            try {
                a = (String) obj.get("author");
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"author\" не является строкой");
            }
            author = a;
        }
        test.setAuthor(author);
        boolean isControl = false;
        if (obj.containsKey("control")) {
            Boolean c;
            try {
                c = (Boolean) obj.get("control");
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"control\" не является истиной или ложью");
            }
            isControl = c;
        }
        test.setControlTest(isControl);
        int mistakes = 3;
        if (obj.containsKey("mistakes")) {
            int m;
            try {
                m = ((Number) obj.get("mistakes")).intValue();
            } catch (ClassCastException e) {
                throw new RuntimeException("Тег \"mistakes\" не является числом");
            }
            mistakes = m;
        }
        test.setMistakes(mistakes);
        return test;
    }
}

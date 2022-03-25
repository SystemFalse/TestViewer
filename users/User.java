package users;

import main.cmd.Command;
import test.Test;
import test.TestManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;

public sealed abstract class User permits Administrator, Pupil, Quest, Teacher {

    public enum Type {

        QUEST, PUPIL {

            @Override
            public int getPermLevel() {
                return 1;
            }

            @Override
            public String getRussianName() {
                return "Ученик";
            }
        }, TEACHER {

            @Override
            public int getPermLevel() {
                return 2;
            }

            @Override
            public String getRussianName() {
                return "Учитель";
            }
        }, ADMIN {

            @Override
            public int getPermLevel() {
                return 3;
            }

            @Override
            public String getRussianName() {
                return "Администратор";
            }
        };

        public int getPermLevel() {
            return 0;
        }

        public String getRussianName() {
            return "Гость";
        }
    }

    public static final int SIZE = 202;

    private String name;
    private String password;
    private final Type type;
    private boolean saveInSystem;

    private long lastMod = System.currentTimeMillis();

    protected User(String name, String password, Type type, boolean saveInSystem) {
        this.type = type;
        setName0(name);
        setPassword0(password);
        this.saveInSystem = saveInSystem;
    }

    public void setName0(String name) {
        if (UserSystem.getInstance() != null && UserSystem.getInstance().hasName(name)) {
            throw new RuntimeException("Это имя уже занято");
        }
        if (name.getBytes(StandardCharsets.UTF_8).length > 124) {
            throw new RuntimeException("Слишком длинное имя");
        } else if (name.isBlank()) {
            throw new RuntimeException("Пустое имя недопустимо");
        }
        if (name.contains("/") || name.contains("\\") || name.contains("*") || name.contains("?")
                || name.contains("\"") || name.contains("<") || name.contains(">") ||
                name.contains("|")) {
            throw new RuntimeException("Имя содержит недопустимый символ\n" +
                    "В имени не должно быть знаков: / \\ * ? \" < >");
        }
        if (UserSystem.getInstance() != null && UserSystem.getInstance().hasName(this.name)) {
            Test[] tests = TestManager.getTestsByRule(test -> test.getAuthor().equals(this.name));
            for (Test test : tests) {
                test.setAuthor(name);
            }
            TestInfo[] ts = TestManager.getInfoByRule(info -> info.hasResultOf(this.name));
            for (TestInfo info : ts) {
                TestManager.addToRewrite(info);
            }
        }
        this.name = name;
        update();
    }

    public void setName(String name) {
        User caller = Session.currentSession.getUser();
        if (caller == this || caller.getType().getPermLevel() > type.getPermLevel()) {
            setName0(name);
        } else {
            throw new RuntimeException("У вас недостаточно прав для этого");
        }
    }

    public String getName() {
        return name;
    }

    public void setPassword0(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 60) {
            throw new RuntimeException("Слишком длинный пароль");
        } else if (password.isBlank()) {
            throw new RuntimeException("Пустой пароль недопустим");
        } else if (password.length() < 8) {
            throw new RuntimeException("Пароль должен быть не менее 8-ми символов");
        }
        this.password = password;
        update();
    }

    public void setPassword(String password) {
        User caller = Session.currentSession.getUser();
        if (caller == this || caller.getType().getPermLevel() > type.getPermLevel()) {
            setPassword0(password);
        } else {
            throw new RuntimeException("У вас недостаточно прав для этого");
        }
        update();
    }

    public String getPassword() {
        return password;
    }

    public Type getType() {
        return type;
    }

    public boolean isSaveInSystem() {
        return saveInSystem;
    }

    public void setSaveInSystem(boolean saveInSystem) {
        this.saveInSystem = saveInSystem;
        update();
    }

    public void delete() {
        UserSystem.getInstance().deleteUser(this);
    }

    public long getLastMod() {
        return lastMod;
    }

    public void update() {
        lastMod = System.currentTimeMillis();
    }

    byte[] write() {
        byte[] nBytes = this.name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer name = ByteBuffer.allocate(128).putInt(nBytes.length).
                put(nBytes);
        byte[] pBytes = this.password.getBytes(StandardCharsets.UTF_8);
        ByteBuffer password = ByteBuffer.allocate(64).putInt(pBytes.length).
                put(pBytes);
        byte[] result = new byte[SIZE];
        System.arraycopy(name.array(), 0, result, 0, 128);
        System.arraycopy(password.array(), 0, result, 128, 64);
        System.arraycopy(ByteBuffer.allocate(8).putLong(lastMod).array(), 0,
                result, 192, 8);
        result[200] = (byte) (type.getPermLevel());
        result[201] = (byte) (saveInSystem ? 1 : 0);
        return result;
    }

    public abstract void readAddition(InputStream addition);

    public abstract void writeAddition(OutputStream addition);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return getName().equals(user.getName());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getPassword().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + (isSaveInSystem() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getFullInfo() {
        if (Command.hasAccess()) {
            return "Пользователь - " + name +
                    "; тип - " + type.getRussianName() +
                    "; пароль - " + password +
                    "; сохранять пароль - " + saveInSystem +
                    "; последние изменение - " + DateFormat.getDateTimeInstance().format(lastMod);
        } else {
            throw new RuntimeException("У вас нет прав на эту операцию");
        }
    }

    static User read(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4).put(bytes[0]).put(bytes[1]).put(bytes[2]).put(bytes[3]);
        byte[] name = new byte[buffer.getInt(0)];
        System.arraycopy(bytes, 4, name, 0, name.length);
        buffer.clear();
        buffer.put(bytes[128]).put(bytes[129]).put(bytes[130]).put(bytes[131]);
        byte[] password = new byte[buffer.getInt(0)];
        System.arraycopy(bytes, 132, password, 0, password.length);
        ByteBuffer lastMod = ByteBuffer.allocate(8);
        for (int i = 192; i < 200; i++) {
            lastMod.put(bytes[i]);
        }
        Type type = switch (bytes[200]) {
            case 3 -> Type.ADMIN;
            case 2 -> Type.TEACHER;
            case 1 -> Type.PUPIL;
            default -> Type.QUEST;
        };
        boolean saveInSystem = bytes[201] == 1;
        switch (type) {
            case PUPIL -> {
                User user = new Pupil(new String(name, StandardCharsets.UTF_8),
                        new String(password, StandardCharsets.UTF_8), saveInSystem);
                user.lastMod = lastMod.getLong(0);
                return user;
            }
            case TEACHER -> {
                User user = new Teacher(new String(name, StandardCharsets.UTF_8),
                        new String(password, StandardCharsets.UTF_8), saveInSystem);
                user.lastMod = lastMod.getLong(0);
                return user;
            }
            case ADMIN -> {
                User user = new Administrator(new String(name, StandardCharsets.UTF_8),
                        new String(password, StandardCharsets.UTF_8), saveInSystem);
                user.lastMod = lastMod.getLong(0);
                return user;
            }
            default -> throw new IllegalStateException("Неизвестное значение типа: " + type);
        }
    }

}

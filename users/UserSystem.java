package users;

import graphics.Assets;
import graphics.OptionPanels;
import main.cmd.Command;
import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;
import test.Test;
import test.TestManager;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

public class UserSystem {

    private static final File path = new File("users.bin");
    private static final UserSystem system = readFile(path);

    private final HashSet<User> users;

    private UserSystem() {
        users = new HashSet<>();
    }

    public void regin(User user) {
        users.add(user);
        writeFile();
    }

    public User access(String login, String password) {
        for (User user : users) {
            if (user.getName().equals(login)) {
                if (user.getPassword().equals(password)) {
                    return user;
                } else {
                    throw new RuntimeException("Неверный пароль");
                }
            }
        }
        throw userNotFound(login);
    }

    public User saveAccess(String login) {
        for (User user : users) {
            if (user.getName().equals(login)) {
                if (user.isSaveInSystem()) {
                    return user;
                }
            }
        }
        throw userNotFound(login);
    }

    public User privateAccess(String login) {
        User caller = Session.currentSession.getUser();
        if (caller == null) {
            throw new RuntimeException("Чтобы выполнить, вы должны войти");
        }
        for (User user : users) {
            if (user.getName().equals(login)) {
                if (caller == user || caller.getType().getPermLevel() >= user.getType().getPermLevel()) {
                    return user;
                }
                return null;
            }
        }
        throw userNotFound(login);
    }

    public User programAccess(String login) {
        for (User user : users) {
            if (user.getName().equals(login)) {
                return user;
            }
        }
        throw userNotFound(login);
    }

    private RuntimeException userNotFound(String login) {
        return new RuntimeException("Пользователь \"" + login + "\" не найден");
    }

    public void deleteCurrentUser() {
        User caller = Session.currentSession.getUser();
        deleteUser(caller);
    }

    void deleteUser(User user) {
        if (users.remove(user)) {
            Session session = Session.currentSession;
            if (session != null) {
                if (user.equals(Session.currentSession.getUser())) {
                    Session.currentSession.terminate();
                }
            }
            TestInfo[] tis = TestManager.getInfoByRule(info -> info.hasResultOf(user.getName()));
            for (TestInfo ti : tis) {
                ti.removeResult(user);
            }
            int r = JOptionPane.showConfirmDialog(null, "Удалить все тесты этого пользователя?",
                    "Предупреждение", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(Assets.QUESTION_ICON));
            if (r == JOptionPane.OK_OPTION) {
                Test[] ts = TestManager.getTestsByRule(test -> test.getAuthor() != null &&
                        test.getAuthor().equals(user.getName()));
                for (Test test : ts) {
                    TestManager.deleteTest(test);
                }
            }
            writeFile();
            OptionPanels.userDelete(user);
        }
    }

    public boolean hasName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String[] getUserNames() {
        Session session = Session.currentSession();
        if (session == null || session.getUser().getType().getPermLevel() != 3) {
            throw new RuntimeException("У вас нет прав для этой операции");
        }
        ArrayList<String> names = new ArrayList<>();
        for (User user : users) {
            names.add(user.getName());
        }
        return names.toArray(new String[0]);
    }

    public String getUserInfo() {
        StringBuilder sb = new StringBuilder();
        Iterator<User> itr = users.iterator();
        while (itr.hasNext()) {
            sb.append(itr.next().getFullInfo());
            if (itr.hasNext()) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public boolean hasAccess(Test test, String password) {
        String author = test.getAuthor();
        if (author == null) {
            return true;
        }
        if (hasName(author)) {
            if (programAccess(author).getPassword().equals(password)) {
                return true;
            }
        }
        for (User admin : users) {
            if (admin.getType() == User.Type.ADMIN) {
                if (admin.getPassword().equals(password)) {
                    return true;
                }
            }
        }
        return Command.hasAccess();
    }

    public boolean hasAccess(Pupil pupil, String password) {
        String className = pupil.getClassName();
        if (className != null) {
            Class clazz = ClassManager.getForName(className);
            String[] teachers = clazz.getTeacherNames();
            for (String name : teachers) {
                User teacher = UserSystem.system.programAccess(name);
                if (teacher.getPassword().equals(password)) {
                    return true;
                }
            }
        }
        for (User admin : users) {
            if (admin.getType() == User.Type.ADMIN) {
                if (admin.getPassword().equals(password)) {
                    return true;
                }
            }
        }
        return Command.hasAccess();
    }

    public static UserSystem getInstance() {
        return system;
    }

    public static void empty() {}

    private static boolean writing;

    public static synchronized void writeFile() {
        while (writing) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        Thread writer = new Thread(() -> {
            writing = true;
            try (FileOutputStream out = new FileOutputStream(path)) {
                Writer writer1 = new Writer(out);
                FunctionalTransfer.transferInt(system.users.size(), writer1);
                for (User user : system.users) {
                    try {
                        byte[] bytes = user.write();
                        for (int i = 0; i < User.SIZE; i++) {
                            bytes[i] = (byte) ~bytes[i];
                        }
                        out.write(bytes);
                        user.writeAddition(out);
                    } catch (IOException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Не удалось записать дополнительные данные пользователя \""
                                        + user.getName() + '"', e));
                    }
                }
            } catch (IOException e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                        new RuntimeException("Не удалось записать файл с пользователями", e));
            }
            writing = false;
        }, "saving users");
        writer.setPriority(Thread.MIN_PRIORITY);
        writer.start();
    }

    private static UserSystem readFile(File file) {
        UserSystem system = new UserSystem();
        if (file == null) {
            return system;
        }
        try {
            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    Reader reader = new Reader(in);
                    int length = FunctionalTransfer.transferInt(reader);
                    while (length-- > 0 & in.available() >= User.SIZE) {
                        byte[] data = new byte[User.SIZE];
                        //noinspection ResultOfMethodCallIgnored
                        in.read(data);
                        for (int i = 0; i < User.SIZE; i++) {
                            data[i] = (byte) ~data[i];
                        }
                        User user = User.read(data);
                        user.readAddition(in);
                        system.users.add(user);
                    }
                } catch (IOException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Не удалось прочесть файл с пользователями", e));
                }
            } else {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                    //noinspection ResultOfMethodCallIgnored
                    file.setWritable(true, false);
                    //noinspection ResultOfMethodCallIgnored
                    file.setReadable(true, false);
                } catch (IOException e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Не удалось создать файл пользователей", e));
                }
            }
        } catch (Exception e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
        return system;
    }

    public static void compareWith(File file) {
        if (path.equals(file)) {
            return;
        }
        UserSystem another = readFile(file);
        int r = JOptionPane.showConfirmDialog(null, "Пользователи будут обновлены до" +
                        " последней версии", "Вы действительно хотите объединить файлы пользователей?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(Assets.QUESTION_ICON));
        if (r == JOptionPane.OK_OPTION) {
            for (User user : another.users) {
                if (system.users.contains(user)) {
                    Date curLastMod = new Date(system.programAccess(user.getName()).getLastMod());
                    Date newLastMod = new Date(user.getLastMod());
                    if (newLastMod.after(curLastMod)) {
                        system.users.remove(user);
                        system.users.add(user);
                    }
                } else {
                    system.users.add(user);
                }
            }
        }
    }
}

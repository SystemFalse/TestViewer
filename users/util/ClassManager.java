package users.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class ClassManager {

    private static final File path = new File("class.info");

    private static final ArrayList<Class> classes;

    static {
        classes = new ArrayList<>();
        try {
            if (path.createNewFile()) {
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),
                        StandardCharsets.UTF_8))) {
                    out.write("Классы:\n");
                    out.flush();
                }
            } else {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path),
                        StandardCharsets.UTF_8))) {
                    load(in);
                }
            }
        } catch (IOException e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                    new RuntimeException("Не удалось создать или прочесть файл class.info", e));
        }
    }

    private ClassManager() {}

    private static void load(BufferedReader stream) throws IOException {
        Stream<String> doc = stream.lines();
        doc.forEach(str -> {
            if (str.equals("Классы:")) {
                return;
            }
            if (str.matches("([1-9]|10|11)\\s\"[А-Я]\"")) {
                addClass(str);
                return;
            }
            throw new RuntimeException("Неверная запись в строке - " + str);
        });
    }

    public static void writeFile() {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),
                StandardCharsets.UTF_8))) {
            load(out);
        } catch (IOException e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                    new RuntimeException("Не удалось записать файл class.info"));
        }
    }

    private static void load(BufferedWriter stream) {
        try {
            stream.write("Классы:\n");
            for (Class cl : classes) {
                stream.write(cl.getName() + '\n');
            }
            stream.flush();
        } catch (IOException e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                    new RuntimeException("Не удалось записать файл class.info"));
        }
    }

    public static boolean exists(String name) {
        for (Class cl : classes) {
            if (cl.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static Class getForName(String name) {
        for (Class cl : classes) {
            if (cl.getName().equals(name)) {
                return cl;
            }
        }
        throw new NoSuchElementException("Класса под названием " + name + " не существует");
    }

    public static void addClass(String name) {
        classes.add(new Class(name));
    }

    public static void empty() {}
}

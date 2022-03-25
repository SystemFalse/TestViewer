package test;

import graphics.Assets;
import graphics.MainMenu;
import main.Main;
import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;
import org.data_transfer.util.Package;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import users.TestInfo;
import users.UserSystem;
import users.ClassManager;

import javax.swing.*;
import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class TestManager {

    private static final File path = new File("tests");

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private static final HashSet<Test> loadedTests = new HashSet<>();
    private static final HashSet<TestInfo> testInformation = new HashSet<>();

    private static final ArrayList<Test> testsToRewrite = new ArrayList<>();
    private static final ArrayList<TestInfo> infoToRewrite = new ArrayList<>();

    static {
        Runnable action = () -> {
            if (path.exists()) {
                if (path.isDirectory()) {
                    File[] files = path.listFiles((dir, name) -> {
                        if (name.contains(".")) {
                            return name.substring(name.lastIndexOf('.')).equals(".test");
                        } else {
                            return false;
                        }
                    });
                    if (files != null) {
                        for (File file : files) {
                            readFile(file.getPath(), false, false, true);
                        }
                    }
                    files = path.listFiles((dir, name) -> {
                        if (name.contains(".")) {
                            return name.substring(name.lastIndexOf('.')).equals(".info");
                        } else {
                            return false;
                        }
                    });
                    if (files != null) {
                        for (File file : files) {
                            readFile(file.getPath(), false, false, true);
                        }
                    }
                } else {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Файл tests не является папкой"));
                }
            } else {
                if (!path.mkdir()) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Не удалось создать папку tests"));
                }
            }
            Main.menu.setTitle(MainMenu.APP_NAME);
        };
        Thread init = new Thread(action, "TestManager initialization");
        init.start();
    }

    private TestManager() {}

    public static void addToRewrite(Test test) {
        if (hasTest(test.getTitle())) {
            if (testsToRewrite.contains(test)) {
                return;
            }
            testsToRewrite.add(test);
        }
    }

    public static void addToRewrite(TestInfo info) {
        if (hasInformation(info.getTest().getTitle())) {
            if (infoToRewrite.contains(info)) {
                return;
            }
            infoToRewrite.add(info);
        }
    }

    public static boolean loadTest(Test test, boolean showWarnings, boolean nativeCall) {
        if (test == null) {
            return false;
        }
        String author = test.getAuthor();
        if (showWarnings && !UserSystem.getInstance().hasName(author)) {
            int r = JOptionPane.showConfirmDialog(null,
                    "Автора, указанного в тесте \"" + test.getTitle() + "\", нет в системе.\nПродолжить?",
                    "Предупреждение", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, new ImageIcon(Assets.QUESTION_ICON));
            if (r == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        if (showWarnings && !ClassManager.exists(test.getClassName())) {
            int r = JOptionPane.showConfirmDialog(null,
                    "Класса, указанного в тесте \"" + test.getTitle() + "\", нет в системе.\nПродолжить?",
                    "Предупреждение", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, new ImageIcon(Assets.QUESTION_ICON));
            if (r == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        if (showWarnings && hasTest(test.getTitle())) {
            int r = JOptionPane.showConfirmDialog(null,
                    "Тест с названием \"" + test.getTitle() + "\" уже существует.\nЗаменить тест новым?",
                    "Предупреждение", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, new ImageIcon(Assets.QUESTION_ICON));
            if (r == JOptionPane.OK_OPTION) {
                for (Test pre : loadedTests) {
                    if (pre.getTitle().equals(test.getTitle())) {
                        loadedTests.remove(pre);
                        break;
                    }
                }
            } else {
                return false;
            }
        }
        loadedTests.add(test);
        if (!nativeCall) testsToRewrite.add(test);
        return true;
    }

    public static boolean loadInformation(TestInfo info, boolean showWarnings, boolean nativeCall) {
        if (info == null) {
            return false;
        }
        Test test = info.getTest();
        if (showWarnings && hasInformation(test.getTitle())) {
            int r = JOptionPane.showConfirmDialog(null,
                    "Информация о тесте с таким же названием уже существует\nОбновить информацию новой?",
                    "Предупреждение", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, new ImageIcon(Assets.QUESTION_ICON));
            if (r == JOptionPane.OK_OPTION) {
                TestInfo pre = getInfoByTitle(info.getTest().getTitle());
                pre.compareWith(info);
            } else {
                return false;
            }
        }
        testInformation.add(info);
        if (!nativeCall) infoToRewrite.add(info);
        return true;
    }

    public static boolean hasTest(String testTitle) {
        for (Test test : loadedTests) {
            if (test.getTitle().equals(testTitle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInformation(String testTitle) {
        for (TestInfo info : testInformation) {
            if (info.getTest().getTitle().equals(testTitle)) {
                return true;
            }
        }
        return false;
    }

    public static Test getTestByTitle(String title) {
        for (Test test : loadedTests) {
            if (test.getTitle().equals(title)) {
                return test;
            }
        }
        throw new NoSuchElementException();
    }

    public static TestInfo getInfoByTitle(String title) {
        for (TestInfo info : testInformation) {
            if (info.getTest().getTitle().equals(title)) {
                return info;
            }
        }
        TestInfo info = new TestInfo(getTestByTitle(title));
        loadInformation(info, false, false);
        return info;
    }

    public static boolean deleteTest(Test test) {
        if (loadedTests.remove(test)) {
            File file = new File(convertToPath(test.getTitle()) + ".test");
            if (file.exists()) {
                file.deleteOnExit();
            }
            if (hasInformation(test.getTitle())) {
                deleteInformation(getInfoByTitle(test.getTitle()));
            }
            return true;
        }
        return false;
    }

    private static void deleteInformation(TestInfo info) {
        testInformation.remove(info);
        File file = new File(convertToPath(info.getTest().getTitle()) + ".info");
        if (file.exists()) {
            file.deleteOnExit();
        }
    }

    public static Test[] getTestsByRule(Predicate<Test> conditional) {
        ArrayList<Test> tests = new ArrayList<>();
        for (Test test : loadedTests) {
            if (conditional.test(test)) {
                tests.add(test);
            }
        }
        return tests.toArray(new Test[0]);
    }

    public static TestInfo[] getInfoByRule(Predicate<TestInfo> conditional) {
        ArrayList<TestInfo> inf = new ArrayList<>();
        for (TestInfo info : testInformation) {
            if (conditional.test(info)) {
                inf.add(info);
            }
        }
        return inf.toArray(new TestInfo[0]);
    }

    public static void update(String title) {
        File file = new File(convertToPath(title) + ".test");
        if (file.exists()) {
            file.deleteOnExit();
        }
        if (hasInformation(title)) {
            file = new File(convertToPath(title) + ".info");
            if (file.exists()) {
                file.deleteOnExit();
            }
        }
    }

    public static boolean isLoaded(Test test) {
        for (Test loaded : loadedTests) {
            if (loaded.deepEquals(test)) {
                return true;
            }
        }
        return false;
    }

    public static void reload() {
        loadedTests.forEach(TestManager::addToRewrite);
        testInformation.forEach(TestManager::addToRewrite);
        writeFiles();
    }

    public static int readFile(String path, boolean deleteOnExit, boolean showWarnings, boolean nativeCall) {
        File file = new File(path);
        if (file.exists() && !file.isHidden()) {
            String name = file.getName(), extension;
            if (name.contains(".")) {
                extension = name.substring(name.lastIndexOf('.'));
            } else {
                extension = "";
            }
            switch (extension) {
                case ".json":
                    try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                        JSONParser parser = new JSONParser();
                        JSONObject root = (JSONObject) parser.parse(in);
                        Test parsed = Test.parseJSON(root);
                        loadTest(parsed, showWarnings, nativeCall);
                        testsToRewrite.add(parsed);
                        if (deleteOnExit && !file.delete()) {
                            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                    new RuntimeException("Файл " + file.getAbsolutePath() + " не был удалён"));
                        }
                        return SUCCESS;
                    } catch (IOException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Не удалось прочесть файл " + file.getAbsolutePath(), e));
                        return FAIL;
                    } catch (ParseException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Ошибка в синтаксисе файла " + file.getAbsolutePath() + ";\n" +
                                        "индекс: " + e.getPosition() + "; тип ошибки:" + e.getErrorType(), e));
                        return FAIL;
                    } catch (ClassCastException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Неверный корневой элемент файла " + file.getAbsolutePath(), e));
                        return FAIL;
                    } catch (RuntimeException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Непредвиденная ошибка в создании теста: " +
                                        e.getMessage(), e));
                        return FAIL;
                    }
                case ".test":
                    try (FileInputStream in = new FileInputStream(file)) {
                        Reader reader = new Reader(in);
                        Package pack = FunctionalTransfer.transferPackage(reader);
                        Test test = Test.unpack(pack);
                        loadTest(test, showWarnings, nativeCall);
                        testsToRewrite.add(test);
                        return SUCCESS;
                    } catch (IOException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Не удалось прочесть файл " + file.getAbsolutePath(), e));
                        return FAIL;
                    } catch (RuntimeException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Непредвиденная ошибка в создании теста: " +
                                        e.getMessage(), e));
                        return FAIL;
                    }
                case ".info":
                    try (FileInputStream in = new FileInputStream(file)) {
                        Reader reader = new Reader(in);
                        Package pack = FunctionalTransfer.transferPackage(reader);
                        TestInfo unpack = TestInfo.unpack(pack);
                        loadInformation(unpack, showWarnings, true);
                        infoToRewrite.add(unpack);
                        return SUCCESS;
                    } catch (IOException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Не удалось прочесть файл " + file.getAbsolutePath(), e));
                        return FAIL;
                    } catch (RuntimeException e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                new RuntimeException("Непредвиденная ошибка в чтении информации о тесте: " +
                                        e.getMessage(), e));
                        return FAIL;
                    }
                default:
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                            new RuntimeException("Файл с расширением " + extension + " не поддерживаются"));
                    return FAIL;
            }
        } else {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                    new RuntimeException("Не удалось прочесть файл " + file.getAbsolutePath()));
            return FAIL;
        }
    }

    public static Test readTest(File file) {
        if (!file.exists()) {
            throw new RuntimeException("Файла " + file.getAbsolutePath() + " не существует");
        }
        String name = file.getName(), extension;
        if (name.contains(".")) {
            extension = name.substring(name.lastIndexOf('.'));
        } else {
            extension = "";
        }
        switch (extension) {
            case ".test" -> {
                try (FileInputStream in = new FileInputStream(file)) {
                    Reader reader = new Reader(in);
                    Package pack = FunctionalTransfer.transferPackage(reader);
                    return Test.unpack(pack);
                } catch (IOException e) {
                    throw new RuntimeException("Не удалось прочесть файл " + file.getAbsolutePath(), e);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Непредвиденная ошибка в создании теста: " +
                            e.getMessage(), e);
                }
            }
            case ".json" -> {
                try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    JSONParser parser = new JSONParser();
                    JSONObject root = (JSONObject) parser.parse(in);
                    return Test.parseJSON(root);
                } catch (IOException e) {
                    throw new RuntimeException("Не удалось прочесть файл " + file.getAbsolutePath(), e);
                } catch (ParseException e) {
                    throw new RuntimeException("Ошибка в синтаксисе файла " + file.getAbsolutePath() + ";\n" +
                            "индекс: " + e.getPosition() + "; тип ошибки:" + e.getErrorType(), e);
                } catch (ClassCastException e) {
                    throw new RuntimeException("Неверный корневой элемент файла " + file.getAbsolutePath(), e);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Непредвиденная ошибка в создании теста: " +
                                    e.getMessage(), e);
                }
            }
            default -> throw new RuntimeException("Файл с расширением " + extension + " не поддерживаются");
        }
    }

    public static void empty() {}

    public static void writeFiles() {
        if (!path.exists()) {
            if (!path.mkdir()) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                        new RuntimeException("Не удалось создать папку tests"));
                return;
            }
        }
        if (path.isDirectory()) {
            ArrayList<String> fails = new ArrayList<>();
            for (Test test : testsToRewrite) {
                try (FileOutputStream out = new FileOutputStream(convertToPath(
                        test.getTitle()) + ".test")) {
                    Package pack = test.pack();
                    Writer writer = new Writer(out);
                    FunctionalTransfer.transferPackage(pack, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                    fails.add('"' + test.getTitle() + "\"");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    fails.add('"' + test.getTitle() + "\" - " + e.getMessage());
                }
            }
            testsToRewrite.clear();
            if (!fails.isEmpty()) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                        new RuntimeException("Не удалось записать тесты: " + fails));
            }
            fails.clear();
            for (TestInfo info : infoToRewrite) {
                try (FileOutputStream out = new FileOutputStream(convertToPath(
                        info.getTest().getTitle()) + ".info")) {
                    Package pack = info.pack();
                    Writer writer = new Writer(out);
                    FunctionalTransfer.transferPackage(pack, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                    fails.add('"' + info.getTest().getTitle() + "\"");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    fails.add('"' + info.getTest().getTitle() + "\" - " + e.getMessage());
                }
            }
            infoToRewrite.clear();
            if (!fails.isEmpty()) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                        new RuntimeException("Не удалось записать информацию о тестах: " + fails));
            }
        }
    }

    public static String convertToPath(String title) {
        StringBuilder sb = new StringBuilder(System.getProperty("user.dir") + "\\tests\\");
        for (char l : title.toCharArray()) {
            if (Character.isAlphabetic(l)) {
                sb.append(l);
                continue;
            }
            if (Character.isDigit(l)) {
                sb.append(l);
                continue;
            }
            if (l == '-' || l == '_') {
                sb.append(l);
                continue;
            }
            if (l == ' ') {
                sb.append('_');
                continue;
            }
            if (l == '"') {
                sb.append('%').append((int) '"');
                continue;
            }
            if (l == '?') {
                sb.append('%').append((int) l);
                continue;
            }
            if ((0x0410 >= l & l <= 0x044f) || l == 'Ё' || l == 'ё') {
                sb.append(l);
                continue;
            }
            sb.append('%').append((int) l);
        }
        return sb.toString();
    }
}

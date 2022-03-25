package users;

import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;
import org.data_transfer.util.Array;
import org.data_transfer.util.StringTransfer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public final class Teacher extends User {

    private final HashSet<Class> classes = new HashSet<>();

    Teacher(String name, String password, boolean saveInSystem) {
        super(name, password, Type.TEACHER, saveInSystem);
    }

    void addClassName0(String className) {
        if (className == null) {
            return;
        }
        for (Class cl : classes) {
            if (cl.getName().equals(className)) {
                return;
            }
        }
        classes.add(ClassManager.getForName(className));
    }

    public void addClassName(String className) {
        addClassName0(className);
        update();
    }

    void removeClassName0(String className) {
        for (Class cl : classes) {
            if (cl.getName().equals(className)) {
                classes.remove(cl);
                break;
            }
        }
    }

    public void removeClassName(String className) {
        removeClassName0(className);
        update();
    }

    public boolean hasClassName(String className) {
        for (Class cl : classes) {
            if (cl.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public void forEachClassName(Consumer<String> action) {
        this.classes.forEach(cl -> action.accept(cl.getName()));
    }

    public void compareWith(List<String> classNames) {
        HashSet<Class> classCopy = new HashSet<>(classes);
        for (String className : classNames) {
            boolean contains = false;
            for (Class cl : classCopy) {
                String name = cl.getName();
                if (name.equals(className)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                ClassManager.getForName(className).addTeacher(this);
            }
        }
        for (Class cl : classCopy) {
            String name = cl.getName();
            boolean contains = false;
            for (String className : classNames) {
                if (name.equals(className)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                cl.removeTeacher(this);
            }
        }
    }

    @Override
    public void readAddition(InputStream addition) {
        Reader reader = new Reader(addition);
        Array<String> names = FunctionalTransfer.transferArray(reader);
        names.forEach(this::addClassName);
    }

    @Override
    public void writeAddition(OutputStream addition) {
        Writer output = new Writer(addition);
        Array<String> names = new Array<>(StringTransfer.INSTANCE);
        this.classes.forEach(cl -> names.add(cl.getName()));
        FunctionalTransfer.transferArray(names, output);
    }

    @Override
    public String getFullInfo() {
        return super.getFullInfo() + "; классы - " + classes;
    }

    public static Teacher regin(String name, String password, boolean saveInSystem) {
        Teacher teacher = new Teacher(name, password, saveInSystem);
        UserSystem.getInstance().regin(teacher);
        return teacher;
    }

    public static Teacher getInstance(String login, String password, boolean saveInSystem) {
        return new Teacher(login, password, saveInSystem);
    }
}

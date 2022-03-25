package users;

import org.data_transfer.FunctionalTransfer;
import org.data_transfer.io.Reader;
import org.data_transfer.io.Writer;

import java.io.InputStream;
import java.io.OutputStream;

public final class Pupil extends User {

    private Class clazz;

    Pupil(String name, String password, boolean saveInSystem) {
        super(name, password, Type.PUPIL, saveInSystem);
    }

    public String getClassName() {
        return clazz != null ? clazz.getName() : null;
    }

    void setClassName0(String className) {
        clazz = ClassManager.getForName(className);
    }

    public void setClassName(String className) {
        setClassName0(className);
        update();
    }

    @Override
    public void readAddition(InputStream addition) {
        Reader input = new Reader(addition);
        String className = FunctionalTransfer.transferString(input);
        if (className == null) {
            return;
        }
        ClassManager.getForName(className).addPupil(this);
    }

    @Override
    public void writeAddition(OutputStream addition) {
        Writer output = new Writer(addition);
        FunctionalTransfer.transferString(clazz.getName(), output);
    }

    public static Pupil regin(String name, String password, boolean saveInSystem) {
        Pupil pupil = new Pupil(name, password, saveInSystem);
        UserSystem.getInstance().regin(pupil);
        return pupil;
    }

    public static Pupil getInstance(String login, String password, boolean saveInSystem) {
        return new Pupil(login, password, saveInSystem);
    }

    @Override
    public String getFullInfo() {
        return super.getFullInfo() + "; класс - " + (clazz != null ? clazz : "нет");
    }
}

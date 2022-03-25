package users;

import java.io.InputStream;
import java.io.OutputStream;

public final class Administrator extends User {

    Administrator(String name, String password, boolean saveInSystem) {
        super(name, password, Type.ADMIN, saveInSystem);
    }

    @Override
    public void readAddition(InputStream addition) {

    }

    @Override
    public void writeAddition(OutputStream addition) {

    }

    public static Administrator regin(String name, String password, boolean saveInSystem) {
        Administrator administrator = new Administrator(name, password, saveInSystem);
        UserSystem.getInstance().regin(administrator);
        return administrator;
    }

    public static Administrator getInstance(String login, String password, boolean saveInSystem) {
        return new Administrator(login, password, saveInSystem);
    }
}

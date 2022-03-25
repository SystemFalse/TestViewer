package users;

import java.io.InputStream;
import java.io.OutputStream;

public final class Quest extends User {

    public Quest() {
        super("Гость", "1234567890", Type.QUEST, false);
    }

    @Override
    public void readAddition(InputStream addition) {

    }

    @Override
    public void writeAddition(OutputStream addition) {

    }
}

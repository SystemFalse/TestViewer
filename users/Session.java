package users;

import graphics.UserPage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Session {

    static Session currentSession;

    private final User user;
    private UserPage window;

    public Session(User user) {
        if (Session.currentSession != null && Session.currentSession.user == user) {
            throw new RuntimeException("Профиль этого пользователя уже открыт");
        }
        init();
        this.user = user;
        window = new UserPage(user);
    }

    public void start() {
        window.setVisible(true);
    }

    public User getUser() {
        return user;
    }

    public UserPage getWindow() {
        return window;
    }

    public void adminView(UserPage page) {
        final UserPage save = window;
        window = page;
        page.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                window = save;
            }
        });
    }

    public void terminate() {
        window.setVisible(false);
        currentSession = null;
    }

    private void init() {
        if (currentSession != null) {
            currentSession.terminate();
        }
        currentSession = this;
    }

    public static Session currentSession() {
        return currentSession;
    }
}

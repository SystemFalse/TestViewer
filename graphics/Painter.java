package graphics;

import users.User;

import javax.swing.*;

@FunctionalInterface
interface Painter {

    void paint(JFrame page, User user);
}

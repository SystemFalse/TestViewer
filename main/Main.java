package main;

import graphics.MainMenu;

import org.data_transfer.imports.TransferorLoader;

import test.TestManager;

import test.impls.CQTransfer;
import test.impls.IQTransfer;
import test.impls.TQTransfer;

import users.UserSystem;

import users.ClassManager;

import javax.swing.*;

import java.awt.*;

import java.io.File;
import java.io.IOException;

public class Main {

	public static MainMenu menu;

	public static void main(String[] args) throws UnsupportedLookAndFeelException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		UIManager.put("OptionPane.yesButtonText", "Да");
		UIManager.put("OptionPane.noButtonText", "Нет");
		UIManager.put("OptionPane.cancelButtonText", "Отмена");
		if (new File("tests").exists()) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			try {
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\cour.ttf")));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\courbd.ttf")));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\courbi.ttf")));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("fonts\\couri.ttf")));
			} catch (FontFormatException | IOException e) {
				//ignore
			}
		}
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		TransferorLoader.loadTransferor(new IQTransfer());
		TransferorLoader.loadTransferor(new TQTransfer());
		TransferorLoader.loadTransferor(new CQTransfer());
		menu = new MainMenu();
		menu.setAutoRequestFocus(true);
		menu.setVisible(true);
		ClassManager.empty();
		UserSystem.empty();
		TestManager.empty();
		ConsoleHandler.start();
		if (menu.isFocused()) {
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			try {
				Robot robot = new Robot(gd);
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				robot.mouseMove(screen.width / 2, screen.height / 2);
			} catch (AWTException e) {
				//ignore
			}
		}
	}
}

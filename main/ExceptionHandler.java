package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		try {
			new Thread(() -> {
				try (PrintWriter out = new PrintWriter(new FileOutputStream("error.log"))) {
					out.println(SimpleDateFormat.getDateTimeInstance().format(new Date()));
					e.printStackTrace(out);
				} catch (IOException ex) {
					//ignore
				}
			}).start();
			JOptionPane.showInternalMessageDialog(null, e.getMessage(),
					"Ошибка", JOptionPane.ERROR_MESSAGE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

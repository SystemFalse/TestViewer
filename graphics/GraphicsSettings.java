package graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class GraphicsSettings {

	public static final Toolkit TOOLS = Toolkit.getDefaultToolkit();

	public static Font inputFont = new Font("Courier New", Font.PLAIN, 24);
	public static Font registerFont = new Font("Courier New", Font.PLAIN, 14);

	public static Font popupMenuFont = new Font("Courier New", Font.PLAIN, 16);

	public static Font userFont = new Font("Courier New", Font.PLAIN, 17);

	public static Font titleFont = new Font("Courier New", Font.BOLD, 30);
	public static Font descriptionFont = new Font("Courier New", Font.PLAIN, 20);
	
	public static Font startButtonFont = new Font("Courier New", Font.BOLD, 35);
	public static Font authorFont = new Font("Courier New", Font.PLAIN, 12);
	public static Font statisticsTextFont = new Font("Courier New", Font.BOLD, 20);
	public static Font statisticFont = new Font("Courier New", Font.PLAIN, 14);
	
	public static Font actionButtonsFont = new Font("Courier New", Font.ITALIC, 20);
	
	public static Font resultTextFont = new Font("Courier New", Font.PLAIN, 30);
	public static Font restartButtonFont = new Font("Courier New", Font.BOLD, 35);
	public static Font toMainButtonFont = new Font("Courier New", Font.BOLD, 30);
	
	public static Font checkBoxFont = new Font("Courier New", Font.PLAIN, 25);

	public static Font stringAnswerFont = new Font("Courier New", Font.ITALIC, 25);

	public static Font compareElementFont = new Font("Courier New", Font.PLAIN, 20);
	public static Font switchButtonFont = new Font("Courier New", Font.PLAIN, 21);
	public static Font compareAnswerFont = new Font("Courier New", Font.BOLD, 18);
	
	public static Font radioButtonFont = new Font("Courier New", Font.PLAIN, 25);
	public static Font labelFont = new Font("Courier New", Font.PLAIN, 25);
	
	public static Font textAreaFont = new Font("Courier New", Font.PLAIN, 25);
	
	public static Font menuFont = new Font("Courier New", Font.BOLD, 18);
	public static Font menuItemFont = new Font("Courier New", Font.PLAIN, 16);

	public static Font testChooseFont = new Font("Courier New", Font.PLAIN, 20);
	
	public static Color lightMainBackground = new Color(168, 204, 181);
	public static Color lightMainText = new Color(58, 117, 196);
	public static Color lightButtonText = new Color(173, 216, 230);
	public static Color menuBackground = new Color(30, 89, 69);
	public static Color hintColor = new Color(128, 128, 128, 128);
	
	private static Font createFont(String value) {
		String[] values = value.split(",\\s");
		int style;
		switch (values[1]) {
			case "PLAIN" -> style = Font.PLAIN;
			case "BOLD" -> style = Font.BOLD;
			case "ITALIC" -> style = Font.ITALIC;
			case "BOLD_ITALIC" -> style = Font.BOLD | Font.ITALIC;
			default -> throw new RuntimeException("Неверное название типа: " + values[1]);
		}
		int size = Integer.parseInt(values[2]);
		return new Font(values[0], style, size);
	}
	
	private static String toString(Font font) {
		return font.getFamily() + ", " +
				(font.isPlain() ?
						"PLAIN" : font.isBold() ?
						"BOLD" : font.isItalic() ?
						"ITALIC" : "BOLD_ITALIC") + ", " +
				font.getSize();
	}
	
	static {
		Properties props = new Properties();
		File f = new File(System.getProperty("user.dir") + "\\graphics.properties");
		try {
			f.createNewFile();
			if (!f.canRead()) {
				f.setReadable(true);
			}
			try (FileInputStream input = new FileInputStream(f)) {
				props.load(input);
				boolean needToWrite = false;
				if (props.containsKey("inputFont")) {
					try {
						inputFont = createFont(props.getProperty("inputFont"));
					} catch (RuntimeException ex) {
						props.put("inputFont", toString(inputFont));
						needToWrite = true;
					}
				} else {
					props.put("inputFont", toString(inputFont));
					needToWrite = true;
				}
				if (props.containsKey("registerFont")) {
					try {
						registerFont = createFont(props.getProperty("registerFont"));
					} catch (RuntimeException ex) {
						props.put("registerFont", toString(registerFont));
						needToWrite = true;
					}
				} else {
					props.put("registerFont", toString(registerFont));
					needToWrite = true;
				}
				if (props.containsKey("popupMenuFont")) {
					try {
						popupMenuFont = createFont(props.getProperty("popupMenuFont"));
					} catch (RuntimeException ex) {
						props.put("popupMenuFont", toString(popupMenuFont));
						needToWrite = true;
					}
				} else {
					props.put("popupMenuFont", toString(popupMenuFont));
					needToWrite = true;
				}
				if (props.containsKey("userFont")) {
					try {
						userFont = createFont(props.getProperty("userFont"));
					} catch (RuntimeException ex) {
						props.put("userFont", toString(userFont));
						needToWrite = true;
					}
				} else {
					props.put("userFont", toString(userFont));
					needToWrite = true;
				}
				if (props.containsKey("titleFont")) {
					try {
						titleFont = createFont(props.getProperty("titleFont"));
					} catch (RuntimeException ex) {
						props.put("titleFont", toString(titleFont));
						needToWrite = true;
					}
				} else {
					props.put("titleFont", toString(titleFont));
					needToWrite = true;
				}
				if (props.containsKey("descriptionFont")) {
					try {
						descriptionFont = createFont(props.getProperty("descriptionFont"));
					} catch (RuntimeException ex) {
						props.put("descriptionFont", toString(descriptionFont));
						needToWrite = true;
					}
				} else {
					props.put("descriptionFont", toString(descriptionFont));
					needToWrite = true;
				}
				if (props.containsKey("startButtonFont")) {
					try {
						startButtonFont = createFont(props.getProperty("startButtonFont"));
					} catch (RuntimeException ex) {
						props.put("startButtonFont", toString(startButtonFont));
						needToWrite = true;
					}
				} else {
					props.put("startButtonFont", toString(startButtonFont));
					needToWrite = true;
				}
				if (props.containsKey("authorFont")) {
					try {
						authorFont = createFont(props.getProperty("authorFont"));
					} catch (RuntimeException ex) {
						props.put("authorFont", toString(authorFont));
						needToWrite = true;
					}
				} else {
					props.put("authorFont", toString(authorFont));
					needToWrite = true;
				}
				if (props.containsKey("statisticsTextFont")) {
					try {
						statisticsTextFont = createFont(props.getProperty("statisticsTextFont"));
					} catch (RuntimeException ex) {
						props.put("statisticsTextFont", toString(statisticsTextFont));
						needToWrite = true;
					}
				} else {
					props.put("statisticsTextFont", toString(statisticsTextFont));
					needToWrite = true;
				}
				if (props.containsKey("statisticFont")) {
					try {
						statisticFont = createFont(props.getProperty("statisticFont"));
					} catch (RuntimeException ex) {
						props.put("statisticFont", toString(statisticFont));
						needToWrite = true;
					}
				} else {
					props.put("statisticFont", toString(statisticFont));
					needToWrite = true;
				}
				if (props.containsKey("actionButtonsFont")) {
					try {
						actionButtonsFont = createFont(props.getProperty("actionButtonsFont"));
					} catch (RuntimeException ex) {
						props.put("actionButtonsFont", toString(actionButtonsFont));
						needToWrite = true;
					}
				} else {
					props.put("actionButtonsFont", toString(actionButtonsFont));
					needToWrite = true;
				}
				if (props.containsKey("resultTextFont")) {
					try {
						resultTextFont = createFont(props.getProperty("resultTextFont"));
					} catch (RuntimeException ex) {
						props.put("resultTextFont", toString(resultTextFont));
						needToWrite = true;
					}
				} else {
					props.put("resultTextFont", toString(resultTextFont));
					needToWrite = true;
				}
				if (props.containsKey("restartButtonFont")) {
					try {
						restartButtonFont = createFont(props.getProperty("restartButtonFont"));
					} catch (RuntimeException ex) {
						props.put("restartButtonFont", toString(restartButtonFont));
						needToWrite = true;
					}
				} else {
					props.put("restartButtonFont", toString(restartButtonFont));
					needToWrite = true;
				}
				if (props.containsKey("toMainButtonFont")) {
					try {
						toMainButtonFont = createFont(props.getProperty("toMainButtonFont"));
					} catch (RuntimeException ex) {
						props.put("toMainButtonFont", toString(toMainButtonFont));
						needToWrite = true;
					}
				} else {
					props.put("toMainButtonFont", toString(toMainButtonFont));
					needToWrite = true;
				}
				if (props.containsKey("checkBoxFont")) {
					try {
						checkBoxFont = createFont(props.getProperty("checkBoxFont"));
					} catch (RuntimeException ex) {
						props.put("checkBoxFont", toString(checkBoxFont));
						needToWrite = true;
					}
				} else {
					props.put("checkBoxFont", toString(checkBoxFont));
					needToWrite = true;
				}
				if (props.containsKey("stringAnswerFont")) {
					try {
						stringAnswerFont = createFont(props.getProperty("stringAnswerFont"));
					} catch (RuntimeException ex) {
						props.put("stringAnswerFont", toString(stringAnswerFont));
						needToWrite = true;
					}
				} else {
					props.put("stringAnswerFont", toString(stringAnswerFont));
					needToWrite = true;
				}
				if (props.containsKey("compareElementFont")) {
					try {
						compareElementFont = createFont(props.getProperty("compareElementFont"));
					} catch (RuntimeException ex) {
						props.put("compareElementFont", toString(compareElementFont));
						needToWrite = true;
					}
				} else {
					props.put("compareElementFont", toString(compareElementFont));
					needToWrite = true;
				}
				if (props.containsKey("switchButtonFont")) {
					try {
						switchButtonFont = createFont(props.getProperty("switchButtonFont"));
					} catch (RuntimeException ex) {
						props.put("switchButtonFont", toString(switchButtonFont));
						needToWrite = true;
					}
				} else {
					props.put("switchButtonFont", toString(switchButtonFont));
					needToWrite = true;
				}
				if (props.containsKey("compareAnswerFont")) {
					try {
						compareAnswerFont = createFont(props.getProperty("compareAnswerFont"));
					} catch (RuntimeException ex) {
						props.put("compareAnswerFont", toString(compareAnswerFont));
						needToWrite = true;
					}
				} else {
					props.put("compareAnswerFont", toString(compareAnswerFont));
					needToWrite = true;
				}
				if (props.containsKey("radioButtonFont")) {
					try {
						radioButtonFont = createFont(props.getProperty("radioButtonFont"));
					} catch (RuntimeException ex) {
						props.put("radioButtonFont", toString(radioButtonFont));
						needToWrite = true;
					}
				} else {
					props.put("radioButtonFont", toString(radioButtonFont));
					needToWrite = true;
				}
				if (props.containsKey("labelFont")) {
					try {
						labelFont = createFont(props.getProperty("labelFont"));
					} catch (RuntimeException ex) {
						props.put("labelFont", toString(labelFont));
						needToWrite = true;
					}
				} else {
					props.put("labelFont", toString(labelFont));
					needToWrite = true;
				}
				if (props.containsKey("textAreaFont")) {
					try {
						textAreaFont = createFont(props.getProperty("textAreaFont"));
					} catch (RuntimeException ex) {
						props.put("textAreaFont", toString(textAreaFont));
						needToWrite = true;
					}
				} else {
					props.put("textAreaFont", toString(textAreaFont));
					needToWrite = true;
				}
				if (props.containsKey("menuFont")) {
					try {
						menuFont = createFont(props.getProperty("menuFont"));
					} catch (RuntimeException ex) {
						props.put("menuFont", toString(menuFont));
						needToWrite = true;
					}
				} else {
					props.put("menuFont", toString(menuFont));
					needToWrite = true;
				}
				if (props.containsKey("menuItemFont")) {
					try {
						menuItemFont = createFont(props.getProperty("menuItemFont"));
					} catch (RuntimeException ex) {
						props.put("menuItemFont", toString(menuItemFont));
						needToWrite = true;
					}
				} else {
					props.put("menuItemFont", toString(menuItemFont));
					needToWrite = true;
				}
				if (props.containsKey("testChooseFont")) {
					try {
						testChooseFont = createFont(props.getProperty("testChooseFont"));
					} catch (RuntimeException ex) {
						props.put("testChooseFont", toString(testChooseFont));
						needToWrite = true;
					}
				} else {
					props.put("testChooseFont", toString(testChooseFont));
					needToWrite = true;
				}
				if (needToWrite) {
					if (!f.canWrite()) {
						if (!f.setWritable(true)) {
							throw new IOException("Файл свойств не может быть записан");
						}
					}
					try (FileOutputStream output = new FileOutputStream(f)) {
						props.store(output, null);
					}
				}
			}
		} catch (Exception e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
		}
	}
	
	private GraphicsSettings() {}
}

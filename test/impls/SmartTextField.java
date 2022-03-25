package test.impls;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SmartTextField extends JTextField {

    private static final Color hintColor = new Color(128, 128, 128, 128);

    public enum InputType {

        Number {

            @Override
            public String getHint() {
                return "число";
            }

            @Override
            public boolean error(String text) {
                return !text.matches("\\d+(\\.\\d+)?|\\.\\d+");
            }

            @Override
            public byte byteNumber() {
                return 0;
            }
        },
        Text {

            @Override
            public String getHint() {
                return "текст";
            }

            @Override
            public boolean error(String text) {
                return !text.matches(".+");
            }

            @Override
            public byte byteNumber() {
                return 1;
            }
        },
        Fraction {

            @Override
            public String getHint() {
                return "дробь";
            }

            @Override
            public boolean error(String text) {
                return !text.matches("\\d+\\s?/\\s?\\d+");
            }

            @Override
            public byte byteNumber() {
                return 2;
            }
        },
        Date {

            @Override
            public String getHint() {
                return "дата";
            }

            @Override
            public boolean error(String text) {
                return !text.matches("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d");
            }

            @Override
            public byte byteNumber() {
                return 3;
            }
        },
        Time {
            @Override

            public String getHint() {
                return "время";
            }

            @Override
            public boolean error(String text) {
                return !text.matches("\\d\\d:\\d\\d");
            }

            @Override
            public byte byteNumber() {
                return 4;
            }
        };

        public String getHint() {
            return null;
        }

        public boolean error(String text) {
            return false;
        }

        public byte byteNumber() {
            return -1;
        }

        public static InputType forByteNumber(byte number) {
            return switch (number) {
                case 0 -> Number;
                case 1 -> Text;
                case 2 -> Fraction;
                case 3 -> Date;
                case 4 -> Time;
                default -> null;
            };
        }
    }

    private final String hint;
    private final InputType it;

    public SmartTextField(String hint, InputType it) {
        this.hint = hint;
        this.it = it;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hint != null && getText().equals("")) {
            g.setColor(hintColor);
            g.drawString(hint, 5, getPreferredSize().height / 3 * 2);
        }
        String text = getText();
        if (!text.equals("") && it.error(text)) {
            setBorder(new LineBorder(Color.RED, 2));
        } else {
            setBorder(new LineBorder(Color.BLACK, 2));
        }
    }
}

package graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Assets {

    public static final Image MAIN_ICON;
    public static final Image USER_PAGE_ICON;
    public static final Image ADMIN_PAGE_ICON;
    public static final Image QUESTION_ICON;
    public static final Image LOAD_MENU_ICON;
    public static final Image PACKAGE_MENU_ICON;
    public static final Image HELP_MENU_ICON;
    public static final Image CREATOR_MENU_ICON;
    public static final Image PREVIEW_MENU_ICON;
    public static final Image COMPARE_USERS_MENU_ICON;
    public static final Image MY_TESTS_MENU_ICON;
    public static final Image CREATING_MENU_ICON;

    static {
        BufferedImage texture = null;
        try {
            texture = ImageIO.read(Objects.requireNonNull(Assets.class.getResourceAsStream("texture.png")));
        } catch (IOException e) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
        assert texture != null;
        MAIN_ICON = texture.getSubimage(0, 0, 64, 64);
        USER_PAGE_ICON = texture.getSubimage(64, 0, 64, 64);
        ADMIN_PAGE_ICON = texture.getSubimage(128, 0, 64, 64);
        QUESTION_ICON = texture.getSubimage(192, 0, 32, 32);
        LOAD_MENU_ICON = texture.getSubimage(224, 0, 20, 20);
        PACKAGE_MENU_ICON = texture.getSubimage(244, 0, 20, 20);
        HELP_MENU_ICON = texture.getSubimage(264, 0, 20, 20);
        CREATOR_MENU_ICON = texture.getSubimage(224, 20, 20, 20);
        PREVIEW_MENU_ICON = texture.getSubimage(244, 20, 20, 20);
        COMPARE_USERS_MENU_ICON = texture.getSubimage(264, 20, 20, 20);
        MY_TESTS_MENU_ICON = texture.getSubimage(224, 40, 20, 20);
        CREATING_MENU_ICON = texture.getSubimage(244, 40, 20, 20);
    }

    private Assets() {}
}

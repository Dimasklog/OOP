package org.example.blackjack.ui;

import javax.swing.*;

/**
 * Запускает оконный интерфейс блэкджека.
 */
public final class BlackjackUiApplication {

    private BlackjackUiApplication() {
    }

    /**
     * Открывает игровой стол.
     *
     * @param args первый аргумент — необязательное зерно генератора
     */
    public static void main(String[] args) {
        final long seed;

        try {
            seed = args.length > 0
                    ? Long.parseLong(args[0])
                    : System.currentTimeMillis();
        } catch (NumberFormatException e) {
            System.err.println(
                    "Зерно должно быть целым числом типа long."
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            UIManager.put("OptionPane.okButtonText", "ОК");
            UIManager.put("OptionPane.cancelButtonText", "Отмена");
            UIManager.put("OptionPane.yesButtonText", "Да");
            UIManager.put("OptionPane.noButtonText", "Нет");

            new BlackjackWindow(seed).setVisible(true);
        });
    }
}
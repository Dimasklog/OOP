package org.example.blackjack.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Масштабирует интерфейс и принадлежащие окну диалоги.
 */
public final class UiScale implements AutoCloseable {
    private static final String STYLE = "blackjack.baseStyle";
    private static final String GAPS = "blackjack.baseGaps";

    private final JFrame window;
    private final Runnable onChange;

    private final JComboBox<String> choice = new JComboBox<>(
            new String[]{
                    "Авто", "100%", "125%",
                    "150%", "175%", "200%"
            }
    );

    private final Timer timer;
    private final ComponentListener resizeListener;
    private final AWTEventListener dialogs;

    private double factor = 1;
    private boolean closed;

    private record Style(Font font, Border border) {
    }

    /**
     * Создает управление масштабом.
     *
     * @param window окно приложения
     * @param onChange обновление размеров и содержимого окна
     */
    public UiScale(JFrame window, Runnable onChange) {
        this.window = window;
        this.onChange = onChange;

        timer = new Timer(120, e -> update());
        timer.setRepeats(false);

        resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                timer.restart();
            }
        };

        window.addComponentListener(resizeListener);
        choice.addActionListener(e -> update());

        dialogs = event -> {
            if (event instanceof WindowEvent e
                    && e.getID() == WindowEvent.WINDOW_OPENED
                    && e.getWindow() instanceof JDialog dialog
                    && belongsToWindow(dialog)) {

                apply(dialog.getContentPane());
                dialog.pack();
                dialog.setLocationRelativeTo(window);
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(
                dialogs,
                AWTEvent.WINDOW_EVENT_MASK
        );
    }

    /**
     * Возвращает панель выбора масштаба.
     *
     * @return панель управления
     */
    public JPanel control() {
        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0)
        );

        panel.add(new JLabel("Масштаб:"));
        panel.add(choice);

        return panel;
    }

    /**
     * Пересчитывает масштаб по размеру окна
     * или выбору пользователя.
     */
    public void update() {
        if (closed) {
            return;
        }

        String selected = (String) choice.getSelectedItem();

        if ("Авто".equals(selected)) {
            double size = Math.min(
                    window.getWidth() / 1200.0,
                    window.getHeight() / 760.0
            );

            factor = Math.round(
                    Math.max(1, Math.min(2.5, size)) * 20
            ) / 20.0;
        } else {
            factor = Integer.parseInt(
                    selected.replace("%", "")
            ) / 100.0;
        }

        apply(window.getContentPane());
        onChange.run();
    }

    /**
     * Масштабирует размер относительно исходного значения.
     *
     * @param value исходный размер
     * @return размер с учетом масштаба
     */
    public int px(int value) {
        return (int) Math.round(value * factor);
    }

    /**
     * Применяет масштаб к дереву компонентов
     * без накопления изменений.
     *
     * @param component корневой компонент
     */
    public void apply(Component component) {
        if (component instanceof JComponent c) {
            Style base = (Style) c.getClientProperty(STYLE);

            if (base == null) {
                Font originalFont = c.getFont();

                if (originalFont == null) {
                    originalFont = new Font(
                            Font.SANS_SERIF,
                            Font.PLAIN,
                            15
                    );
                }

                base = new Style(originalFont, c.getBorder());
                c.putClientProperty(STYLE, base);
            }

            c.setFont(font(base.font()));
            c.setBorder(border(base.border()));

            if (c instanceof AbstractButton button
                    && button.getText() != null
                    && !button.getText().isEmpty()) {

                button.setMargin(new Insets(
                        px(4), px(4), px(4), px(4)
                ));
            }

            if (c instanceof JScrollBar bar) {
                bar.setPreferredSize(
                        bar.getOrientation() == JScrollBar.VERTICAL
                                ? new Dimension(px(17), px(48))
                                : new Dimension(px(48), px(17))
                );
            }

            LayoutManager layout = c.getLayout();
            int[] gaps = (int[]) c.getClientProperty(GAPS);

            if (layout instanceof BorderLayout current) {
                if (gaps == null) {
                    gaps = new int[]{
                            current.getHgap(),
                            current.getVgap()
                    };
                }

                current.setHgap(px(gaps[0]));
                current.setVgap(px(gaps[1]));

            } else if (layout instanceof FlowLayout current) {
                if (gaps == null) {
                    gaps = new int[]{
                            current.getHgap(),
                            current.getVgap()
                    };
                }

                current.setHgap(px(gaps[0]));
                current.setVgap(px(gaps[1]));

            } else if (layout instanceof GridLayout current) {
                if (gaps == null) {
                    gaps = new int[]{
                            current.getHgap(),
                            current.getVgap()
                    };
                }

                current.setHgap(px(gaps[0]));
                current.setVgap(px(gaps[1]));
            }

            if (gaps != null) {
                c.putClientProperty(GAPS, gaps);
            }
        }

        if (component instanceof Container parent) {
            for (Component child : parent.getComponents()) {
                apply(child);
            }
        }
    }

    private Font font(Font original) {
        return original.deriveFont(
                (float) (Math.max(15, original.getSize2D()) * factor)
        );
    }

    private Border border(Border original) {
        if (original instanceof TitledBorder current) {
            Font titleFont = current.getTitleFont();

            if (titleFont == null) {
                titleFont = new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        15
                );
            }

            return BorderFactory.createTitledBorder(
                    border(current.getBorder()),
                    current.getTitle(),
                    current.getTitleJustification(),
                    current.getTitlePosition(),
                    font(titleFont),
                    current.getTitleColor()
            );
        }

        if (original instanceof EmptyBorder current) {
            Insets insets = current.getBorderInsets();

            return BorderFactory.createEmptyBorder(
                    px(insets.top),
                    px(insets.left),
                    px(insets.bottom),
                    px(insets.right)
            );
        }

        if (original instanceof LineBorder current) {
            return BorderFactory.createLineBorder(
                    current.getLineColor(),
                    Math.max(1, px(current.getThickness())),
                    current.getRoundedCorners()
            );
        }

        if (original instanceof CompoundBorder current) {
            return BorderFactory.createCompoundBorder(
                    border(current.getOutsideBorder()),
                    border(current.getInsideBorder())
            );
        }

        return original;
    }

    private boolean belongsToWindow(Window child) {
        for (Window owner = child.getOwner();
             owner != null;
             owner = owner.getOwner()) {

            if (owner == window) {
                return true;
            }
        }

        return false;
    }

    /**
     * Возвращает размещение карт с переносом строк.
     *
     * @return размещение по ширине области просмотра
     */
    public static FlowLayout wrappingCards() {
        return new FlowLayout(FlowLayout.LEFT, 6, 6) {
            @Override
            public Dimension preferredLayoutSize(Container target) {
                synchronized (target.getTreeLock()) {
                    int outside = 0;
                    Container ancestor = target.getParent();

                    while (ancestor != null
                            && !(ancestor instanceof JViewport)) {

                        Insets insets = ancestor.getInsets();
                        outside += insets.left + insets.right;
                        ancestor = ancestor.getParent();
                    }

                    int width = ancestor == null
                            ? target.getWidth()
                            : ancestor.getWidth() - outside;

                    if (width <= 0) {
                        width = 800;
                    }

                    Insets insets = target.getInsets();

                    int limit = Math.max(
                            1,
                            width - insets.left - insets.right
                                    - 2 * getHgap()
                    );

                    int rowWidth = 0;
                    int rowHeight = 0;
                    int totalHeight = 0;
                    int totalWidth = 0;

                    for (Component child : target.getComponents()) {
                        if (!child.isVisible()) {
                            continue;
                        }

                        Dimension size = child.getPreferredSize();
                        int gap = rowWidth == 0 ? 0 : getHgap();

                        if (rowWidth > 0
                                && rowWidth + gap + size.width > limit) {

                            totalWidth = Math.max(totalWidth, rowWidth);
                            totalHeight += rowHeight + getVgap();

                            rowWidth = 0;
                            rowHeight = 0;
                            gap = 0;
                        }

                        rowWidth += gap + size.width;
                        rowHeight = Math.max(rowHeight, size.height);
                    }

                    return new Dimension(
                            Math.max(totalWidth, rowWidth)
                                    + insets.left + insets.right
                                    + 2 * getHgap(),
                            totalHeight + rowHeight
                                    + insets.top + insets.bottom
                                    + 2 * getVgap()
                    );
                }
            }

            @Override
            public Dimension minimumLayoutSize(Container target) {
                return preferredLayoutSize(target);
            }
        };
    }

    /**
     * Останавливает обработку изменения размеров
     * и закрывает подписки на события.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;
        timer.stop();

        window.removeComponentListener(resizeListener);
        Toolkit.getDefaultToolkit().removeAWTEventListener(dialogs);
    }
}
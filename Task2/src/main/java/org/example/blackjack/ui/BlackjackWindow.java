package org.example.blackjack.ui;

import org.example.blackjack.console.ConsoleText;
import org.example.blackjack.console.EventFormatter;
import org.example.blackjack.model.*;
import org.example.blackjack.model.actions.*;
import org.example.blackjack.model.enums.*;
import org.example.blackjack.model.events.*;
import org.example.blackjack.model.exceptions.*;
import org.example.blackjack.model.view.HandView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Игровой стол Swing с изменяемым составом участников.
 * Все изменения интерфейса выполняются в потоке обработки событий Swing.
 *
 * @serial exclude
 */
public class BlackjackWindow extends JFrame {
    private static final Color FELT = new Color(28, 83, 62);
    private static final int MINIMUM_DECK_COUNT = 6;

    private final Random random;
    private final UiScale scaling;

    private JSplitPane bodySplit;
    private JSplitPane playSplit;
    private JScrollPane boardScroll;
    private JPanel actionButtons;

    private final List<Player> players = new ArrayList<>();
    private final Map<PlayerId, Money> bets = new LinkedHashMap<>();
    private final Map<HandKey, HandOutcome> results = new LinkedHashMap<>();

    private final DefaultTableModel rosterModel = new DefaultTableModel(
            new String[]{"ID", "Игрок", "Баланс", "Ставка"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable roster = new JTable(rosterModel);
    private final JPanel board = new JPanel();
    private final JTextArea journal = new JTextArea();
    private final JLabel status = new JLabel();
    private final JLabel shoeInfo = new JLabel();

    private final JButton start = new JButton("Начать раздачу");

    private final JButton add = new JButton("+");
    private final JButton remove = new JButton("−");
    private final JButton changeBet = new JButton("Ставка");
    private final JButton topUp = new JButton("Баланс");

    private final JButton hit = new JButton("Взять карту");
    private final JButton stand = new JButton("Остановиться");
    private final JButton doubleDown = new JButton("Удвоить");
    private final JButton split = new JButton("Разделить");
    private final JButton surrender = new JButton("Сдаться");

    private final JPanel insurancePanel =
            new JPanel(new BorderLayout(6, 4));

    private final JLabel insurancePrompt = new JLabel();
    private final JTextField insuranceAmount = new JTextField(8);
    private final JButton buyInsurance = new JButton("Купить страховку");
    private final JButton declineInsurance = new JButton("Отказаться");

    private Shoe shoe;
    private BlackjackRound round;
    private List<Player> roundPlayers = List.of();
    private EventFormatter formatter;
    private PlayerId shownInsurancePlayer;

    private long nextId = 100;
    private int roundNumber;
    private int eventCursor;
    private int insuranceIndex;

    private record HandKey(PlayerId playerId, int handIndex) {
    }

    /**
     * Создает стол с двумя начальными участниками.
     *
     * @param seed зерно общего генератора случайных чисел
     */
    public BlackjackWindow(long seed) {
        super("Блэкджек");

        random = new Random(seed);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));
        setSize(1200, 760);
        setLocationRelativeTo(null);

        scaling = new UiScale(this, this::applySizing);

        buildLayout(seed);
        bindActions();

        addPlayer("Вася", money("100", false), money("10", true));
        addPlayer("Петя", money("100", false), money("10", true));

        append("Зерно игры: " + seed);

        refresh();
        scaling.update();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (!isRoundActive()
                        || JOptionPane.showConfirmDialog(
                        BlackjackWindow.this,
                        "Раздача не завершена. Закрыть окно?",
                        "Выход",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
    }

    private void buildLayout(long seed) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
        setContentPane(root);

        JPanel heading = new JPanel(new BorderLayout(10, 4));

        JTextField seedText =
                new JTextField("Зерно: " + seed, 18);
        seedText.setEditable(false);

        heading.add(seedText, BorderLayout.WEST);
        heading.add(status, BorderLayout.CENTER);
        heading.add(scaling.control(), BorderLayout.EAST);
        heading.add(shoeInfo, BorderLayout.SOUTH);

        root.add(heading, BorderLayout.NORTH);

        roster.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        roster.setRowHeight(26);

        JPanel rosterPanel = new JPanel(new BorderLayout(6, 6));
        rosterPanel.setBorder(
                BorderFactory.createTitledBorder("Участники стола")
        );
        rosterPanel.add(
                new JScrollPane(roster), BorderLayout.CENTER
        );

        JPanel management = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 6, 4)
        );

        add.setToolTipText("Добавить игрока");
        remove.setToolTipText("Удалить выбранного игрока");
        changeBet.setToolTipText(
                "Изменить ставку выбранного игрока"
        );
        topUp.setToolTipText(
                "Пополнить баланс выбранного игрока"
        );

        for (JButton button : List.of(
                add, remove, changeBet, topUp)) {
            management.add(button);
        }

        rosterPanel.add(management, BorderLayout.SOUTH);

        board.setLayout(new BoxLayout(board, BoxLayout.Y_AXIS));
        board.setBackground(FELT);
        board.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        journal.setEditable(false);
        journal.setLineWrap(true);
        journal.setWrapStyleWord(true);
        journal.setFont(
                new Font(Font.MONOSPACED, Font.PLAIN, 12)
        );

        JScrollPane journalScroll = new JScrollPane(journal);
        journalScroll.setBorder(
                BorderFactory.createTitledBorder("Журнал раздач")
        );

        boardScroll = new JScrollPane(board);

        playSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                boardScroll,
                journalScroll
        );
        playSplit.setResizeWeight(0.76);

        bodySplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                rosterPanel,
                playSplit
        );
        bodySplit.setDividerLocation(360);

        root.add(bodySplit, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(6, 6));

        insurancePanel.add(
                insurancePrompt, BorderLayout.NORTH
        );

        JPanel insuranceChoices = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        insuranceChoices.add(insuranceAmount);
        insuranceChoices.add(buyInsurance);
        insuranceChoices.add(declineInsurance);

        insurancePanel.add(
                insuranceChoices, BorderLayout.CENTER
        );

        controls.add(insurancePanel, BorderLayout.NORTH);

        actionButtons = new JPanel(
                new GridLayout(0, 5, 6, 6)
        );

        for (JButton button : List.of(
                hit, stand, doubleDown, split, surrender)) {
            actionButtons.add(button);
        }

        controls.add(actionButtons, BorderLayout.CENTER);
        controls.add(start, BorderLayout.SOUTH);

        root.add(controls, BorderLayout.SOUTH);
    }

    private void applySizing() {
        int width = getContentPane().getWidth() > 0
                ? getContentPane().getWidth()
                : getWidth();

        bodySplit.setDividerSize(scaling.px(8));
        bodySplit.setDividerLocation(
                Math.min(
                        scaling.px(360),
                        (int) (width * 0.38)
                )
        );

        playSplit.setDividerSize(scaling.px(8));

        if (playSplit.getHeight() > 0) {
            playSplit.setDividerLocation(0.76);
        }

        int columns = Math.max(
                1,
                Math.min(5, width / scaling.px(160))
        );

        ((GridLayout) actionButtons.getLayout())
                .setColumns(columns);

        roster.setRowHeight(scaling.px(28));
        roster.getTableHeader().setPreferredSize(
                new Dimension(0, scaling.px(32))
        );

        getContentPane().revalidate();
        validate();
        refresh();
    }

    /**
     * Освобождает окно и обработчики масштабирования.
     */
    @Override
    public void dispose() {
        if (scaling != null) {
            scaling.close();
        }

        super.dispose();
    }

    private void bindActions() {
        add.addActionListener(
                e -> guarded(this::addPlayerDialog)
        );

        remove.addActionListener(e -> guarded(() -> {
            Player player = selectedPlayer();

            players.remove(player);
            bets.remove(player.getId());

            append(player.getName() + " покинул стол.");
        }));

        changeBet.addActionListener(e -> guarded(() -> {
            Player player = selectedPlayer();

            String input = JOptionPane.showInputDialog(
                    this,
                    "Новая ставка: " + player.getName(),
                    bets.get(player.getId()).amount().toPlainString()
            );

            if (input != null) {
                bets.put(player.getId(), money(input, true));
            }
        }));

        topUp.addActionListener(e -> guarded(() -> {
            Player player = selectedPlayer();

            String input = JOptionPane.showInputDialog(
                    this,
                    "Сумма пополнения: " + player.getName(),
                    "100"
            );

            if (input != null) {
                Money amount = money(input, true);
                player.credit(amount);

                append(
                        player.getName()
                                + ": пополнение "
                                + amount.amount().toPlainString()
                );
            }
        }));

        start.addActionListener(
                e -> guarded(this::beginRound)
        );

        hit.addActionListener(
                e -> guarded(() -> perform(ActionType.HIT))
        );
        stand.addActionListener(
                e -> guarded(() -> perform(ActionType.STAND))
        );
        doubleDown.addActionListener(
                e -> guarded(() -> perform(ActionType.DOUBLE))
        );
        split.addActionListener(
                e -> guarded(() -> perform(ActionType.SPLIT))
        );
        surrender.addActionListener(
                e -> guarded(() -> perform(ActionType.SURRENDER))
        );

        buyInsurance.addActionListener(e -> guarded(() -> {
            round.buyInsurance(
                    roundPlayers.get(insuranceIndex).getId(),
                    money(insuranceAmount.getText(), true)
            );
            insuranceIndex++;
        }));

        declineInsurance.addActionListener(e -> guarded(() -> {
            round.declineInsurance(
                    roundPlayers.get(insuranceIndex).getId()
            );
            insuranceIndex++;
        }));

        roster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateControls();
            }
        });
    }

    private void addPlayerDialog() {
        JTextField name = new JTextField(
                "Игрок " + (players.size() + 1)
        );
        JTextField balance = new JTextField("100");
        JTextField bet = new JTextField("10");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));

        form.add(new JLabel("Имя:"));
        form.add(name);

        form.add(new JLabel("Начальный баланс:"));
        form.add(balance);

        form.add(new JLabel("Ставка:"));
        form.add(bet);

        int answer = JOptionPane.showConfirmDialog(
                this,
                form,
                "Новый игрок",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (answer == JOptionPane.OK_OPTION) {
            String value = name.getText().trim();

            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "Введите имя игрока."
                );
            }

            addPlayer(
                    value,
                    money(balance.getText(), false),
                    money(bet.getText(), true)
            );

            append(value + " присоединился к столу.");
        }
    }

    private void addPlayer(String name, Money balance, Money bet) {
        Player player = new Player(
                new PlayerId(nextId),
                name,
                balance
        );

        nextId += 100;

        players.add(player);
        bets.put(player.getId(), bet);
    }

    private Player selectedPlayer() {
        int index = roster.getSelectedRow();

        if (index < 0 || index >= players.size()) {
            throw new IllegalArgumentException(
                    "Выберите игрока в таблице."
            );
        }

        return players.get(index);
    }

    private void beginRound() {
        List<Player> participants = new ArrayList<>();

        for (Player player : players) {
            if (player.getBalance().amount()
                    .compareTo(bets.get(player.getId()).amount()) >= 0) {
                participants.add(player);
            } else {
                append(
                        player.getName()
                                + " пропускает раздачу: недостаточно денег."
                );
            }
        }

        if (participants.isEmpty()) {
            throw new IllegalArgumentException(
                    "Некому сделать ставку. Пополните баланс."
            );
        }

        int reserve = BlackjackRound.requiredCardReserve(
                participants.size()
        );

        Shoe prepared = Shoe.prepareForRound(
                shoe,
                reserve,
                MINIMUM_DECK_COUNT,
                random
        );

        if (prepared != shoe) {
            append(
                    "Подготовлен башмак: "
                            + prepared.getDeckCount()
                            + " колод."
            );
        }

        shoe = prepared;
        roundPlayers = List.copyOf(participants);
        round = new BlackjackRound(roundPlayers, shoe);

        Map<PlayerId, String> names = new LinkedHashMap<>();

        for (Player player : roundPlayers) {
            names.put(player.getId(), player.getName());
        }

        formatter = new EventFormatter(names);
        eventCursor = 0;
        insuranceIndex = 0;
        shownInsurancePlayer = null;
        results.clear();

        append("\n=== Раздача " + (++roundNumber) + " ===");

        for (Player player : roundPlayers) {
            round.placeBet(
                    player.getId(),
                    bets.get(player.getId())
            );
        }

        round.start();
    }

    private void perform(ActionType type) {
        PlayerId id = round.getCurrentPlayerId();
        int index = round.getCurrentHandIndex();

        GameAction action = switch (type) {
            case HIT -> new HitAction(id, index);
            case STAND -> new StandAction(id, index);
            case DOUBLE -> new DoubleAction(id, index);
            case SPLIT -> new SplitAction(id, index);
            case SURRENDER -> new SurrenderAction(id, index);
        };

        action.execute(round);
    }

    private void guarded(Runnable operation) {
        try {
            operation.run();
            publishEvents();

            if (round != null) {
                while (round.getPhase() == RoundPhase.DEALER_TURN
                        || round.getPhase() == RoundPhase.SETTLEMENT) {

                    if (round.getPhase() == RoundPhase.DEALER_TURN) {
                        round.playDealer();
                    } else {
                        round.settle();
                    }

                    publishEvents();
                }
            }
        } catch (InsufficientFundsException e) {
            error("Недостаточно денег для этого действия.");
        } catch (IllegalMoveException e) {
            error("Это действие сейчас запрещено правилами.");
        } catch (ShoeEmptyException e) {
            error(
                    "В башмаке закончились карты. "
                            + "Раздача не завершена."
            );
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }

        refresh();
    }

    private void publishEvents() {
        if (round == null) {
            return;
        }

        List<GameEvent> events = round.getEvents();

        while (eventCursor < events.size()) {
            GameEvent event = events.get(eventCursor++);
            append(formatter.format(event));

            if (event instanceof HandSettledEvent settled) {
                results.put(
                        new HandKey(
                                settled.playerId(),
                                settled.handIndex()
                        ),
                        settled.outcome()
                );
            }
        }
    }

    private void refresh() {
        int selected = roster.getSelectedRow();
        rosterModel.setRowCount(0);

        for (Player player : players) {
            rosterModel.addRow(new Object[]{
                    player.getId().value(),
                    player.getName(),
                    player.getBalance().amount().toPlainString(),
                    bets.get(player.getId()).amount().toPlainString()
            });
        }

        if (!players.isEmpty()) {
            int index = Math.max(
                    0,
                    Math.min(selected, players.size() - 1)
            );

            if (round != null
                    && round.getPhase() == RoundPhase.PLAYER_TURNS) {

                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).getId()
                            .equals(round.getCurrentPlayerId())) {
                        index = i;
                    }
                }
            }

            roster.setRowSelectionInterval(index, index);
        }

        board.removeAll();
        JPanel activeSection = null;

        if (round == null) {
            board.add(whiteLabel(
                    "Настройте участников и нажмите «Начать раздачу»."
            ));
        } else {
            String dealer = round.isDealerRevealed()
                    ? "Дилер — очки: " + round.getDealerTotal()
                    : "Дилер — вторая карта закрыта";

            board.add(cardSection(
                    dealer,
                    round.getVisibleDealerCards(),
                    !round.isDealerRevealed()
            ));

            for (Player player : roundPlayers) {
                List<HandView> hands = round.getHandViews(
                        player.getId()
                );

                for (int i = 0; i < hands.size(); i++) {
                    HandView hand = hands.get(i);

                    boolean current =
                            round.getPhase() == RoundPhase.PLAYER_TURNS
                                    && player.getId()
                                    .equals(round.getCurrentPlayerId())
                                    && i == round.getCurrentHandIndex();

                    HandOutcome outcome = results.get(
                            new HandKey(player.getId(), i)
                    );

                    String title = (current ? "▶ " : "")
                            + player.getName()
                            + " (ID: " + player.getId().value() + ")"
                            + ", рука " + (i + 1)
                            + " — " + hand.total() + " очков"
                            + ", ставка "
                            + hand.bet().amount().toPlainString()
                            + ", "
                            + (outcome == null
                            ? ConsoleText.status(hand.status())
                            : ConsoleText.outcome(outcome));

                    JPanel section = cardSection(
                            title,
                            hand.cards(),
                            false
                    );

                    board.add(section);

                    if (current) {
                        activeSection = section;
                    }
                }
            }
        }

        scaling.apply(getContentPane());

        board.revalidate();
        board.repaint();

        if (activeSection != null) {
            JPanel visible = activeSection;

            SwingUtilities.invokeLater(() -> {
                if (visible.getParent() == board) {
                    Rectangle target = visible.getBounds();
                    Dimension extent =
                            boardScroll.getViewport().getExtentSize();

                    target.width = Math.min(
                            target.width, extent.width
                    );
                    target.height = Math.min(
                            target.height, extent.height
                    );

                    board.scrollRectToVisible(target);
                }
            });
        }

        shoeInfo.setText(
                shoe == null
                        ? "Башмак будет подготовлен перед раздачей."
                        : "Башмак: " + shoe.getDeckCount()
                        + " колод, осталось карт: "
                        + shoe.remainingCards()
        );

        updateControls();
    }

    private void updateControls() {
        boolean active = isRoundActive();
        boolean selected = roster.getSelectedRow() >= 0;

        add.setEnabled(!active);
        remove.setEnabled(!active && selected);
        changeBet.setEnabled(!active && selected);
        topUp.setEnabled(!active && selected);

        boolean canBet = false;

        for (Player player : players) {
            if (player.getBalance().amount()
                    .compareTo(bets.get(player.getId()).amount()) >= 0) {
                canBet = true;
            }
        }

        start.setEnabled(!active && canBet);
        start.setText(
                round == null
                        ? "Начать раздачу"
                        : "Следующая раздача"
        );

        boolean turn = round != null
                && round.getPhase() == RoundPhase.PLAYER_TURNS;

        Set<ActionType> available = round == null
                ? Set.of()
                : round.getAvailableActions();

        hit.setEnabled(available.contains(ActionType.HIT));
        stand.setEnabled(available.contains(ActionType.STAND));
        doubleDown.setEnabled(available.contains(ActionType.DOUBLE));
        split.setEnabled(available.contains(ActionType.SPLIT));
        surrender.setEnabled(available.contains(ActionType.SURRENDER));

        if (turn) {
            PlayerId id = round.getCurrentPlayerId();
            Player player = roundPlayers.get(0);

            for (Player candidate : roundPlayers) {
                if (candidate.getId().equals(id)) {
                    player = candidate;
                }
            }

            status.setText(
                    "Ходит " + player.getName()
                            + ", рука "
                            + (round.getCurrentHandIndex() + 1)
            );
        } else {
            status.setText(
                    round == null
                            ? "Подготовка стола"
                            : ConsoleText.phase(round.getPhase())
            );
        }

        boolean insurance = round != null
                && round.getPhase() == RoundPhase.INSURANCE;

        insurancePanel.setVisible(insurance);

        if (insurance) {
            Player player = roundPlayers.get(insuranceIndex);

            Money maximum = round.getHandViews(player.getId())
                    .getFirst().bet()
                    .multiply(new BigDecimal("0.5"));

            insurancePrompt.setText(
                    player.getName()
                            + ": страховка до "
                            + maximum.amount().toPlainString()
            );

            if (!player.getId().equals(shownInsurancePlayer)) {
                insuranceAmount.setText(
                        maximum.amount().toPlainString()
                );
                shownInsurancePlayer = player.getId();
            }

            buyInsurance.setEnabled(
                    player.getBalance().amount().signum() > 0
            );
        }
    }

    private boolean isRoundActive() {
        return round != null
                && round.getPhase() != RoundPhase.FINISHED;
    }

    private JPanel cardSection(
            String title, List<Card> cards, boolean hidden) {

        JPanel section = new JPanel(new BorderLayout(0, 6)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(
                        Integer.MAX_VALUE,
                        getPreferredSize().height
                );
            }
        };

        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 14, 0)
        );

        section.add(whiteLabel(title), BorderLayout.NORTH);

        JPanel row = new JPanel(UiScale.wrappingCards());
        row.setOpaque(false);

        for (Card card : cards) {
            row.add(cardFace(card));
        }

        if (hidden) {
            row.add(cardFace(null));
        }

        section.add(row, BorderLayout.CENTER);
        return section;
    }

    private JLabel cardFace(Card card) {
        JLabel label = new JLabel("?", SwingConstants.CENTER);

        label.setPreferredSize(new Dimension(
                scaling.px(80),
                scaling.px(112)
        ));

        label.setOpaque(true);
        label.setFont(
                new Font(Font.SERIF, Font.BOLD, 32)
        );

        label.setBorder(
                BorderFactory.createLineBorder(
                        new Color(190, 190, 190),
                        1,
                        true
                )
        );

        label.setBackground(
                card == null
                        ? new Color(37, 56, 110)
                        : Color.WHITE
        );
        label.setForeground(Color.WHITE);

        if (card != null) {
            String rank = switch (card.rank()) {
                case ACE -> "Т";
                case KING -> "К";
                case QUEEN -> "Д";
                case JACK -> "В";
                default -> Integer.toString(
                        card.rank().getValue()
                );
            };

            String suit = switch (card.suit()) {
                case SPADES -> "♠";
                case HEARTS -> "♥";
                case DIAMONDS -> "♦";
                case CLUBS -> "♣";
            };

            label.setText(
                    "<html><center>"
                            + rank + "<br>" + suit
                            + "</center></html>"
            );

            label.setForeground(
                    card.suit() == Suit.HEARTS
                            || card.suit() == Suit.DIAMONDS
                            ? new Color(185, 30, 45)
                            : new Color(25, 25, 25)
            );

            label.setToolTipText(
                    ConsoleText.cards(List.of(card))
            );
        }

        return label;
    }

    private JTextArea whiteLabel(String text) {
        int width = boardScroll == null
                ? getWidth() - scaling.px(420)
                : boardScroll.getViewport().getWidth()
                - scaling.px(40);

        JTextArea label = new JTextArea(text);

        label.setEditable(false);
        label.setFocusable(false);
        label.setOpaque(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setForeground(Color.WHITE);

        label.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 15)
        );

        label.setSize(
                Math.max(200, width),
                Short.MAX_VALUE
        );

        return label;
    }

    private Money money(String text, boolean positive) {
        try {
            Money value = new Money(
                    new BigDecimal(text.trim().replace(',', '.'))
            );

            if (positive && value.amount().signum() == 0) {
                throw new IllegalArgumentException();
            }

            return value;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    positive
                            ? "Введите сумму больше нуля."
                            : "Введите неотрицательную сумму."
            );
        }
    }

    private void append(String text) {
        journal.append(text + System.lineSeparator());
        journal.setCaretPosition(
                journal.getDocument().getLength()
        );
    }

    private void error(String text) {
        JOptionPane.showMessageDialog(
                this,
                text,
                "Не удалось выполнить действие",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
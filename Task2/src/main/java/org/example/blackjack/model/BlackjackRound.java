    package org.example.blackjack.model;

    import org.example.blackjack.model.enums.HandOutcome;
    import org.example.blackjack.model.enums.HandStatus;
    import org.example.blackjack.model.enums.Rank;
    import org.example.blackjack.model.enums.RoundPhase;
    import org.example.blackjack.model.exceptions.IllegalMoveException;
    import org.example.blackjack.model.exceptions.InsufficientFundsException;
    import org.example.blackjack.model.exceptions.ShoeEmptyException;

    import java.math.BigDecimal;
    import java.util.ArrayList;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;

    /**
     * Раздача блэкджека.
     * Хранит участников в порядке посадки, их руки,
     * башмак, руку дилера и текущую фазу раздачи.
     */
    public class BlackjackRound {
        private final List<Player> players;
        private final Map<PlayerId, List<RoundHand>> hands = new LinkedHashMap<>();
        private final Shoe shoe;
        private final Hand dealerHand = new Hand();
        private RoundPhase phase = RoundPhase.BETTING;
        private final HandEvaluator evaluator = new HandEvaluator();
        private int currentPlayerIndex = -1;
        private int currentHandIndex = -1;

        /**
         * Создает раздачу в фазе приема ставок.
         *
         * @param players участники в порядке посадки
         * @param shoe башмак для выдачи карт
         * @throws IllegalArgumentException если список игроков null или пустой, башмак null, список содержит null или повторяющиеся идентификаторы
         */
        public BlackjackRound(List<Player> players, Shoe shoe) {
            if (players == null) {
                throw new IllegalArgumentException("players must not be null");
            }
            if (players.isEmpty()) {
                throw new IllegalArgumentException("players must not be empty");
            }
            if (shoe == null) {
                throw new IllegalArgumentException("shoe must not be null");
            }

            for (Player player : players) {
                if (player == null) {
                    throw new IllegalArgumentException("player must not be null");
                }

                PlayerId id = player.getId();

                if (hands.containsKey(id)) {
                    throw new IllegalArgumentException("player ids must be unique");
                }

                hands.put(id, new ArrayList<>());
            }

            this.players = List.copyOf(players);
            this.shoe = shoe;
        }

        /**
         * Принимает ставку игрока и создает его первую руку.
         *
         * @param playerId идентификатор игрока
         * @param bet ставка игрока
         * @throws IllegalArgumentException если идентификатор или ставка null, либо ставка не больше нуля
         * @throws IllegalMoveException если прием ставок завершен, игрок не участвует в раздаче или уже сделал ставку
         * @throws InsufficientFundsException если игроку не хватает денег
         */
        public void placeBet(PlayerId playerId, Money bet){
            if(playerId == null){
                throw new IllegalArgumentException("player id must not be null");
            }
            if (bet == null){
                throw new IllegalArgumentException("bet must not be null");
            }
            if (!phase.equals(RoundPhase.BETTING)){
                throw new IllegalMoveException("phase must be 'BETTING'");
            }
            Player player = findPlayer(playerId);

            List<RoundHand> playerHands = hands.get(playerId);

            if (!playerHands.isEmpty()) {
                throw new IllegalMoveException("bet has already been placed");
            }
            RoundHand roundHand = new RoundHand(bet);
            player.debit(bet);
            playerHands.add(roundHand);
        }

        /**
         * Начинает раздачу, выдает начальные карты и определяет следующую фазу.
         * @throws IllegalMoveException если раздача уже началась, не все участники сделали ставки или карт недостаточно
         */
        public void start(){
            validateInitialDeal();
            phase = RoundPhase.INITIAL_DEAL;
            dealInitialCards();
            choosePhaseAfterInitialDeal();
        }

        /**
         * Выдает карту текущей руке и обновляет очередь.
         *
         * @param playerId идентификатор игрока
         * @param handIndex индекс руки, начиная с нуля
         * @throws IllegalArgumentException если идентификатор null
         *                                  или индекс отрицательный
         * @throws IllegalMoveException если добор этой рукой сейчас запрещен
         * @throws ShoeEmptyException если в башмаке закончились карты
         */
        public void hit(PlayerId playerId, int handIndex){
            RoundHand currentHand = validateCurrentHand(playerId, handIndex);
            currentHand.addCard(shoe.draw());
            advanceTurn();
        }

        /**
         * Останавливает текущую руку и обновляет очередь.
         *
         * @param playerId идентификатор игрока
         * @param handIndex индекс руки, начиная с нуля
         * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
         * @throws IllegalMoveException если остановка этой рукой сейчас запрещена
         */
        public void stand(PlayerId playerId, int handIndex){
            RoundHand currentHand = validateCurrentHand(playerId, handIndex);
            currentHand.stand();
            advanceTurn();
        }

        /**
         * Сдает текущую руку и обновляет очередь.
         *
         * @param playerId идентификатор игрока
         * @param handIndex индекс руки, начиная с нуля
         * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
         * @throws IllegalMoveException если сдача этой рукой сейчас запрещена
         */
        public void surrender(PlayerId playerId, int handIndex){
            RoundHand currentHand = validateCurrentHand(playerId, handIndex);
            currentHand.surrender();
            advanceTurn();
        }

        /**
         * Списывает дополнительную ставку, удваивает ставку руки
         * и выдает ей одну последнюю карту.
         *
         * @param playerId идентификатор игрока
         * @param handIndex индекс руки, начиная с нуля
         * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
         * @throws IllegalMoveException если удвоение этой рукой сейчас запрещено
         * @throws InsufficientFundsException если не хватает денег
         * @throws ShoeEmptyException если башмак пуст
         */
        public void doubleDown(PlayerId playerId, int handIndex){
            RoundHand currentHand = validateCurrentHand(playerId, handIndex);
            if (currentHand.getCards().size() != 2){
                throw new IllegalMoveException("hand must have exactly two cards");
            }
            if (shoe.remainingCards() == 0){
                throw new ShoeEmptyException("shoe is empty");
            }
            Player currentPlayer = findPlayer(playerId);
            currentPlayer.debit(currentHand.getBet());
            currentHand.doubleDown(shoe.draw());
            advanceTurn();
        }

        /**
         * Разделяет текущую пару на две руки с одинаковыми ставками.
         * Списывает дополнительную ставку и выдает каждой руке по карте.
         * После разделения тузов обе руки останавливаются.
         *
         * @param playerId идентификатор игрока
         * @param handIndex индекс разделяемой руки
         * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
         * @throws IllegalMoveException если разделение сейчас запрещено, достигнут лимит рук или недостаточно карт
         * @throws InsufficientFundsException если не хватает денег
         */
        public void split(PlayerId playerId, int handIndex){
            RoundHand currentHand = validateCurrentHand(playerId, handIndex);

            if (currentHand.getCards().size() != 2){
                throw new IllegalMoveException("hand must have exactly two cards");
            }
            if (currentHand.getCards().getFirst().rank() != currentHand.getCards().getLast().rank()){
                throw new IllegalMoveException("ranks must be same");
            }

            List<RoundHand> roundHandList = hands.get(playerId);

            if (roundHandList.size() >= 4){
                throw new IllegalMoveException("maximum of four hands per player");
            }
            if (shoe.remainingCards() < 2){
                throw new IllegalMoveException("not enough cards for split");
            }

            RoundHand roundHandFirst = new RoundHand(currentHand.getBet(), true);
            RoundHand roundHandSecond = new RoundHand(currentHand.getBet(), true);

            roundHandFirst.addCard(currentHand.getCards().getFirst());
            roundHandSecond.addCard(currentHand.getCards().getLast());

            findPlayer(playerId).debit(currentHand.getBet());

            roundHandFirst.addCard(shoe.draw());
            roundHandSecond.addCard(shoe.draw());

            if (currentHand.getCards().getFirst().rank() == Rank.ACE) {
                if (!roundHandFirst.getStatus().isFinished()) {
                    roundHandFirst.stand();
                }
                if (!roundHandSecond.getStatus().isFinished()) {
                    roundHandSecond.stand();
                }
            }

            roundHandList.set(handIndex, roundHandFirst);
            roundHandList.add(handIndex + 1, roundHandSecond);
            advanceTurn();
        }

        /**
         * Выполняет ход дилера и переводит раздачу к расчетам.
         * Дилер добирает до 17, включая остановку на мягких 17.
         * Если сравнивать руки не требуется, карты не добираются.
         *
         * @throws IllegalMoveException если сейчас не ход дилера
         * @throws ShoeEmptyException если при доборе закончились карты
         */
        public void playDealer(){
            if (phase != RoundPhase.DEALER_TURN){
                throw new IllegalMoveException("phase must be 'DEALER_TURN'");
            }
            if (hasHandsToCompare()){
                while (evaluator.evaluate(dealerHand) < 17){
                    dealerHand.addCard(shoe.draw());
                }
            }
            phase = RoundPhase.SETTLEMENT;
        }

        /**
         * Начисляет выплаты по всем рукам и завершает раздачу.
         * Нулевые выплаты пропускаются.
         *
         * @throws IllegalMoveException если сейчас не фаза расчетов или расчет уже выполнен
         */
        public void settle(){
            if (phase != RoundPhase.SETTLEMENT){
                throw new IllegalMoveException("phase must be 'SETTLEMENT'");
            }
            for (Player player: players){
                for (RoundHand roundHand: hands.get(player.getId())){
                    HandOutcome outcome = determineOutcome(roundHand);
                    Money payout = outcome.payout(roundHand.getBet());
                    if (payout.amount().compareTo(BigDecimal.ZERO) > 0){
                        player.credit(payout);
                    }
                }
            }
            phase = RoundPhase.FINISHED;
        }


        /**
         * Возвращает идентификатор игрока, который сейчас ходит.
         *
         * @return идентификатор текущего игрока
         * @throws IllegalMoveException если сейчас не фаза действий игроков
         */
        public PlayerId getCurrentPlayerId(){
            if (phase != RoundPhase.PLAYER_TURNS){
                throw new IllegalMoveException("phase must be 'PLAYER_TURNS'");
            }
            return players.get(currentPlayerIndex).getId();
        }

        /**
         * Возвращает индекс текущей руки игрока.
         * Индексация начинается с нуля.
         *
         * @return индекс текущей руки
         * @throws IllegalMoveException если сейчас не фаза действий игроков
         */
        public int getCurrentHandIndex(){
            if (phase != RoundPhase.PLAYER_TURNS){
                throw new IllegalMoveException("phase must be 'PLAYER_TURNS'");
            }
            return currentHandIndex;
        }


        /**
         * Возвращает текущую фазу раздачи.
         *
         * @return текущая фаза
         */
        public RoundPhase getPhase() {
            return phase;
        }

        /**
         * Возвращает неизменяемый список идентификаторов
         * игроков в порядке посадки.
         *
         * @return идентификаторы участников
         */
        public List<PlayerId> getPlayerIds() {
            return List.copyOf(hands.keySet());
        }

        private Player findPlayer(PlayerId playerId){
            for (Player player: players){
                if (player.getId().equals(playerId)){
                    return player;
                }
            }
            throw new IllegalMoveException("player did not find");
        }

        private void validateInitialDeal(){
            if (!phase.equals(RoundPhase.BETTING)){
                throw new IllegalMoveException("phase must be 'BETTING'");
            }
            for (Player player: players){
                if(hands.get(player.getId()).size() != 1){
                    throw new IllegalMoveException("each player must have exactly one hand");
                }
            }
            if (2 * (players.size() + 1) > shoe.remainingCards()){
                throw new IllegalMoveException("not enough cards for initial deal");
            }
        }

        private void dealInitialCards(){
            for (int i = 0; i < 2; i++) {
                for (Player player : players) {
                    RoundHand playerHand = hands.get(player.getId()).getFirst();
                    playerHand.addCard(shoe.draw());
                }
                dealerHand.addCard(shoe.draw());
            }
        }

        private boolean dealerHasBlackjack(){
            return dealerHand.getCards().size() == 2 && evaluator.evaluate(dealerHand) == 21;
        }

        private boolean selectFirstActiveHand(){
            currentPlayerIndex = -1;
            currentHandIndex = -1;
            for (int i = 0; i < players.size(); i++) {
                List<RoundHand> roundHands = hands.get(players.get(i).getId());
                for (int j = 0; j < roundHands.size(); j++) {
                    if (!roundHands.get(j).getStatus().isFinished()){
                        currentPlayerIndex = i;
                        currentHandIndex = j;
                        return true;
                    }
                }
            }
            return false;
        }

        private void choosePhaseAfterInitialDeal(){
            if (Rank.ACE == dealerHand.getCards().getFirst().rank()){
                phase = RoundPhase.INSURANCE;
            } else if (dealerHasBlackjack()) {
                phase = RoundPhase.SETTLEMENT;
            } else {
                if (selectFirstActiveHand()){
                    phase = RoundPhase.PLAYER_TURNS;
                } else {
                    phase = RoundPhase.SETTLEMENT;
                }
            }
        }

        private RoundHand validateCurrentHand(PlayerId playerId, int handIndex){
            if (playerId == null){
                throw new IllegalArgumentException("player id must not be null");
            }
            if (handIndex < 0){
                throw new IllegalArgumentException("hand index must be non-negative");
            }
            if (phase != RoundPhase.PLAYER_TURNS){
                throw new IllegalMoveException("phase must be 'PLAYER_TURNS'");
            }
            if (!playerId.equals(getCurrentPlayerId())){
                throw new IllegalMoveException("player id incorrect");
            }
            if (handIndex != getCurrentHandIndex()){
                throw new IllegalMoveException("hand index incorrect");
            }
            RoundHand currentHand = hands.get(playerId).get(handIndex);

            if (currentHand.getStatus().isFinished()) {
                throw new IllegalMoveException("hand is already finished");
            }

            return currentHand;
        }

        private void advanceTurn(){
            if (selectFirstActiveHand()){
                phase = RoundPhase.PLAYER_TURNS;
            } else {
                phase = RoundPhase.DEALER_TURN;
            }
        }
        private boolean hasHandsToCompare(){
            for (List<RoundHand> roundHandLists: hands.values()){
                for (RoundHand roundHand: roundHandLists){
                    if (roundHand.getStatus() == HandStatus.STOOD && !roundHand.isBlackjack()){
                        return true;
                    }
                }
            }
            return false;
        }

        private HandOutcome determineOutcome(RoundHand hand){
            if (hand.getStatus() == HandStatus.BUST){
                return HandOutcome.LOSS;
            } else if (dealerHasBlackjack()) {
                if (hand.isBlackjack()){
                    return HandOutcome.PUSH;
                } else {
                    return HandOutcome.LOSS;
                }
            } else if (hand.getStatus() == HandStatus.SURRENDERED) {
                return HandOutcome.SURRENDER;
            } else if (hand.isBlackjack()) {
                return HandOutcome.BLACKJACK;
            } else if (evaluator.evaluate(dealerHand) > 21) {
                return HandOutcome.WIN;
            } else {
                if (hand.getTotal() > evaluator.evaluate(dealerHand)){
                    return HandOutcome.WIN;
                } else if (hand.getTotal() < evaluator.evaluate(dealerHand)) {
                    return HandOutcome.LOSS;
                } else {
                    return HandOutcome.PUSH;
                }
            }
        }
    }
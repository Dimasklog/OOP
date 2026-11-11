package org.example.blackjack.model;

import java.math.BigDecimal;
import org.example.blackjack.model.exceptions.InsufficientFundsException;

/**
 * Игрок.
 */
public class Player {
    private final PlayerId id;
    private final String name;
    private Money balance;

    /**
     * Создает игрока.
     * @param id идентификационный номер игрока
     * @param name имя игрока
     * @param balance счет игрока
     * @throws IllegalArgumentException если {@code id == null} или {@code name == null} или {@code name.isBlank()} или {@code balance == null}
     */
    public Player(PlayerId id, String name, Money balance){
        if (id == null){
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null){
            throw new IllegalArgumentException("name must not be null");
        }
        if (name.isBlank()){
            throw new IllegalArgumentException("name must not be blank");
        }
        if (balance == null){
            throw new IllegalArgumentException("balance must not be null");
        }
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    /**
     * Вычитание со счета.
     * @param amount вычитаемая сумма
     * @throws IllegalArgumentException если {@code amount == null} или {@code amount.amount().compareTo(BigDecimal.ZERO) <= 0}
     * @throws InsufficientFundsException если {@code balance.amount().compareTo(amount.amount()) < 0}
     */
    public void debit(Money amount){
        if (amount == null){
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("amount must be positive");
        }
        if (balance.amount().compareTo(amount.amount()) < 0){
            throw new InsufficientFundsException("insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    /**
     * Добавляет на счет игрока сумму.
     * @param amount добавляемая сумма
     * @throws IllegalArgumentException если {@code amount == null} или {@code amount.amount().compareTo(BigDecimal.ZERO) <= 0}
     */
    public void credit(Money amount){
        if (amount == null){
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("amount must be positive");
        }
        balance = balance.add(amount);
    }

    /**
     * Возвращает идентификационный номер игрока.
     * @return идентификационный номер игрока
     */
    public PlayerId getId() {
        return id;
    }

    /**
     * Возвращает имя игрока.
     * @return имя игрока
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает баланс игрока.
     * @return баланс игрока
     */
    public Money getBalance() {
        return balance;
    }
}

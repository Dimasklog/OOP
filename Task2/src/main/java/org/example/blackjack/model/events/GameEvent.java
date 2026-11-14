package org.example.blackjack.model.events;

/**
 * Общий тип событий раздачи.
 */
public sealed interface GameEvent permits BetPlacedEvent, PhaseChangedEvent, PlayerActionEvent {
}
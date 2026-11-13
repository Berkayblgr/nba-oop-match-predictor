package nba.iterator;

import nba.model.Player;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom iterator implementation for traversing a list of {@link Player} instances sequentially.
 * Enforces the Iterator design pattern without exposing the internal roster data structure.
 */
public class PlayerIterator implements Iterator<Player> {

    private final List<Player> players;
    private int currentIndex;

    /**
     * Constructs a new {@code PlayerIterator} with defensive null protection.
     * @param players the list of players to iterate over
     * @throws IllegalArgumentException if {@code players} is null
     */
    public PlayerIterator(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("players cannot be null");
        }
        this.players = List.copyOf(players);
        this.currentIndex = 0;
    }

    /**
     * Checks if there are more players left in the iteration sequence.
     * @return {@code true} if the iteration has more elements; {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return currentIndex < players.size();
    }

    /**
     * Returns the next player in the iteration.
     * @return the next {@link Player}
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Player next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more players in roster");
        }
        Player player = players.get(currentIndex);
        currentIndex++;
        return player;
    }
}
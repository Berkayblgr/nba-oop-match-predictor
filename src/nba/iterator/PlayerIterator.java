package nba.iterator;

import nba.model.Player;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PlayerIterator implements Iterator<Player> {

    private final List<Player> players;
    private int currentIndex;

    public PlayerIterator(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("players cannot be null");
        }
        this.players = List.copyOf(players);
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < players.size();
    }

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

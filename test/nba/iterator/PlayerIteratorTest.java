package nba.iterator;

import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

import nba.model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class PlayerIteratorTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException when player list is null")
    void shouldThrowExceptionWhenPlayerListIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerIterator(null));
    }


    @Test
    @DisplayName("Should return false for hasNext when player list is empty")
    void shouldReturnFalseForHasNextWhenListIsEmpty() {
        PlayerIterator playerIterator = new PlayerIterator(List.of());
        assertFalse(playerIterator.hasNext());
    }



    @Test
    @DisplayName("Should iterate over players sequentially in correct order")
    void shouldIterateOverPlayersInSequentialOrder() {
        Player player1 = new Player("Luka Doncic", 27, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 46.0);
        Player player2 = new Player("Derrick Rose", 27, "G", 30.0, 10.0, 10.0, 2.0, 3.0, 2.0, 46.0);

        PlayerIterator playerIterator = new PlayerIterator(List.of(player1, player2));

        assertTrue(playerIterator.hasNext());
        assertEquals(player1, playerIterator.next());
        assertTrue(playerIterator.hasNext());
        assertEquals(player2, playerIterator.next());
        assertFalse(playerIterator.hasNext());
    }



    @Test
    @DisplayName("Should throw NoSuchElementException when next is called on empty or exhausted iterator")
    void shouldThrowNoSuchElementExceptionWhenNextCalledPastEnd() {
        PlayerIterator playerIterator = new PlayerIterator(List.of());
        assertThrows(NoSuchElementException.class, () -> playerIterator.next());
    }

}
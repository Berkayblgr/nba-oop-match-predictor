package nba.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player createDefaultPlayer() {
        return new Player("Luka Doncic", 27, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 46.0);
    }

    @Test
    @DisplayName("Should create Player with valid stats and return correct getter values")
    void shouldCreatePlayerWithValidStats() {
        // Arrange
        Player player = createDefaultPlayer();

        // Assert
        assertEquals("Luka Doncic", player.getName());
        assertEquals(27, player.getAge());
        assertEquals("G", player.getPosition());
        assertEquals(20.0, player.getPpg());
        assertEquals(10.0, player.getRpg());
        assertEquals(10.0, player.getApg());
        assertEquals(2.0, player.getSpg());
        assertEquals(1.0, player.getBpg());
        assertEquals(2.0, player.getTopg());
        assertEquals(46.0, player.getFgPct());
    }

    @Test
    @DisplayName("Should calculate exact base impact score when FG% is at league average (46.0%)")
    void shouldCalculateBaseImpactScoreAtLeagueAverageFgPct() {
        // Arrange
        Player player = createDefaultPlayer();

        // Act
        double actualImpact = player.calculateImpactScore();

        // Assert
        assertEquals(49.0, actualImpact, 0.00001);
    }

    @Test
    @DisplayName("Should boost impact score for high FG% and penalize for low FG%")
    void shouldAdjustImpactScoreBasedOnShootingEfficiency() {
        // Arrange
        Player efficientPlayer = new Player("Tyrese Maxey", 25, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 56.0);
        Player averagePlayer = createDefaultPlayer();
        Player inefficientPlayer = new Player("Dennis Schöder", 33, "G",  20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 36.0);

        // Act
        double efficentFgPct = efficientPlayer.calculateImpactScore();
        double averageFgPct = averagePlayer.calculateImpactScore();
        double inefficientFgPct = inefficientPlayer.calculateImpactScore();

        // Assert
        assertTrue(efficentFgPct > averageFgPct);
        assertTrue(averageFgPct > inefficientFgPct);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when any box-score stat is negative")
    void shouldThrowExceptionWhenStatIsNegative() {
        // Arrange
        Player player = createDefaultPlayer();
        assertThrows(IllegalArgumentException.class, () -> player.setPpg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> player.setRpg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> player.setApg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> player.setSpg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> player.setBpg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> player.setTopg(-1.0));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when FG% is below 0 or above 100")
    void shouldThrowExceptionWhenFgPctIsOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new Player("Victor Wembanyama", 22, "C", 20, 10, 10, 2, 1, 2, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new Player("Victor Wembanyama", 22, "C", 20, 10, 10, 2, 1, 2, 101.0));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when position is null or blank")
    void shouldThrowExceptionWhenPositionIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Player("Cooper Flag", 20, null, 20, 10, 10, 2, 1, 2, 46 ));
        assertThrows(IllegalArgumentException.class, () -> new Player("Cooper Flag", 20, "", 20, 10, 10, 2, 1, 2, 46 ));
    }

    @Test
    @DisplayName("Should format role summary with position and calculated impact score")
    void shouldReturnFormattedRoleSummary() {
        // Arrange
        Player player = createDefaultPlayer();

        // Act
        String roleSummary = player.getRoleSummary();

        // Assert
        assertNotNull(roleSummary);
        assertTrue(roleSummary.startsWith("Player G"));
        assertTrue(roleSummary.contains("Impact Score:"));
    }

    @Test
    @DisplayName("Should format toString with all player statistics and impact score")
    void shouldReturnFormattedToString() {
        Player player = createDefaultPlayer();
        String str = player.toString();

        assertNotNull(str);
        assertTrue(str.contains("Luka Doncic"));
        assertTrue(str.contains("Pos: G"));
        assertTrue(str.contains("PPG:"));
        assertTrue(str.contains("FG%:"));
        assertTrue(str.contains("Impact:"));
    }

}






















package nba.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduledMatchTest {

    @Test
    @DisplayName("Should create ScheduledMatch with valid data, trim and convert team codes to uppercase")
    void shouldCreateScheduledMatchAndNormalizeTeamCodes() {
        ScheduledMatch scheduledMatch = new ScheduledMatch("lal", "hou", "Scheduled");

        assertEquals("LAL", scheduledMatch.homeTeamCode());
        assertEquals("HOU", scheduledMatch.awayTeamCode());
        assertEquals("Scheduled", scheduledMatch.matchStatus());
    }



    @Test
    @DisplayName("Should default matchStatus to 'Scheduled' when status is null or blank")
    void shouldDefaultMatchStatusToScheduledWhenNullOrBlank() {
        ScheduledMatch scheduledMatch1 = new ScheduledMatch("LAL", "HOU", null);
        ScheduledMatch scheduledMatch2 = new ScheduledMatch("SAS", "NYK", "  ");

        assertEquals("Scheduled", scheduledMatch1.matchStatus());
        assertEquals("Scheduled", scheduledMatch2.matchStatus());
    }



    @Test
    @DisplayName("Should throw IllegalArgumentException when homeTeamCode is null or blank")
    void shouldThrowExceptionWhenHomeTeamCodeIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new ScheduledMatch(null, "HOU", "Scheduled"));
        assertThrows(IllegalArgumentException.class, () -> new ScheduledMatch("  ", "BOS", "Scheduled"));
    }



    @Test
    @DisplayName("Should throw IllegalArgumentException when awayTeamCode is null or blank")
    void shouldThrowExceptionWhenAwayTeamCodeIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new ScheduledMatch("LAL", null, "Scheduled"));
        assertThrows(IllegalArgumentException.class, () -> new ScheduledMatch("BOS", "  ", "Scheduled"));
    }



    @Test
    @DisplayName("Should consider two ScheduledMatch records equal when all components match")
    void shouldBeEqualWhenAllComponentsMatch() {
        ScheduledMatch scheduledMatch1 = new ScheduledMatch("LAL", "HOU", "Scheduled");
        ScheduledMatch scheduledMatch2 = new ScheduledMatch("LAL", "HOU", "Scheduled");
        ScheduledMatch diffMatch = new ScheduledMatch("SAS", "BOS", "Scheduled");

        assertEquals(scheduledMatch1, scheduledMatch2);
        assertEquals(scheduledMatch1.hashCode(), scheduledMatch2.hashCode());
        assertNotEquals(scheduledMatch1, diffMatch);
    }
}
package nba.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoachTest {

    @Test
    @DisplayName("Should create Coach with valid data and return correct getter values")
    void shouldCreateCoachWithValidData() {
        Coach coach = new Coach("Steve Kerr", 59, "Motion Offense", 8.5);

        assertEquals("Steve Kerr", coach.getName());
        assertEquals(59, coach.getAge());
        assertEquals("Motion Offense", coach.getTacticalStyle());
        assertEquals(8.5, coach.getTacticalBonus());
    }



    @Test
    @DisplayName("Should throw IllegalArgumentException when tactical style is null or blank")
    void shouldThrowIllegalArgumentExceptionWhenTacticalStyleIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Coach("Steve Kerr",59,null,8.5));
        assertThrows(IllegalArgumentException.class, () -> new Coach("Steve Kerr",59,"  ",8.5));
    }



    @Test
    @DisplayName("Should throw IllegalArgumentException when tactical bonus is out of bounds (0.0 - 15.0")
    void shouldThrowIllegalArgumentExceptionWhenTacticalBonusIsOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new Coach("Steve Kerr",59,"Motion Offense", -1));
        assertThrows(IllegalArgumentException.class, () -> new Coach("Steve Kerr",59,"Motion Offense", 15.1));
    }



    @Test
    @DisplayName("Should accept exact boundary values for tactical bonus (0.0 and 15.0)")
    void shouldAcceptExactBoundaryValuesForTacticalBonus() {
        Coach coach1 = new Coach("Steve Kerr", 59, "Motion Offense", 0.0);
        Coach coach2 = new Coach("Steve Kerr", 59, "Motion Offense", 15.0);

        assertEquals(0, coach1.getTacticalBonus(), 0.0001);
        assertEquals(15, coach2.getTacticalBonus(), 0.0001);
    }



    @Test
    @DisplayName("Should format role summary with tactical style and bonus")
    void shouldReturnFormattedRoleSummary() {
        Coach coach = new Coach("Steve Kerr", 59, "Motion Offense", 8.5);

        String roleSummary = coach.getRoleSummary();

        assertNotNull(roleSummary);
        assertTrue(roleSummary.startsWith("Head Coach: "));
        assertTrue(roleSummary.contains("Tactical Bonus: "));
    }



    @Test
    @DisplayName("Should format toString with coach details and tactical style")
    void shouldReturnFormattedToString() {
        Coach coach = new Coach("Steve Kerr", 59, "Motion Offense", 8.5);

        String coachDetails = coach.toString();

        assertNotNull(coachDetails);
        assertTrue(coachDetails.contains("Steve Kerr"));
        assertTrue(coachDetails.contains("Motion Offense"));
        assertTrue(coachDetails.contains("Bonus: "));
    }
}
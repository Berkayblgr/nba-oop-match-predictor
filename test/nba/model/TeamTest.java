package nba.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class TeamTest {

    private Coach createDefaultCoach() {
        return new Coach("Steve Kerr", 59, "Motion Offense", 5.0);
    }
    private Team createDefaultTeam() {
        return new Team("Golden State Warriors", "GSW", createDefaultCoach());
    }

    @Test
    @DisplayName("Should create Team with valid data and return correct initial state")
    void shouldCreateTeamWithValidData() {
        Team team = createDefaultTeam();

        assertEquals("Golden State Warriors", team.getName());
        assertEquals("GSW", team.getAbbreviation());
        assertEquals("Steve Kerr", team.getCoach().getName());
        assertEquals(0, team.getRosterSize());
        assertEquals(0, team.getWins());
        assertEquals(0, team.getLosses());
    }



    @Test
    @DisplayName("Should trim and convert abbreviation to uppercase")
    void shouldTrimAndUppercaseAbbreviation() {
        Coach coach = createDefaultCoach();
        Team team = new Team("Golden State Warriors", "  gsw  ", coach);

        assertEquals("GSW", team.getAbbreviation());
    }



    @Test
    @DisplayName("Should throw IllegalArgumentException when name, abbreviation or coach is invalid")
    void shouldThrowExceptionWhenConstructorArgsAreInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Team(null, "GSW", createDefaultCoach()));
        assertThrows(IllegalArgumentException.class, () -> new Team("Golden State Warriors", " ",  createDefaultCoach()));
        assertThrows(IllegalArgumentException.class, () -> new Team("Golden State Warriors", "GSW", null));
    }



    @Test
    @DisplayName("Should add player to roster and increase roster size")
    void shouldAddPlayerToRoster() {
        Player player = new Player("Luka Doncic", 27, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 46.0);
        Team team = createDefaultTeam();

        team.addPlayer(player);

        assertEquals(1, team.getRosterSize());
        assertTrue(team.getPlayers().contains(player));
    }



    @Test
    @DisplayName("Should throw IllegalArgumentException when adding null or duplicate player")
    void shouldThrowExceptionWhenAddingNullOrDuplicatePlayer() {
        Player player = new Player("Luka Doncic", 27, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 46.0);
        Team team = createDefaultTeam();

        team.addPlayer(player);

        assertThrows(IllegalArgumentException.class, () -> team.addPlayer(null));
        assertThrows(IllegalArgumentException.class, () -> team.addPlayer(player));

    }



    @Test
    @DisplayName("Should prevent external modification of roster via defensive copy (List.copyOf)")
    void shouldPreventExternalModificationOfRoster() {
        Player player = new Player("Luka Doncic", 27, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 46.0);
        Team team = createDefaultTeam();

        assertThrows(UnsupportedOperationException.class, () -> team.getPlayers().add(player));
    }



    @Test
    @DisplayName("Should return 0.0 team power when roster is empty")
    void shouldReturnZeroPowerWhenRosterIsEmpty() {
        Team team = createDefaultTeam();
        assertEquals(0.0, team.calculateTeamPower(), 0.0001);
    }



    @Test
    @DisplayName("Should calculate team power including player rotation, coach bonus and record form")
    void shouldCalculateTeamPowerCorrectly() {
        Player player = new Player("Luka Doncic", 27, "G", 20.0, 10.0, 10.0, 2.0, 1.0, 2.0, 46.0);
        Team team = createDefaultTeam();

        team.addPlayer(player);
        team.setRecord(5, 5);
        double actualTeamPower = team.calculateTeamPower();

        assertEquals(54.0, actualTeamPower, 0.0001);
    }











}
package nba.model;

/**
 * Immutable data carrier representing a scheduled NBA matchup extracted from the daily scoreboard.
 * @param homeTeamCode  three-letter code of the host team (e.g., "LAL")
 * @param awayTeamCode  three-letter code of the visiting team (e.g., "BOS")
 * @param matchStatus   scheduled tip-off time or current match status
 */
public record ScheduledMatch(String homeTeamCode, String awayTeamCode, String matchStatus) {

    /**
     * Compact constructor validating that team codes are not blank.
     */
    public ScheduledMatch {
        if (homeTeamCode == null || homeTeamCode.isBlank()) {
            throw new IllegalArgumentException("homeTeamCode must not be null or blank");
        }
        if (awayTeamCode == null || awayTeamCode.isBlank()) {
            throw new IllegalArgumentException("awayTeamCode must not be null or blank");
        }
        if (matchStatus == null || matchStatus.isBlank()) {
            matchStatus = "Scheduled";
        }
        homeTeamCode = homeTeamCode.trim().toUpperCase();
        awayTeamCode = awayTeamCode.trim().toUpperCase();
    }
}

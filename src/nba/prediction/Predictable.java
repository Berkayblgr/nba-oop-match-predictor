package nba.prediction;

import nba.model.Team;

/**
 * Strategy interface defining the contract for NBA prediction engines.
 * Enables polymorphic simulation under varying game contexts (e.g., Regular Season vs. Playoffs).
 */
public interface Predictable {

    /**
     * Simulates a matchup between a home team and an away team and predicts the winner.
     * @param homeTeam the team hosting the game on its home court
     * @param awayTeam the visiting team
     * @return an immutable {@link MatchPredictionResult} holding the winner, win probability, and power breakdown
     * @throws IllegalArgumentException if either team is null, empty, or if a team is matched against itself
     */
    MatchPredictionResult predictWinner(Team homeTeam, Team awayTeam);

    /**
     * Returns the human-readable display name of this prediction strategy.
     * @return display name of predictor
     */
    String getPredictorName();
}

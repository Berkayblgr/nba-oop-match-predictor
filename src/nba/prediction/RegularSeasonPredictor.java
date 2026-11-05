package nba.prediction;

import nba.model.Team;

/**
 * Regular season prediction engine implementing {@link Predictable}.
 * Incorporates a standard home-court rating bonus and calculates win probability using a Pythagorean expectation exponent to produce realistic separation.
 */
public class RegularSeasonPredictor implements Predictable{

    private static final double HOME_COURT_BONUS = 2.2;

    @Override
    public String getPredictorName() {
        return "Regular Season Predictor";
    }

    /**
     * Predicts the match outcome by applying standard regular season home-court advantage
     * and computing win probabilities via Pythagorean expectation (exponent 2.8).
     * @param homeTeam the team hosting the game on its home court
     * @param awayTeam the visiting team
     * @return the prediction outcome
     * @throws IllegalArgumentException if teams are null, empty or identical
     */
    @Override
    public MatchPredictionResult predictWinner(Team homeTeam, Team awayTeam) {
        if (homeTeam == null || awayTeam == null) {
            throw new IllegalArgumentException("Home team or away team is null");
        }
        if (homeTeam.getRosterSize() == 0 || awayTeam.getRosterSize() == 0) {
            throw new IllegalArgumentException("Home team or away team is empty");
        }
        if (homeTeam.equals(awayTeam)) {
            throw new IllegalArgumentException("A team cannot play against itself");
        }

        double homeScore = Math.max(1.0, homeTeam.calculateTeamPower() + HOME_COURT_BONUS);
        double awayScore = Math.max(1.0,  awayTeam.calculateTeamPower());

        double homeExp = Math.pow(homeScore, 2.8);
        double awayExp = Math.pow(awayScore, 2.8);
        double totalExp = homeExp + awayExp;
        Team winner;
        double winProbability;

        if (homeScore >= awayScore) {
            winner = homeTeam;
            winProbability = (homeExp / totalExp) * 100.0;
        } else {
            winner = awayTeam;
            winProbability = (awayExp / totalExp) * 100.0;
        }

        String summary = String.format("%s (Home, +%.1f Court Bonus): %.2f rating vs %s (Away): %.2f rating",
                homeTeam.getAbbreviation(), HOME_COURT_BONUS, homeScore, awayTeam.getAbbreviation(), awayScore);

        return new MatchPredictionResult(winner, winProbability, summary);
    }
}
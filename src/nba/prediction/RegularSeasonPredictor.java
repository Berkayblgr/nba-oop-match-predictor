package nba.prediction;

import nba.model.Team;

public class RegularSeasonPredictor implements Predictable{

    private static final double HOME_COURT_BONUS = 2.2;

    @Override
    public String getPredictorName() {
        return "Regular Season Predictor";
    }

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
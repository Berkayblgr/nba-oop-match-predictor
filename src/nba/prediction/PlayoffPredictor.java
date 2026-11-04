package nba.prediction;

import nba.model.Team;
import nba.model.Player;

public class PlayoffPredictor implements Predictable{

    private static final double HOME_COURT_PLAYOFF_BONUS = 3.2;
    private static final double STAR_PLAYER_WEIGHT = 0.35;
    private static final double COACH_PLAYOFF_WEIGHT = 0.50;

    @Override
    public String getPredictorName() {
        return "Playoff Predictor";
    }

    private double calculatePlayoffPower(Team team) {
        double maxStarImpact = 0.0;
        for (Player player : team) {
            double impact = player.calculateImpactScore();
            if (impact > maxStarImpact) {
                maxStarImpact = impact;
            }
        }
        return team.calculateTeamPower() + (maxStarImpact * STAR_PLAYER_WEIGHT) +
                (team.getCoach().getTacticalBonus() * COACH_PLAYOFF_WEIGHT);
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

        double homeScore = Math.max(1.0, calculatePlayoffPower(homeTeam) + HOME_COURT_PLAYOFF_BONUS);
        double awayScore = Math.max(1.0,  calculatePlayoffPower(awayTeam));

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

        String summary = String.format("%s (Home, +Playoff Star/Coach/Court Bonus): %.2f rating vs %s (Away, +Star/Coach Bonus): %.2f rating",
                homeTeam.getAbbreviation(), homeScore, awayTeam.getAbbreviation(), awayScore);

        return new MatchPredictionResult(winner, winProbability, summary);
    }
}

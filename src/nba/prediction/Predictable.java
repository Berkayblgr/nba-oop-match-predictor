package nba.prediction;

import nba.model.Team;

public interface Predictable {

    MatchPredictionResult predictWinner(Team homeTeam, Team awayTeam);
    String getPredictorName();
}

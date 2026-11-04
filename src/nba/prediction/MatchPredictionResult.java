package nba.prediction;

import nba.model.Team;

public class MatchPredictionResult {

    private final Team predictedWinner;
    private final double winProbability;
    private final String analysisSummary;

    public MatchPredictionResult(Team predictedWinner, double winProbability, String analysisSummary) {
        if (predictedWinner == null) {
            throw new IllegalArgumentException("predictedWinner is null");
        }
        if (winProbability < 0.0 || winProbability > 100.0 || Double.isNaN(winProbability)) {
            throw new IllegalArgumentException("winProbability is out of range");
        }
        if (analysisSummary == null || analysisSummary.isBlank()) {
            throw new IllegalArgumentException("analysisSummary is null or empty");
        }
        this.predictedWinner = predictedWinner;
        this.winProbability = winProbability;
        this.analysisSummary = analysisSummary;
    }

    public Team getPredictedWinner() {
        return predictedWinner;
    }
    public double getWinProbability() {
        return winProbability;
    }
    public String getAnalysisSummary() {
        return analysisSummary;
    }

    @Override
    public String toString() {
        return String.format("Predicted Winner : %s [%s] (Win Probability: %.1f%%)%nPower Breakdown  : %s",
                predictedWinner.getName(), predictedWinner.getAbbreviation(), winProbability, analysisSummary);    }
}
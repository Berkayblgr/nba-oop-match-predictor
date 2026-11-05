package nba.prediction;

import nba.model.Team;

/**
 * Immutable value object encapsulating the outcome of a simulated NBA match prediction.
 */
public class MatchPredictionResult {

    private final Team predictedWinner;
    private final double winProbability;
    private final String analysisSummary;

    /**
     * Constructs a new immutable prediction result with validation checks.
     * @param predictedWinner   the predicted winning team
     * @param winProbability    the calculated win probability percentage (between 0.0 and 100.0)
     * @param analysisSummary   textual breakdown of home vs. away ratings and applied bonuses
     * @throws IllegalArgumentException if predictedWinner or analysisSummary is invalid, or winProbability is out of range
     */
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
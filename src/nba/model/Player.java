package nba.model;

/**
 * Represents an active NBA basketball player with per-game box-score statistics
 * and advanced impact score calculations.
 */
public class Player extends Person {

    private String position;
    private double ppg;
    private double rpg;
    private double apg;
    private double spg;
    private double bpg;
    private double topg;
    private double fgPct;

    private static final double PPG_WEIGHT = 1.0;
    private static final double RPG_WEIGHT = 1.2;
    private static final double APG_WEIGHT = 1.5;
    private static final double SPG_WEIGHT = 2.0;
    private static final double BPG_WEIGHT = 1.6;
    private static final double TOP_WEIGHT = 1.8;
    private static final double LEAGUE_AVG_FG_PCT = 46.0;

    /**
     * Constructs a new Player with comprehensive statistics and validations.
     * @param name      the player's full name
     * @param age       the player's age
     * @param position  the player's primary position (e.g., G, F, C)
     * @param ppg       points per game
     * @param rpg       rebounds per game
     * @param apg       assists per game
     * @param spg       steals per game
     * @param bpg       blocks per game
     * @param topg      turnovers per game
     * @param fgPct     field goal shooting percentage (0.0 to 100.0)
     * @throws IllegalArgumentException if any statistical value is invalid
     */
    public Player(String name, int age, String position, double ppg,
                  double rpg, double apg, double spg, double bpg, double topg, double fgPct) {

        super (name, age);
        setPosition(position);
        setPpg(ppg);
        setRpg(rpg);
        setApg(apg);
        setSpg(spg);
        setBpg(bpg);
        setTopg(topg);
        setFgPct(fgPct);
    }

    public String getPosition() {
        return position;
    }
    public double getPpg() {
        return ppg;
    }
    public double getRpg() {
        return rpg;
    }
    public double getApg() {
        return apg;
    }
    public double getSpg() {
        return spg;
    }
    public double getBpg() {
        return bpg;
    }
    public double getTopg() {
        return topg;
    }
    public double getFgPct() {
        return fgPct;
    }

    /**
     * Validates that a numeric player statistic is non-negative and a valid number.
     * @param value     the numeric statistical value to validate
     * @param statName  the human-readable label of the metric used in error messages
     * @throws IllegalArgumentException if {@code value} is negative or {@link Double#isNaN(double)}
     */
    private void validateStat(double value, String statName) {
        if (value < 0 || Double.isNaN(value)) {
            throw new IllegalArgumentException(statName + " cannot be negative: " + value);
        }
    }

    public final void setPosition(String position) {
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("Invalid position");
        }
        this.position = position;
    }
    public final void setPpg(double ppg) {
        validateStat(ppg, "Ppg");
        this.ppg = ppg;
    }
    public final void setRpg(double rpg) {
        validateStat(rpg, "Rpg");
        this.rpg = rpg;
    }
    public final void setApg(double apg) {
        validateStat(apg, "Apg");
        this.apg = apg;
    }
    public final void setSpg(double spg) {
        validateStat(spg, "Spg");
        this.spg = spg;
    }
    public final void setBpg(double bpg) {
        validateStat(bpg, "Bpg");
        this.bpg = bpg;
    }
    public final void setTopg(double topg) {
        validateStat(topg, "Topg");
        this.topg = topg;
    }
    public final void setFgPct(double fgPct) {
        if (fgPct < 0.0 || fgPct > 100.0 || Double.isNaN(fgPct)) {
            throw new IllegalArgumentException("FG% must be between 0.0 and 100.0: " + fgPct);
        }
        this.fgPct = fgPct;
    }

    /**
     * Calculates the player's holistic impact rating using weighted box-score metrics
     * adjusted by shooting efficiency multiplier relative to NBA league average (46.0%).
     * @return calculated player impact score
     */
    public double calculateImpactScore() {
        double baseImpact = PPG_WEIGHT * ppg + RPG_WEIGHT * rpg + APG_WEIGHT * apg
                + SPG_WEIGHT * spg + BPG_WEIGHT * bpg - TOP_WEIGHT * topg;
        double efficiencyMultiplier = 1.0 + ((fgPct - LEAGUE_AVG_FG_PCT) / 200.0);
        return baseImpact * efficiencyMultiplier;
    }

    @Override
    public String getRoleSummary() {
        return String.format("Player %s | Impact Score: %.2f", position, calculateImpactScore());
    }

    @Override
    public String toString() {
        return String.format("%s | Pos: %s | PPG: %.1f | FG%%: %.1f%% | RPG: %.1f | APG: %.1f | SPG: %.1f | BPG: %.1f | TOPG: %.1f | Impact: %.2f",
                super.toString(), position, ppg, fgPct, rpg, apg, spg, bpg, topg, calculateImpactScore());
    }
}
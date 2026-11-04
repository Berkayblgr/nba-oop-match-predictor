package nba.model;

public class Coach extends Person {

    private static final double MIN_TACTICAL_BONUS = 0.0;
    private static final double MAX_TACTICAL_BONUS = 15.0;

    private String tacticalStyle;
    private double tacticalBonus;

    public Coach(String name, int age, String tacticalStyle,
                 double tacticalBonus) {

        super(name, age);
        setTacticalStyle(tacticalStyle);
        setTacticalBonus(tacticalBonus);
    }

    public String getTacticalStyle() {
        return tacticalStyle;
    }
    public double getTacticalBonus() {
        return tacticalBonus;
    }

    public final void setTacticalStyle(String tacticalStyle) {
        if (tacticalStyle == null || tacticalStyle.isBlank()) {
            throw new IllegalArgumentException("TacticalStyle is null or empty");
        }
        this.tacticalStyle = tacticalStyle;
    }

    public final void setTacticalBonus(double tacticalBonus) {
        if (tacticalBonus < MIN_TACTICAL_BONUS || tacticalBonus > MAX_TACTICAL_BONUS) {
            throw new IllegalArgumentException("TacticalBonus is out of range");
        }
        this.tacticalBonus = tacticalBonus;
    }

    @Override
    public String getRoleSummary() {
        return String.format("Head Coach: %s | Tactical Bonus: +%.1f", tacticalStyle, tacticalBonus);
    }
    @Override
    public String toString() {
        return String.format("%s | Style: %s | Bonus: +%.1f", super.toString(), tacticalStyle, tacticalBonus);
    }
}






package nba.model;

import nba.iterator.PlayerIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Comparator;

public class Team implements Iterable<Player> {

    private String name;
    private String abbreviation;
    private Coach coach;
    private final List<Player> roster;
    private int wins;
    private int losses;

    public Team(String name, String abbreviation, Coach coach) {
        setName(name);
        setAbbreviation(abbreviation);
        setCoach(coach);
        this.roster = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public String getAbbreviation() {
        return abbreviation;
    }
    public Coach getCoach() {
        return coach;
    }
    public List<Player> getPlayers() {
        return List.copyOf(roster);
    }
    public int getRosterSize() {
        return roster.size();
    }
    public int getWins() {
        return wins;
    }
    public int getLosses() {
        return losses;
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be null or blank");
        }
        this.name = name;
    }
    public final void setAbbreviation(String abbreviation) {
        if (abbreviation == null || abbreviation.isBlank()) {
            throw new IllegalArgumentException("Team abbreviation cannot be null or blank");
        }
        this.abbreviation = abbreviation.trim().toUpperCase();
    }
    public final void setCoach(Coach coach) {
        if (coach == null) {
            throw new IllegalArgumentException("Coach cannot be null");
        }
        this.coach = coach;
    }
    public void setRecord(int wins, int losses) {
        if (wins >= 0 && losses >= 0) {
            this.wins = wins;
            this.losses = losses;
        }
    }


    public void addPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        } else if (roster.contains(player)) {
            throw new IllegalArgumentException("Player is already in the roster: " + player.getName());
        }
        roster.add(player);
    }

    @Override
    public Iterator<Player> iterator() {
        return new PlayerIterator(roster);
    }

    public double calculateTeamPower() {
        if (roster.isEmpty()) {
            return 0.0;
        }
        List<Player> sorted = new ArrayList<>();
        for (Player player : this) {
            sorted.add(player);
        }
        sorted.sort(Comparator.comparingDouble(Player::calculateImpactScore).reversed());

        int rotationSize = Math.min(8, sorted.size());
        double weightedImpact = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < rotationSize; i++) {
            double weight = (i < 5) ? 1.2 : 0.6;
            weightedImpact += sorted.get(i).calculateImpactScore() * weight;
            totalWeight += weight;
        }
        double recordBonus = 0.0;
        int totalGames = wins + losses;
        if (totalGames > 0) {
            double winPct = (double) wins / totalGames;
            recordBonus = (winPct - 0.50) * 14.0; // Ör: 6-2 (%75) -> +3.5 bonus | 2-6 (%25) -> -3.5 ceza
        }
        return (weightedImpact / totalWeight) + coach.getTacticalBonus() + recordBonus;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] (%d-%d) | Coach: %s | Players: %d | Power: %.2f",
                name, abbreviation, wins, losses, coach.getName(), roster.size(), calculateTeamPower());
    }

    @Override
    public boolean equals(Object other) {
        // Identitaet
        if (this == other)
            return true;
        // null
        if (other == null)
            return false;
        // Typvergliech
        if (getClass() != other.getClass())
            return false;
        // Team-Cast
        Team otherTeam = (Team) other;
        // Attribute vergleichen
        if (!name.equals(otherTeam.name))
            return false;
        if (!abbreviation.equals(otherTeam.abbreviation))
            return false;
        // Objekte sind gleich
        return true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, abbreviation);
    }
}




















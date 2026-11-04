package nba.ui;

import nba.model.Player;
import nba.model.Team;
import nba.network.NbaApiClient;
import nba.prediction.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final NbaApiClient apiClient;
    private final Scanner scanner;

    public ConsoleMenu(NbaApiClient apiClient) {
        if (apiClient == null) {
            throw new IllegalArgumentException("apiClient cannot be null");
        }
        this.apiClient = apiClient;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== NBA 2026-27 OOP ANALYZER & MATCH PREDICTOR ===");
            System.out.println("[1] List Supported NBA Teams");
            System.out.println("[2] Inspect Team Roster & Coach");
            System.out.println("[3] Player Impact Score Ranking");
            System.out.println("[4] Simulate Match Prediction");
            System.out.println("[0] Exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listTeams();
                case "2" -> inspectRoster();
                case "3" -> rankPlayersByImpact();
                case "4" -> predictMatch();
                case "0" -> {
                    System.out.println("Thank you for using NBA 2026-27. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice! Please enter 0, 1, 2, 3 or 4.");
            }
        }
    }

    private Team promptTeam(String prompt) {
        System.out.print(prompt);
        String code = scanner.nextLine().trim().toUpperCase();
        if (!apiClient.isSupportedTeam(code)) {
            System.out.println("Unknown team code: "  + code);
            return null;
        } else {
            System.out.println("Loading live 2026-27 roster for " + code + "...");
            return apiClient.fetchTeam(code);
        }
    }

    private void listTeams() {
        System.out.println("\n--- Supported NBA Teams ---");
        for (String info : apiClient.getSupportedTeamsSummary()) {
            System.out.println(info);
        }
    }

    private void inspectRoster() {
        Team team = promptTeam("Enter team code (e.g. LAL, BOS, HOU): ");
        if (team == null) {
            return;
        }

        System.out.println("\n" + team);
        System.out.println("Coach -> " + team.getCoach().getName() + " | " + team.getCoach().getRoleSummary());
        for (Player player : team) {
            System.out.println("  - " + player);
        }
    }

    private void rankPlayersByImpact() {
        Team team = promptTeam("Enter team code to rank players (e.g. LAL, BOS, HOU): ");
        if (team == null) {
            return;
        }

        List<Player> sortedPlayers = new ArrayList<>(team.getPlayers());
        sortedPlayers.sort(Comparator.comparingDouble(Player::calculateImpactScore).reversed());

        System.out.println("\n--- Impact Score Ranking: " + team.getName() + " ---");
        int rank = 1;
        for (Player player : sortedPlayers) {
            System.out.printf("%2d. %-24s | %s%n", rank++, player.getName(), player.getRoleSummary());
        }
    }

    private void predictMatch() {
        Team hometeam = promptTeam("Enter home team code to predict (e.g. LAL, BOS, HOU): ");
        if (hometeam == null) {
            return;
        }
        Team awayteam = promptTeam("Enter away team code to predict (e.g. LAL, BOS, HOU): ");
        if (awayteam == null) {
            return;
        }

        if (hometeam.equals(awayteam)) {
            System.out.println("A team cannot play against itself!");
            return;
        }

        System.out.print("Select Mode ([1] Regular Season, [2] Playoff): ");
        String mode = scanner.nextLine().trim();

        Predictable predictor;
        if (mode.equals("2")) {
            predictor = new PlayoffPredictor();
        } else {
            predictor = new RegularSeasonPredictor();
        }

        MatchPredictionResult result = predictor.predictWinner(hometeam, awayteam);
        System.out.println("\n=== " + predictor.getPredictorName().toUpperCase() + " ===");
        System.out.println(result);
    }

}
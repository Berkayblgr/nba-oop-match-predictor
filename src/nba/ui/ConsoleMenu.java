package nba.ui;

import nba.model.ScheduledMatch;
import nba.model.Player;
import nba.model.Team;
import nba.network.NbaApiClient;
import nba.prediction.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive text-based console interface driving user interactions,
 * team inspections, player rankings, and polymorphic match simulations.
 */
public class ConsoleMenu {

    private final NbaApiClient apiClient;
    private final Scanner scanner;

    /**
     * Constructs a new ConsoleMenu bound to an API client instance.
     * @param apiClient the network client used for fetching live NBA data
     * @throws IllegalArgumentException if apiClient is null
     */
    public ConsoleMenu(NbaApiClient apiClient) {
        if (apiClient == null) {
            throw new IllegalArgumentException("apiClient cannot be null");
        }
        this.apiClient = apiClient;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main menu event loop, processing user selections until exit.
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== NBA 2026-27 OOP ANALYZER & MATCH PREDICTOR ===");
            System.out.println("[1] List Supported NBA Teams");
            System.out.println("[2] Inspect Team Roster & Coach");
            System.out.println("[3] Player Impact Score Ranking");
            System.out.println("[4] Simulate Match Prediction");
            System.out.println("[5] Predict Tonight's NBA Games");
            System.out.println("[0] Exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listTeams();
                case "2" -> inspectRoster();
                case "3" -> rankPlayersByImpact();
                case "4" -> predictMatch();
                case "5" -> predictTonightGames();
                case "0" -> {
                    System.out.println("Thank you for using NBA 2026-27. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice! Please enter 0, 1, 2, 3, 4 or 5.");
            }
        }
    }

    /**
     * Prompts the user to enter an NBA team code and fetches the populated team object.
     * @param prompt display message prompting input
     * @return populated {@link Team} instance or null if the code is invalid
     */
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

    /**
     * Displays all 30 supported NBA franchises.
     */
    private void listTeams() {
        System.out.println("\n--- Supported NBA Teams ---");
        for (String info : apiClient.getSupportedTeamsSummary()) {
            System.out.println(info);
        }
    }

    /**
     * Inspects a selected franchise's head coach and prints every player
     * via the custom iterator implemented on {@link Team}.
     */
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

    /**
     * Ranks all active players on a team by their calculated impact score in descending order.
     */
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

    /**
     * Guides the user through a matchup simulation, allowing selection between
     * regular season and playoff prediction strategies polymorphically.
     */
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

    /**
     * Fetches today's scheduled NBA matchups and performs batch match predictions
     * displaying tip-off times converted to Berlin local time (CET/CEST).
     */
    private void predictTonightGames() {
        System.out.println("\n=== TONIGHT'S NBA MATCHUPS & PREDICTIONS (BERLIN TIME) ===");
        List<ScheduledMatch> matches = apiClient.fetchTonightMatches();
        int matchNumber = 1;
        Predictable predictor;
        predictor = new RegularSeasonPredictor();
        for (ScheduledMatch match : matches) {
            Team home = apiClient.fetchTeam(match.homeTeamCode());
            Team away = apiClient.fetchTeam(match.awayTeamCode());
            MatchPredictionResult result = predictor.predictWinner(home, away);

            System.out.printf("%nMatch #%d: %s @ %s | Tip-off: %s%n",
                    matchNumber++, match.awayTeamCode(), match.homeTeamCode(), match.matchStatus());
            System.out.println(result);
        }
    }
}
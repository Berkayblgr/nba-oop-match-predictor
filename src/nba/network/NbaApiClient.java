package nba.network;

import nba.model.ScheduledMatch;
import nba.model.Coach;
import nba.model.Player;
import nba.model.Team;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous HTTP client layer managing network communication with ESPN's public NBA endpoints.
 * Handles concurrent athlete fetching via {@link CompletableFuture}, in-memory team caching,
 * and graceful fallback to offline rosters if live endpoints are unreachable.
 */
public class NbaApiClient {

    private static final String ROSTER_URL =
            "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/teams/%s/roster";
    private static final String OVERVIEW_URL =
            "https://site.web.api.espn.com/apis/common/v3/sports/basketball/nba/athletes/%s/overview";
    private static final String SCOREBOARD_URL =
            "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard";

    private record TeamMeta(String espnId, String fullName, String coachStyle, double coachBonus) {
    }

    private final HttpClient httpClient;
    private final Map<String, TeamMeta> teamCatalog;
    private final Map<String, Team> teamCache;

    /**
     * Initializes the client with an 8-second connect timeout and populates the 30-team NBA catalog.
     */
    public NbaApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.teamCatalog = new LinkedHashMap<>();
        this.teamCache = new HashMap<>();
        initCatalog();
    }

    private void initCatalog() {
        teamCatalog.put("ATL", new TeamMeta("1", "Atlanta Hawks", "Pace & Space", 5.5));
        teamCatalog.put("BOS", new TeamMeta("2", "Boston Celtics", "Two-Way Elite", 9.5));
        teamCatalog.put("BKN", new TeamMeta("17", "Brooklyn Nets", "Balanced", 4.5));
        teamCatalog.put("CHA", new TeamMeta("30", "Charlotte Hornets", "Offensive", 4.0));
        teamCatalog.put("CHI", new TeamMeta("4", "Chicago Bulls", "Balanced", 5.0));
        teamCatalog.put("CLE", new TeamMeta("5", "Cleveland Cavaliers", "Defensive", 8.5));
        teamCatalog.put("DAL", new TeamMeta("6", "Dallas Mavericks", "Defensive", 7.5));
        teamCatalog.put("DEN", new TeamMeta("7", "Denver Nuggets", "Motion Offense", 8.5));
        teamCatalog.put("DET", new TeamMeta("8", "Detroit Pistons", "Physical Defense", 6.0));
        teamCatalog.put("GSW", new TeamMeta("9", "Golden State Warriors", "Motion Offense", 9.0));
        teamCatalog.put("HOU", new TeamMeta("10", "Houston Rockets", "Aggressive Defense", 8.0));
        teamCatalog.put("IND", new TeamMeta("11", "Indiana Pacers", "Fast Break", 7.5));
        teamCatalog.put("LAC", new TeamMeta("12", "LA Clippers", "Half-Court Tactical", 8.0));
        teamCatalog.put("LAL", new TeamMeta("13", "Los Angeles Lakers", "Offensive", 7.5));
        teamCatalog.put("MEM", new TeamMeta("29", "Memphis Grizzlies", "Pace & Defense", 7.0));
        teamCatalog.put("MIA", new TeamMeta("14", "Miami Heat", "Zone & Grit", 9.5));
        teamCatalog.put("MIL", new TeamMeta("15", "Milwaukee Bucks", "Inside-Out", 6.5));
        teamCatalog.put("MIN", new TeamMeta("16", "Minnesota Timberwolves", "Rim Protection", 8.0));
        teamCatalog.put("NOP", new TeamMeta("3", "New Orleans Pelicans", "Balanced", 5.5));
        teamCatalog.put("NYK", new TeamMeta("18", "New York Knicks", "Physical Grit", 8.5));
        teamCatalog.put("OKC", new TeamMeta("25", "Oklahoma City Thunder", "Switch Everything", 9.5));
        teamCatalog.put("ORL", new TeamMeta("19", "Orlando Magic", "Lockdown Defense", 7.5));
        teamCatalog.put("PHI", new TeamMeta("20", "Philadelphia 76ers", "Tactical Schemes", 7.5));
        teamCatalog.put("PHX", new TeamMeta("21", "Phoenix Suns", "Perimeter Scoring", 7.0));
        teamCatalog.put("POR", new TeamMeta("22", "Portland Trail Blazers", "Rebuild Pace", 4.5));
        teamCatalog.put("SAC", new TeamMeta("23", "Sacramento Kings", "Dribble Hand-Off", 6.5));
        teamCatalog.put("SAS", new TeamMeta("24", "San Antonio Spurs", "Team Ball", 8.0));
        teamCatalog.put("TOR", new TeamMeta("28", "Toronto Raptors", "Transition", 5.5));
        teamCatalog.put("UTA", new TeamMeta("26", "Utah Jazz", "Five-Out", 6.5));
        teamCatalog.put("WAS", new TeamMeta("27", "Washington Wizards", "Development", 4.0));
    }

    /**
     * Normalizes non-standard ESPN team abbreviations (e.g., "GS", "WSH", "UTAH")
     * into standard three-letter NBA franchise codes recognized by the catalog.
     * @param code raw team abbreviation from scoreboard feed
     * @return normalized standard team code
     */
    public String normalizeTeamCode(String code) {
        if (code == null) return "";
        String c = code.trim().toUpperCase();
        return switch (c) {
            case "GS", "GSW" -> "GSW";
            case "NO", "NOP" -> "NOP";
            case "UT", "UTAH", "UTA" -> "UTA";
            case "SA", "SAS" -> "SAS";
            case "NY", "NYK" -> "NYK";
            case "WSH", "WAS" -> "WAS";
            case "PHX", "PHO" -> "PHX";
            case "BKN", "BRK" -> "BKN";
            default -> c;
        };
    }

    /**
     * Checks if a given three-letter team abbreviation is part of the supported NBA catalog.
     * @param teamCode three-letter team abbreviation
     * @return true if supported, false otherwise
     */
    public boolean isSupportedTeam(String teamCode) {
        teamCode = normalizeTeamCode(teamCode);
        if (teamCode == null) {
            return false;
        }
        return teamCatalog.containsKey(teamCode.trim().toUpperCase());
    }

    /**
     * Returns a formatted summary list of all supported NBA teams for display in the console menu.
     * @return list of team summary strings (e.g., "LAL - Los Angeles Lakers")
     */
    public List<String> getSupportedTeamsSummary() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, TeamMeta> entry : teamCatalog.entrySet()) {
            list.add(entry.getKey() + " - " + entry.getValue().fullName());
        }
        return list;
    }

    /**
     * Fetches a fully populated {@link Team} with coach and active players for the specified code.
     * Serves cached instances if already loaded; falls back to an offline default roster upon network failure.
     * @param teamCode three-letter team abbreviation
     * @return fully populated {@link Team}
     * @throws IllegalArgumentException if the team code ist not recognized
     */
    public Team fetchTeam(String teamCode) {
        teamCode = normalizeTeamCode(teamCode);
        if (!isSupportedTeam(teamCode)) {
            throw new IllegalArgumentException("Unsupported NBA team code: " + teamCode);
        }
        String code = teamCode.trim().toUpperCase();
        if (teamCache.containsKey(code)) {
            return teamCache.get(code);
        }
        TeamMeta meta = teamCatalog.get(code);
        try {

            String rosterJson = sendGet(String.format(ROSTER_URL, meta.espnId()));
            Coach coach = SimpleJsonParser.parseCoach(rosterJson, meta.coachStyle(), meta.coachBonus());
            Team team = new Team(meta.fullName(), code, coach);
            int[] record = SimpleJsonParser.parseTeamRecord(rosterJson);
            team.setRecord(record[0], record[1]);
            List<String[]> rosterEntries = SimpleJsonParser.parseRosterEntries(rosterJson);
            List<CompletableFuture<Player>> futures = new ArrayList<>();
            for (String[] entry : rosterEntries) {
                String athleteId = entry[0];
                String fullName = entry[1];
                int age = Integer.parseInt(entry[2]);
                String position = entry[3];
                HttpRequest req = buildGetRequest(String.format(OVERVIEW_URL, athleteId));
                CompletableFuture<Player> future = httpClient
                        .sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenApply(res -> res.statusCode() == 200
                                ? SimpleJsonParser.parsePlayerStats(fullName, age, position, res.body())
                                : null)
                        .exceptionally(ex -> null);
                futures.add(future);
            }

            for (CompletableFuture<Player> future : futures) {
                Player player = future.join();
                if (player != null) {
                    try {
                        team.addPlayer(player);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            if (team.getRosterSize() == 0) {
                throw new IllegalStateException("No active players returned from live API");
            }
            teamCache.put(code, team);
            return team;
        } catch (Exception e) {
            System.out.println("[INFO] Live API unavailable(" + e.getMessage() + "). Loading offline fallback roster...");
            Team fallback = createFallbackTeam(code, meta);
            teamCache.put(code, fallback);
            return fallback;
        }
    }

    /**
     * Fetches today's live scheduled NBA matchups from ESPN's scoreboard endpoint.
     * Falls back to sample marquee matchups if the endpoint is unreachable or offline.
     * @return a list of {@link ScheduledMatch} instances scheduled for today
     */
    public List<ScheduledMatch> fetchTonightMatches() {
        try {
            String todayDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            String json = sendGet(SCOREBOARD_URL + "?dates=" + todayDate);
            List<ScheduledMatch> matches = SimpleJsonParser.parseScheduledMatches(json);
            if (!matches.isEmpty()) {
                return matches;
            }
            throw new IllegalStateException("No scheduled games found");
        } catch (Exception e) {
            System.out.println("[INFO] Live scoreboard unavailable. Loading sample matchups...");
            return createFallbackMatches();
        }
    }

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .header("User-Agent", "NbaRosterMatchupAnalyzer/1.0")
                .GET()
                .build();
    }
    private String sendGet(String url) throws Exception {
        HttpResponse<String> response = httpClient.send(buildGetRequest(url), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP status " + response.statusCode());
        }
        return response.body();
    }

    /**
     * Builds an offline fallback team with realistic stats when the live API is inaccessible.
     * @param code team abbreviation
     * @param meta team metadata record
     * @return fallback {@link Team}
     */
    private Team createFallbackTeam(String code, TeamMeta meta) {
        Coach coach = new Coach("Head Coach (" + code + ")", 45, meta.coachStyle(), meta.coachBonus());
        Team team = new Team(meta.fullName(), code, coach);
        if (code.equals("LAL")) {
            team.addPlayer(new Player("Luka Doncic", 27, "G", 33.5, 8.4, 8.8, 1.6, 0.6, 4.1, 49.7));
            team.addPlayer(new Player("Austin Reaves", 28, "G", 20.2, 4.5, 5.8, 1.1, 0.3, 2.4, 47.5));
            team.addPlayer(new Player("Walker Kessler", 25, "C", 11.4, 10.8, 1.5, 0.6, 2.6, 1.2, 65.0));
        } else if (code.equals("BOS")) {
            team.addPlayer(new Player("Jayson Tatum", 28, "F", 28.4, 8.5, 5.4, 1.2, 0.7, 2.7, 47.5));
            team.addPlayer(new Player("Paul George", 36, "F", 21.0, 5.4, 4.2, 1.5, 0.5, 2.3, 45.5));
            team.addPlayer(new Player("Derrick White", 32, "G", 16.5, 4.3, 5.1, 1.1, 1.1, 1.6, 46.5));
        } else {
            team.addPlayer(new Player(code + " Star Guard", 26, "G", 25.0, 5.2, 6.8, 1.4, 0.4, 2.8, 45.0));
            team.addPlayer(new Player(code + " Starting Wing", 25, "F", 18.5, 6.4, 3.2, 1.1, 0.6, 1.9, 46.5));
            team.addPlayer(new Player(code + " Rim Center", 27, "C", 14.2, 10.5, 2.1, 0.7, 2.1, 1.7, 62.0));
        }
        return team;
    }

    private List<ScheduledMatch> createFallbackMatches() {
        List<ScheduledMatch> fallback = new ArrayList<>();
        fallback.add(new ScheduledMatch("LAL", "BOS", "01:30 CET"));
        fallback.add(new ScheduledMatch("DEN", "GSW", "03:00 CET"));
        fallback.add(new ScheduledMatch("NYK", "CLE", "01:00 CET"));
        return fallback;
    }
}
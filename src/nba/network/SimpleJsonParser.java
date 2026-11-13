package nba.network;

import nba.model.ScheduledMatch;
import nba.model.Coach;
import nba.model.Player;
import java.util.ArrayList;
import java.util.List;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight, zero-dependency JSON parsing utility for extracting NBA data
 * from ESPN's public API endpoints using pure Java string manipulation.
 */
public class SimpleJsonParser {

    /**
     * Extracts a scalar string value for a given key from a JSON fragment.
     * @param json  the JSON string to search within
     * @param key   the JSON property name
     * @return the extracted string value, or an empty string if not found
     */
    public static String extractValue(String json, String key) {

        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return "";
        }

        int colonIndex = json.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex == -1) {
            return "";
        }

        int startIndex = colonIndex + 1;

        int commaIndex = json.indexOf(',', startIndex);
        int braceIndex = json.indexOf('}', startIndex);

        int endIndex;
        if (commaIndex != -1 && (braceIndex == -1 || commaIndex < braceIndex)) {
            endIndex = commaIndex;
        } else {
            endIndex = braceIndex;
        }

        if (endIndex == -1) {
            return "";
        }

        return json.substring(startIndex, endIndex).replace("\"", "").trim();
    }

    /**
     * Extracts an array of string values for a specified array key from JSON.
     * @param json      the JSON string containing the target array
     * @param arrayKey  the key identifying the JSON array
     * @return          an array of trimmed string values, or an empty array if not found
     */
    public static String[] extractArray(String json, String arrayKey) {

        String searchKey = "\"" + arrayKey + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return new String[0];
        }

        int openBracket = json.indexOf('[', keyIndex + searchKey.length());
        int closeBracket = json.indexOf(']', openBracket + 1);
        if (openBracket == -1 || closeBracket == -1) {
            return new String[0];
        }

        String content = json.substring(openBracket + 1, closeBracket).replace("\"", "").trim();
        if (content.isEmpty()) {
            return new String[0];
        }

        return content.split(",");
    }

    /**
     * Safely parses a string value into a double, returning 0.0 on error or blank input.
     * @param value the string representation of number
     * @return parsed double or 0.0
     */
    private static double parseSafeDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Parses the head coach name from an ESPN roster JSON and constructs a {@link Coach}.
     * @param rosterJson    the raw team roster JSON string
     * @param tacticalStyle the assigned tactical philosophy for this franchise
     * @param tacticalBonus the tactical rating bonus
     * @return a constructed {@link Coach} instance
     */
    public static Coach parseCoach(String rosterJson, String tacticalStyle, double tacticalBonus) {
        int coachIndex = rosterJson.indexOf("\"coach\":[");
        String coachName = "Head Coach";
        if (coachIndex != -1) {
            String coachSection = rosterJson.substring(coachIndex);
            String firstName = extractValue(coachSection, "firstName");
            String lastName = extractValue(coachSection, "lastName");
            String combined = (firstName + " " + lastName).trim();
            if (!combined.isEmpty()) {
                coachName = combined;
            }
        }
        return new Coach(coachName, 45, tacticalStyle, tacticalBonus);
    }

    /**
     * Parses the team's win-loss record summary from the ESPN roster JSON payload.
     * @param rosterJson the raw roster JSON payload
     * @return a two-element int array {@code [wins, losses]}, or {@code [0, 0]} if unavailable
     */
    public static int[] parseTeamRecord(String rosterJson) {
        String record = extractValue(rosterJson, "recordSummary"); // e.g., "6-2"
        String[] parts = record.split("-");
        if (parts.length >= 2) {
            int wins = (int) parseSafeDouble(parts[0]);
            int losses = (int) parseSafeDouble(parts[1]);
            return new int[]{wins, losses};
        }
        return new int[]{0, 0};
    }

    /**
     * Parses all athlete entries from a roster JSON, automatically filtering out
     * any player officially designated as "Out" in live injury reports.
     * @param rosterJson the raw roster JSON payload
     * @return list of string arrays each containing [athleteId, fullName, age, position]
     */
    public static List<String[]> parseRosterEntries(String rosterJson) {
        List<String[]> entries = new ArrayList<>();
        String uidMarker = "\"uid\":\"s:40~l:46~a:";
        int searchFrom = 0;
        while (true) {
            int markerPos = rosterJson.indexOf(uidMarker, searchFrom);
            if (markerPos == -1) {
                break; // Break if no more athlete entries remain
            }
            int idStart = markerPos + uidMarker.length();
            int idEnd = rosterJson.indexOf('"', idStart);
            if (idEnd == -1) {
                break;
            }

            String athleteId = rosterJson.substring(idStart, idEnd);
            int nextMarkerPos = rosterJson.indexOf(uidMarker, idEnd + 1);
            String athleteSlice = (nextMarkerPos != -1)
                    ? rosterJson.substring(idEnd, nextMarkerPos)
                    : rosterJson.substring(idEnd);

            if (athleteSlice.contains("\"status\":\"Out\"")) {
                searchFrom = idEnd + 1;
                continue; // Skip injured ("Out") player
            }

            String fullName = extractValue(athleteSlice, "fullName");
            int posIndex = athleteSlice.indexOf("\"position\":{");
            String bioSlice = (posIndex != -1) ? athleteSlice.substring(0, posIndex) : athleteSlice;
            int age = (int) parseSafeDouble(extractValue(bioSlice, "age"));
            if (age < 18 || age > 60) {
                age = 25;
            }
            String position = (posIndex != -1) ? extractValue(athleteSlice.substring(posIndex), "abbreviation") : "G";
            if (position.isBlank()) {
                position = "G";
            }
            if (!fullName.isBlank()) {
                entries.add(new String[]{athleteId, fullName, String.valueOf(age), position});
            }
            searchFrom = idEnd + 1;
        }
        return entries;
    }

    /**
     * Parses an individual player's regular season per-game statistics and shooting efficiency
     * from ESPN's athlete overview endpoint into a {@link Player} instance.
     * @param name       player full name
     * @param age        player age
     * @param position   player court position
     * @param overviewJson athlete overview JSON payload
     * @return a populated {@link Player} instance, or null if the player has no regular season game log
     */
    public static Player parsePlayerStats(String name, int age, String position, String overviewJson) {
        double fgPct = 46.0;
        int regIndex = overviewJson.indexOf("\"Regular Season\"");
        if (regIndex == -1) {
            return null; // Skip player with no regular season games played
        }
        String[] labels = extractArray(overviewJson, "labels");
        String[] stats = extractArray(overviewJson.substring(regIndex), "stats");
        if (labels.length == 0 || stats.length < labels.length) {
            return null;
        }
        double gamesPlayed = 0.0;
        double ppg = 0.0, rpg = 0.0, apg = 0.0, spg = 0.0, bpg = 0.0, topg = 0.0;
        for (int i = 0; i < labels.length; i++) {
            switch (labels[i].trim()) {
                case "GP" -> gamesPlayed = parseSafeDouble(stats[i]);
                case "PTS" -> ppg = parseSafeDouble(stats[i]);
                case "REB" -> rpg = parseSafeDouble(stats[i]);
                case "AST" -> apg = parseSafeDouble(stats[i]);
                case "STL" -> spg = parseSafeDouble(stats[i]);
                case "BLK" -> bpg = parseSafeDouble(stats[i]);
                case "TO" -> topg = parseSafeDouble(stats[i]);
                case "FG%" -> fgPct = parseSafeDouble(stats[i]);
                default -> {
                }
            }
        }
        if (gamesPlayed <= 0) {
            return null;
        }
        return new Player(name, age, position, ppg, rpg, apg, spg, bpg, topg, fgPct);

    }

    /**
     * Parses today's scheduled NBA matchups from ESPN's scoreboard JSON payload.
     * Extracts home and away team abbreviations by parsing matchup patterns (e.g., "BOS @ LAL").
     * @param scoreboardJson the raw daily scoreboard JSON response
     * @return a list of {@link ScheduledMatch} instances representing today's games
     */
    public static List<ScheduledMatch> parseScheduledMatches(String scoreboardJson) {

        List<ScheduledMatch> matches = new ArrayList<>();
        if (scoreboardJson == null || scoreboardJson.isBlank()) {
            return matches;
        }

        String marker = "\"shortName\":\"";
        int searchFrom = 0;

        while (true) {
            int markerPos = scoreboardJson.indexOf(marker, searchFrom);
            if (markerPos == -1) {
                break;
            }
            int valStart = markerPos + marker.length();
            int valEnd = scoreboardJson.indexOf('"', valStart);
            if  (valEnd == -1) {
                break;
            }
            String matchUp = scoreboardJson.substring(valStart, valEnd);
            if (matchUp.contains("@")) {
                String[] teams = matchUp.split("@");
                String away = teams[0].trim();
                String home = teams[1].trim();

                int checkEnd = Math.min(scoreboardJson.length(), markerPos + 500);
                if (scoreboardJson.substring(markerPos, checkEnd).contains("\"state\":\"post\"")) {
                    searchFrom = valEnd + 1;
                    continue;
                }

                int dateMarker = scoreboardJson.lastIndexOf("\"date\":\"", markerPos);
                String matchTime = "Scheduled (CET)";
                if (dateMarker != -1) {
                    int dStart = dateMarker + 8; // Length of "\"date\":\""
                    int dEnd = scoreboardJson.indexOf('"', dStart);
                    if (dEnd != -1) {
                        matchTime = convertUtcToBerlinTime(scoreboardJson.substring(dStart, dEnd));
                    }
                }
                matches.add(new ScheduledMatch(home, away, matchTime));
            }
            searchFrom = valEnd + 1;
        }
        return matches;
    }

    /**
     * Converts an ISO-8601 UTC timestamp string from ESPN into formatted Berlin local time (CET/CEST).
     * @param utcDateStr ISO-8601 formatted date string (e.g., "2026-11-06T00:30Z")
     * @return formatted local time string (e.g., "01:30 CET") or a fallback label if parsing fails
     */
    public static String convertUtcToBerlinTime(String utcDateStr) {
        try {
            String normalized = utcDateStr.trim();
            // Normalize 17-char timestamps without seconds (e.g., "2026-11-07T00:00Z") by inserting ":00"
            if (normalized.endsWith("Z") && normalized.length() == 17) {
                normalized = normalized.substring(0, 16) + ":00Z";
            }
            Instant instant = Instant.parse(normalized);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm z", java.util.Locale.US)
                    .withZone(ZoneId.of("Europe/Berlin"));
            return formatter.format(instant);
        } catch (Exception e) {
            return "Scheduled (CET)";
        }
    }
}
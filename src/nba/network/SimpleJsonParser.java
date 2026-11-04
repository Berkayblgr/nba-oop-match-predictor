package nba.network;

import nba.model.Coach;
import nba.model.Player;
import java.util.ArrayList;
import java.util.List;

public class SimpleJsonParser {

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

    public static int[] parseTeamRecord(String rosterJson) {
        String record = extractValue(rosterJson, "recordSummary"); // örn: "6-2"
        String[] parts = record.split("-");
        if (parts.length >= 2) {
            int wins = (int) parseSafeDouble(parts[0]);
            int losses = (int) parseSafeDouble(parts[1]);
            return new int[]{wins, losses};
        }
        return new int[]{0, 0};
    }

    public static List<String[]> parseRosterEntries(String rosterJson) {
        List<String[]> entries = new ArrayList<>();
        String uidMarker = "\"uid\":\"s:40~l:46~a:";
        int searchFrom = 0;
        while (true) {
            int markerPos = rosterJson.indexOf(uidMarker, searchFrom);
            if (markerPos == -1) {
                break; // Başka oyuncu kalmadıysa döngüden çık
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
                continue; // Sakat ("Out") oyuncuyu atla, sıradakine geç
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

    public static Player parsePlayerStats(String name, int age, String position, String overviewJson) {
        double fgPct = 46.0;
        int regIndex = overviewJson.indexOf("\"Regular Season\"");
        if (regIndex == -1) {
            return null; // Bu sezon henüz maça çıkmamış (sakat/çaylak) oyuncuyu atla
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
}
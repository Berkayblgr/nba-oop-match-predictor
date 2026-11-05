import nba.network.NbaApiClient;
import nba.ui.ConsoleMenu;

/**
 * Main application entry point for the NBA OOP Analyzer & Match Predictor.
 */
public class Main {

    /**
     * Bootstraps the API client and starts the console user interface loop.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        NbaApiClient apiClient = new NbaApiClient();
        ConsoleMenu menu = new ConsoleMenu(apiClient);
        menu.start();
    }
}

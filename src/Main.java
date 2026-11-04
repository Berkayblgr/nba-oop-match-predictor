import nba.network.NbaApiClient;
import nba.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        NbaApiClient apiClient = new NbaApiClient();
        ConsoleMenu menu = new ConsoleMenu(apiClient);
        menu.start();
    }
}

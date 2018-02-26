package corex.all;

import corex.game.StartupGame;
import corex.gateway.StartupGateway;
import corex.gateway.StartupGateway2;
import corex.login.StartupLogin;

public class StartupAll {

    public static void main(String[] args) {
        StartupLogin.main(args);
        StartupGame.main(args);
        StartupGateway.main(args);
        StartupGateway2.main(args);

        System.out.println("success all");
    }
}

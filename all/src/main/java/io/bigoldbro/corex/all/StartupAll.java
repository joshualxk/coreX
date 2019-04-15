package io.bigoldbro.corex.all;

import io.bigoldbro.corex.game.StartupGame;
import io.bigoldbro.corex.gateway.StartupGateway;
import io.bigoldbro.corex.login.StartupLogin;

public class StartupAll {

    public static void main(String[] args) {
        StartupLogin.main(args);
        StartupGame.main(args);
        StartupGateway.main(args);

        System.out.println("success all");
    }
}

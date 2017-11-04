package co.selim.gameserver;

import co.selim.gameserver.websocket.GameMovementHandler;
import spark.Spark;

public class GameServer {
    public static void main(String[] args) {
        Spark.port(8080);


        Spark.webSocket("/api/movement/*", GameMovementHandler.class);

        Spark.get("/api/movement/*", (req, res) -> {
            res.status(101);
            return "Switching Protocols";
        });

        Spark.options("/*", (req, res) -> {
            System.err.println("OPTIONS");
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        Spark.before((req, res) -> {
            System.err.println("BEFORE");
            res.header("Access-Control-Allow-Origin", req.headers("Origin"));
            res.header("Access-Control-Allow-Credentials", "true");
        });
    }
}

package com.futebol.colaborativo.api;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.futebol.colaborativo.SistemaFutebol;

public class ApiServer {

    private static final Gson gson = new Gson();

    public static void start(SistemaFutebol sistema) {

        port(8080);

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.type("application/json");
        });

        get("/api/jogadores", (req, res) ->
            gson.toJson(sistema.getEstados())
        );

        // STATUS
        get("/api/status", (req, res) ->
            gson.toJson(sistema.getStatus())
        );

    }
}
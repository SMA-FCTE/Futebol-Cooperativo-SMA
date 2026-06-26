package com.futebol.colaborativo.api;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.futebol.colaborativo.SistemaFutebol;

import java.util.Map;

public class ApiServer {

    private static final Gson gson = new Gson();

    public static void start(SistemaFutebol sistema) {

        port(8080);

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.type("application/json");
        });

        // Preflight CORS para requisições POST vindas do browser
        options("/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
            return "";
        });

        get("/api/jogadores", (req, res) ->
            gson.toJson(sistema.getEstados())
        );

        get("/api/status", (req, res) ->
            gson.toJson(sistema.getStatus())
        );

        post("/api/partida/iniciar", (req, res) -> {
            try {
                JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
                int duracaoSegundos = body.get("duracaoSegundos").getAsInt();

                if (duracaoSegundos < 60 || duracaoSegundos > 1800) {
                    res.status(400);
                    return gson.toJson(Map.of("erro", "duracaoSegundos deve estar entre 60 e 1800"));
                }

                sistema.iniciarPartida(duracaoSegundos);
                return gson.toJson(Map.of("ok", true));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Requisicao invalida"));
            }
        });

        post("/api/partida/reiniciar", (req, res) -> {
            sistema.reiniciarPartida();
            return gson.toJson(Map.of("ok", true));
        });

    }
}
package com.test.backend.app;

import com.sun.net.httpserver.HttpHandler;
import com.test.backend.app.endpoints.HighScoreEndpoint;
import com.test.backend.app.endpoints.LoginEndpoint;
import com.test.backend.app.endpoints.ScoreEndpoint;
import com.test.backend.app.model.User;
import com.test.backend.app.requestparser.HighScoreRequestParser;
import com.test.backend.app.requestparser.LoginRequestParser;
import com.test.backend.app.requestparser.ScoreRequest;
import com.test.backend.app.requestparser.ScoreRequestParser;
import com.test.backend.app.services.LoginService;
import com.test.backend.app.services.ScoreService;
import com.test.backend.app.sservices.SDefaultLoginService;
import com.test.backend.app.sservices.SDefaultScoreService;
import com.test.backend.server.BackEndServer;
import com.test.backend.server.dispatcher.DefaultHttpHandler;
import com.test.backend.server.endpoint.Endpoints;
import com.test.backend.server.endpoint.RequestParser;
import com.test.backend.server.http.DefaultRequestBuilder;
import com.test.backend.server.http.DefaultResponseBuilder;
import com.test.backend.server.http.HttpResponseSender;
import com.test.backend.server.http.ResponseBuilder;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Single Thread Version of the Server. Faster than multithread due not to share state with lock contention
 */
public class SingleThreadTestServer {

    //In milliseconds: So 10 minutes
    private static long DEFAULT_TOKEN_EXPIRATION = 1000 * 60 * 10;

    private static int SCORES_PER_LEVEL = 15;

    private final BackEndServer server;

    public SingleThreadTestServer(long tokenExpirationTime, int scoresPerLevel) {

        //Services
        ThreadPoolExecutor threadPoolExecutor =//
                new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5000));

        ThreadPoolExecutor loginThreadPool =//
                new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5000));

        LoginService loginService = new SDefaultLoginService(tokenExpirationTime, loginThreadPool);
        ScoreService scoreService = new SDefaultScoreService(scoresPerLevel);

        //Endpoints
        ResponseBuilder responseBuilder = new DefaultResponseBuilder();

        RequestParser<User> loginRequestParser = new LoginRequestParser();
        RequestParser<ScoreRequest> scoreRequestParser = new ScoreRequestParser();
        RequestParser<Integer> highScoreRequestParser = new HighScoreRequestParser();

        Endpoints endpoints = new Endpoints( //
                Arrays.asList( //
                        new ScoreEndpoint(responseBuilder, scoreRequestParser, scoreService, loginService), //
                        new LoginEndpoint(responseBuilder, loginRequestParser, loginService), //
                        new HighScoreEndpoint(responseBuilder, highScoreRequestParser, scoreService)//
                )//
        );

        //Server
        HttpHandler httpHandler = //
                new DefaultHttpHandler(endpoints, new DefaultRequestBuilder(), //
                        responseBuilder, new HttpResponseSender());

        server = new BackEndServer.BackEndServerBuilder().
                address("0.0.0.0").port(8888) //
                .connectionThreads(350)//
                .httpHandler("/", httpHandler) //
                .threadPoolExecutor(threadPoolExecutor).build(); //

    }

    public void runServer() {
        Scanner user_input = null;
        try {
            user_input = new Scanner(System.in);
            server.start();
            System.out.println("Press Enter to stop the server");
            user_input.nextLine();

        } finally {
            if (user_input != null) {
                user_input.close();
            }
            stopServer();
        }
    }

    public void stopServer() {
        server.stop(1);
        System.out.println("TestServer Stopped");
    }

    public static void main(String[] args) throws IllegalArgumentException {
        SingleThreadTestServer testServer = new SingleThreadTestServer(DEFAULT_TOKEN_EXPIRATION, SCORES_PER_LEVEL);
        testServer.runServer();

    }

}

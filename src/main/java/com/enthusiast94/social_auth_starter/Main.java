package com.enthusiast94.social_auth_starter;

/**
 * Created by ManasB on 7/28/2015.
 */

import com.enthusiast94.social_auth_starter.controllers.UserController;
import com.enthusiast94.social_auth_starter.services.AccessTokenService;
import com.enthusiast94.social_auth_starter.services.UserService;
import com.enthusiast94.social_auth_starter.utils.ApiResponse;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import static spark.Spark.before;
import static spark.Spark.halt;
import static spark.SparkBase.port;

public class Main {

    public static void main (String[] args) {

        /**
         * Configure spark
         */

        port(3000);


        /**
         * Configure db
         */

        Morphia morphia = new Morphia();

        // tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.mapPackage("com.enthusiast94.social_auth_starter.models");

        // create the Datastore connecting to the default port on the local host
        Datastore db = morphia.createDatastore(new MongoClient("localhost"), "social_auth_starter_db");
        db.ensureIndexes();


        /**
         * Setup endpoints
         */

        AccessTokenService accessTokenService = new AccessTokenService(db);
        UserService userService = new UserService(db);

        // set response type for all requests to json
        before((req, res) -> res.type("application/json"));

        // require authentication for all me/ requests
        before("/me", (req, res) -> {
            String authHeader = req.headers("Authorization");

            if (authHeader == null) {
                halt(new ApiResponse(401, "no authorization header found", null).toJson());
                return;
            }

            String accessToken = authHeader.substring("Token".length()+1, authHeader.length());
            if (!accessTokenService.isAccessTokenValid(accessToken)) {
                halt(new ApiResponse(401, "invalid access token", null).toJson());
            }
        });

        new UserController(userService, accessTokenService).setupEndpoints();

    }
}

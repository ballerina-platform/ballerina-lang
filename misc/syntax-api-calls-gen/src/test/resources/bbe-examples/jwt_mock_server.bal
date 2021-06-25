// This is a mock JWK server, which is to expose the JWK components for testing purpose.
import ballerina/config;
import ballerina/http;

listener http:Listener oauth2Server = new(20000, {
    secureSocket: {
        keyStore: {
            path: config:getAsString("b7a.home") +
                  "/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service oauth2 on oauth2Server {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/jwks"
    }
    resource function jwks(http:Caller caller, http:Request req) {
        http:Response res = new;
        json jwks = {
          "keys": [
            {
              "kty": "RSA",
              "e": "AQAB",
              "use": "sig",
              "kid": "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ",
              "alg": "RS256",
              "n": "AIFcoun1YlS4mShJ8OfcczYtZXGIes_XWZ7oPhfYCqhSIJnXD3vqrUu4" +
                    "GXNY2E41jAm8dd7BS5GajR3g1GnaZrSqN0w3bjpdbKjOnM98l2-i9-J" +
                    "P5XoedJsyDzZmml8Xd7zkKCuDqZIDtZ99poevrZKd7Gx5n2Kg0K5FSt" +
                    "bZmDbTyX30gi0_griIZyVCXKOzdLp2sfskmTeu_wF_vrCaagIQCGSc6" +
                    "0Yurnjd0RQiMWA10jL8axJjnZ-IDgtKNQK_buQafTedrKqhmzdceozS" +
                    "ot231I9dth7uXvmPSjpn23IYUIpdj_NXCIt9FSoMg5-Q3lhLg6GK3nZ" +
                    "OPuqgGa8TMPs="
            }
          ]
        };
        res.setJsonPayload(jwks);
        checkpanic caller->respond(res);
    }
}

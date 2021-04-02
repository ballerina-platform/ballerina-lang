import ballerina/config;
import ballerina/crypto;
import ballerina/io;
import ballerina/jwt;
import ballerina/time;

public function main() {
    // Defines the keystore.
    crypto:KeyStore keyStore = {
        path: config:getAsString("b7a.home") +
              "/bre/security/ballerinaKeystore.p12",
        password: "ballerina"
    };

    // Defines the JWT keystore configurations.
    jwt:JwtKeyStoreConfig keyStoreConfig = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    // Defines the JWT header and sets the values.
    jwt:JwtHeader header = {};
    header.alg = jwt:RS256;
    header.typ = "JWT";
    header.kid = "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ";

    // Defines the JWT payload and sets the values.
    jwt:JwtPayload payload = {};
    payload.sub = "admin";
    payload.iss = "ballerina";
    payload.jti = "100078234ba23";
    payload.aud = ["vEwzbcasJVQm1jVYHUHCjhxZ4tYa"];
    payload.exp = time:currentTime().time/1000 + 600;

    // Issues a JWT based on the provided header, payload, and keystore config.
    string|jwt:Error jwt = jwt:issueJwt(header, payload, keyStoreConfig);
    if (jwt is string) {
        io:println("Issued JWT: ", jwt);
    } else {
        io:println("An error occurred while issuing the JWT: ",
                    jwt.message());
    }

    // Defines the truststore.
    crypto:TrustStore trustStore = {
        path: config:getAsString("b7a.home") +
              "/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    };

    // Defines the JWT validator configurations with truststore configurations.
    jwt:JwtValidatorConfig validatorConfig1 = {
        issuer: "ballerina",
        audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa",
        clockSkewInSeconds: 60,
        trustStoreConfig: {
            certificateAlias: "ballerina",
            trustStore: trustStore
        }
    };

    // Validates the created JWT. Signature is validated using the truststore.
    jwt:JwtPayload|jwt:Error result = jwt:validateJwt(<string>jwt, 
                                                      validatorConfig1);
    if (result is jwt:JwtPayload) {
        io:println("Validated JWT Payload: ", result.toString());
    } else {
        io:println("An error occurred while validating the JWT: ",
                                                            result.message());
    }

    // Defines the JWT validator configurations with JWKs configurations.
    jwt:JwtValidatorConfig validatorConfig2 = {
        issuer: "ballerina",
        audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa",
        clockSkewInSeconds: 60,
        jwksConfig: {
            url: "https://localhost:20000/oauth2/jwks",
            clientConfig: {
                secureSocket: {
                    trustStore: trustStore
                }
            }
        }
    };

    // Validates the created JWT. Signature is validated using the JWKS endpoint.
    result = jwt:validateJwt(<string>jwt, validatorConfig2);
    if (result is jwt:JwtPayload) {
        io:println("Validated JWT Payload: ", result.toString());
    } else {
        io:println("An error occurred while validating the JWT: ",
                                                        result.message());
    }
}

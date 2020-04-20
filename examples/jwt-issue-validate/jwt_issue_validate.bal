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

    // Defines the JWT payload and sets the values.
    jwt:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    // Issues a JWT based on the provided header, payload and config.
    string|jwt:Error jwt = jwt:issueJwt(header, payload, keyStoreConfig);
    if (jwt is string) {
        io:println("Issued JWT: ", jwt);
    } else {
        io:println("An error occurred while issuing the JWT: ",
                    jwt.detail()?.message);
    }

    // Defines the truststore.
    crypto:TrustStore trustStore = {
        path: config:getAsString("b7a.home") +
              "/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    };

    // Defines the JWT validator configurations.
    jwt:JwtValidatorConfig validatorConfig = {
        issuer: "wso2",
        audience: "ballerina",
        clockSkewInSeconds: 60,
        trustStoreConfig: {
            certificateAlias: "ballerina",
            trustStore: trustStore
        }
    };

    // Validates the created JWT.
    jwt:JwtPayload|jwt:Error result = jwt:validateJwt(<string>jwt, validatorConfig);
    if (result is jwt:JwtPayload) {
        io:println("Validated JWT Payload: ", result);
    } else {
        io:println("An error occurred while validating the JWT: ",
                    result.detail()?.message);
    }
}

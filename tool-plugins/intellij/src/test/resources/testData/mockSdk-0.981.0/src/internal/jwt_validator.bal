// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/log;
import ballerina/time;

documentation {
    Represents JWT validator configurations.
}
public type JWTValidatorConfig record {
    string issuer,
    string audience,
    int clockSkew,
    string certificateAlias,
    string trustStoreFilePath,
    string trustStorePassword,
};

documentation {
    Validity given JWT token.

    P{{jwtToken}} JWT token that need to validate
    P{{config}} JWTValidatorConfig object
    R{{}} If JWT token is valied true , else false
    R{{}} If JWT token is valied return the JWT payload
    R{{}} If token validation fails
}
public function validate(string jwtToken, JWTValidatorConfig config) returns JwtPayload|error {
    string[] encodedJWTComponents;
    match getJWTComponents(jwtToken) {
        string[] encodedJWT => encodedJWTComponents = encodedJWT;
        error e => return e;
    }

    JwtHeader header = {};
    JwtPayload payload = {};
    match parseJWT(encodedJWTComponents) {
        error e => return e;
        (JwtHeader, JwtPayload) result => {
            (header, payload) = result;
        }
    }

    match validateJWT(encodedJWTComponents, header, payload, config) {
        error e => return e;
        boolean isValid => {
            if (isValid){
                return payload;
            } else {
                error err = {message:"Invalid JWT token"};
                return err;
            }
        }
    }
}

function getJWTComponents(string jwtToken) returns (string[])|error {
    string[] jwtComponents = jwtToken.split("\\.");
    if (lengthof jwtComponents != 3) {
        log:printDebug("Invalid JWT token :" + jwtToken);
        error err = {message:"Invalid JWT token"};
        return err;
    }
    return jwtComponents;
}

function parseJWT(string[] encodedJWTComponents) returns ((JwtHeader, JwtPayload)|error) {
    json headerJson = {};
    json payloadJson = {};
    match getDecodedJWTComponents(encodedJWTComponents) {
        error e => return e;
        (json, json) result => {
            (headerJson, payloadJson) = result;
        }
    }

    JwtHeader jwtHeader = parseHeader(headerJson);
    JwtPayload jwtPayload = parsePayload(payloadJson);
    return (jwtHeader, jwtPayload);
}

function getDecodedJWTComponents(string[] encodedJWTComponents) returns ((json, json)|error) {
    string jwtHeader = check urlDecode(encodedJWTComponents[0]).base64Decode();
    string jwtPayload = check urlDecode(encodedJWTComponents[1]).base64Decode();
    json jwtHeaderJson = {};
    json jwtPayloadJson = {};

    match parseJson(jwtHeader) {
        json result => jwtHeaderJson = result;
        error err => return err;
    }
    match parseJson(jwtPayload) {
        json result => jwtPayloadJson = result;
        error err => return err;
    }
    return (jwtHeaderJson, jwtPayloadJson);
}

function parseHeader(json jwtHeaderJson) returns (JwtHeader) {
    JwtHeader jwtHeader = {};
    map customClaims;

    string[] keys = jwtHeaderJson.getKeys();

    foreach key in keys {
        //TODO get alg from a constant
        if (key == "alg") {
            jwtHeader.alg = jwtHeaderJson[key].toString();
        } else if (key == TYP) {
            jwtHeader.typ = jwtHeaderJson[key].toString();
        } else if (key == CTY) {
            jwtHeader.cty = jwtHeaderJson[key].toString();
        } else if (key == KID) {
            jwtHeader.kid = jwtHeaderJson[key].toString();
        } else {
            if (lengthof jwtHeaderJson[key] > 0) {
                customClaims[key] = convertToStringArray(jwtHeaderJson[key]);
            } else {
                customClaims[key] = jwtHeaderJson[key].toString();
            }
        }
    }
    jwtHeader.customClaims = customClaims;
    return jwtHeader;
}

function parsePayload(json jwtPayloadJson) returns (JwtPayload) {
    JwtPayload jwtPayload = {};
    map customClaims;
    string[] keys = jwtPayloadJson.getKeys();
    foreach key in keys {
        if (key == ISS) {
            jwtPayload.iss = jwtPayloadJson[key].toString();
        } else if (key == SUB) {
            jwtPayload.sub = jwtPayloadJson[key].toString();
        } else if (key == AUD) {
            jwtPayload.aud = convertToStringArray(jwtPayloadJson[key]);
        } else if (key == JTI) {
            jwtPayload.jti = jwtPayloadJson[key].toString();
        } else if (key == EXP) {
            var value = jwtPayloadJson[key].toString();
            jwtPayload.exp = <int>value but { error => 0 };
        } else if (key == NBF) {
            var value = jwtPayloadJson[key].toString();
            jwtPayload.nbf = <int>value but { error => 0 };
        } else if (key == IAT) {
            var value = jwtPayloadJson[key].toString();
            jwtPayload.iat = <int>value but { error => 0 };
        }
        else {
            if (lengthof jwtPayloadJson[key] > 0) {
                customClaims[key] = convertToStringArray(jwtPayloadJson[key]);
            } else {
                customClaims[key] = jwtPayloadJson[key].toString();
            }
        }
    }
    jwtPayload.customClaims = customClaims;
    return jwtPayload;
}

function validateJWT(string[] encodedJWTComponents, JwtHeader jwtHeader, JwtPayload jwtPayload, JWTValidatorConfig
config) returns (boolean|error) {
    if (!validateMandatoryFields(jwtPayload)) {
        error err = {message:
        "Mandatory fields(Issuer, Subject, Expiration time or Audience) are empty in the given JSON Web Token."};
        return err;
    }
    if (!validateSignature(encodedJWTComponents, jwtHeader, config)) {
        error err = {message:"Invalid signature"};
        return err;
    }
    if (!validateIssuer(jwtPayload, config)) {
        error err = {message:"JWT contained invalid issuer name : " + jwtPayload.iss};
        return err;
    }
    if (!validateAudience(jwtPayload, config)) {
        //TODO need to set expected audience or available audience list
        error err = {message:"Invalid audience"};
        return err;
    }
    if (!validateExpirationTime(jwtPayload, config)) {
        error err = {message:"JWT token is expired"};
        return err;
    }
    if (!validateNotBeforeTime(jwtPayload)) {
        error err = {message:"JWT token is used before Not_Before_Time"};
        return err;
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return true;
}

function validateMandatoryFields(JwtPayload jwtPayload) returns (boolean) {
    if (jwtPayload.iss == "" || jwtPayload.sub == "" || jwtPayload.exp == 0 || lengthof jwtPayload.aud == 0) {
        return false;
    }
    return true;
}

function validateSignature(string[] encodedJWTComponents, JwtHeader jwtHeader, JWTValidatorConfig config) returns (
        boolean) {
    string assertion = encodedJWTComponents[0] + "." + encodedJWTComponents[1];
    string signPart = encodedJWTComponents[2];
    TrustStore trustStore = {};
    trustStore.certificateAlias = config.certificateAlias;
    trustStore.trustStoreFilePath = config.trustStoreFilePath;
    trustStore.trustStorePassword = config.trustStorePassword;
    return verifySignature(assertion, signPart, jwtHeader.alg, trustStore);
}

function validateIssuer(JwtPayload jwtPayload, JWTValidatorConfig config) returns (boolean) {
    return jwtPayload.iss == config.issuer;
}

function validateAudience(JwtPayload jwtPayload, JWTValidatorConfig config) returns (boolean) {
    foreach audience in jwtPayload.aud {
        if (audience == config.audience) {
            return true;
        }
    }
    return false;
}

function validateExpirationTime(JwtPayload jwtPayload, JWTValidatorConfig config) returns (boolean) {
    //Convert current time which is in milliseconds to seconds.
    int expTime = jwtPayload.exp;
    if (config.clockSkew > 0){
        expTime = expTime + config.clockSkew;
    }
    return expTime > time:currentTime().time / 1000;
}

function validateNotBeforeTime(JwtPayload jwtPayload) returns (boolean) {
    return time:currentTime().time > jwtPayload.nbf;
}

function convertToStringArray(json jsonData) returns (string[]) {
    string[] outData = [];
    if (lengthof jsonData > 0) {
        int i = 0;
        while (i < lengthof jsonData) {
            outData[i] = jsonData[i].toString();
            i = i + 1;
        }
    } else {
        outData[0] = jsonData.toString();
    }
    return outData;
}

function urlDecode(string encodedString) returns (string) {
    string decodedString = encodedString.replaceAll("-", "+");
    decodedString = decodedString.replaceAll("_", "/");
    return decodedString;
}

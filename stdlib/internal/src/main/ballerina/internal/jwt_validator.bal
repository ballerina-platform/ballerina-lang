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

# Represents JWT validator configurations.
# + issuer - Expected issuer
# + audience - Expected audience
# + clockSkew - Clock skew in seconds
# + certificateAlias - Certificate alias used for validation
# + trustStoreFilePath - Trust store file path
# + trustStorePassword - Trust store password
public type JWTValidatorConfig record {
    string issuer = "";
    string audience = "";
    int clockSkew = 0;
    string certificateAlias = "";
    string trustStoreFilePath = "";
    string trustStorePassword = "";
};

# Validity given JWT token.
#
# + jwtToken - JWT token that need to validate
# + config - JWTValidatorConfig object
# + return - If JWT token is valied return the JWT payload.
#            An error if token validation fails.
public function validate(string jwtToken, JWTValidatorConfig config) returns JwtPayload|error {
    string[] encodedJWTComponents = [];
    var jwtComponents = getJWTComponents(jwtToken);
    if (jwtComponents is string[]) {
        encodedJWTComponents = jwtComponents;
    } else if (jwtComponents is error) {
        return jwtComponents;
    } else {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "Invalid JWT token" });
        return jwtError;
    }

    string[] aud = [];
    JwtHeader header = {};
    JwtPayload payload = {};
    var decodedJwt = parseJWT(encodedJWTComponents);
    if (decodedJwt is (JwtHeader, JwtPayload)) {
        (header, payload) = decodedJwt;
    } else if (decodedJwt is error) {
        return decodedJwt;
    } else {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "Invalid JWT token" });
        return jwtError;
    }

    var jwtValidity = validateJWT(encodedJWTComponents, header, payload, config);
    if (jwtValidity is error) {
        return jwtValidity;
    } else if (jwtValidity is boolean) {
        if (jwtValidity) {
            return payload;
        } else {
            error jwtError = error(INTERNAL_ERROR_CODE, { message : "Invalid JWT token" });
            return jwtError;
        }
    } else {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "Invalid JWT token" });
        return jwtError;
    }
}

function getJWTComponents(string jwtToken) returns (string[])|error {
    string[] jwtComponents = jwtToken.split("\\.");
    if (jwtComponents.length() != 3) {
        log:printDebug(function() returns string {
            return "Invalid JWT token :" + jwtToken;
        });
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "Invalid JWT token" });
        return jwtError;
    }
    return jwtComponents;
}

function parseJWT(string[] encodedJWTComponents) returns ((JwtHeader, JwtPayload)|error) {
    json headerJson = {};
    json payloadJson = {};
    var decodedJWTComponents = getDecodedJWTComponents(encodedJWTComponents);
    if (decodedJWTComponents is (json, json)) {
        (headerJson, payloadJson) = decodedJWTComponents;
    } else if (decodedJWTComponents is error) {
        return decodedJWTComponents;
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

    var jsonHeader = parseJson(jwtHeader);
    if (jsonHeader is json) {
        jwtHeaderJson = jsonHeader;
    } else if (jsonHeader is error) {
        return jsonHeader;
    }

    var jsonPayloaad = parseJson(jwtPayload);
    if (jsonPayloaad is json) {
        jwtPayloadJson = jsonPayloaad;
    } else if (jsonPayloaad is error) {
        return jsonPayloaad;
    }
    return (jwtHeaderJson, jwtPayloadJson);
}

function parseHeader(json jwtHeaderJson) returns (JwtHeader) {
    JwtHeader jwtHeader = {};
    map customClaims = {};

    string[] keys = jwtHeaderJson.getKeys();

    foreach key in keys {
        if (key == ALG) {
            jwtHeader.alg = jwtHeaderJson[key].toString();
        } else if (key == TYP) {
            jwtHeader.typ = jwtHeaderJson[key].toString();
        } else if (key == CTY) {
            jwtHeader.cty = jwtHeaderJson[key].toString();
        } else if (key == KID) {
            jwtHeader.kid = jwtHeaderJson[key].toString();
        } else {
            if (jwtHeaderJson[key].length() > 0) {
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
    string[] aud = [];
    JwtPayload jwtPayload = { iss: "", sub: "", aud: aud, exp: 0 };
    map customClaims = {};
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
            string exp = jwtPayloadJson[key].toString();
            var value = <int>exp;
            if (value is int) {
                jwtPayload.exp = value;
            } else {
                jwtPayload.exp = 0;
            }
        } else if (key == NBF) {
            string nbf = jwtPayloadJson[key].toString();
            var value = <int>nbf;
            if (value is int) {
                jwtPayload.nbf = value;
            } else {
                jwtPayload.nbf = 0;
            }
        } else if (key == IAT) {
            string iat = jwtPayloadJson[key].toString();
            var value = <int>iat;
            if (value is int) {
                jwtPayload.iat = value;
            } else {
                jwtPayload.iat = 0;
            }
        }
        else {
            if (jwtPayloadJson[key].length() > 0) {
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
    if (!validateMandatoryJwtHeaderFields(jwtHeader)) {
        error jwtError = error(INTERNAL_ERROR_CODE,
                        { message : "Mandatory field signing algorithm(alg) is empty in the given JSON Web Token." });
        return jwtError;
    }
    if (!validateMandatoryFields(jwtPayload)) {
        error jwtError = error(INTERNAL_ERROR_CODE,
                        { message : "Mandatory fields(Issuer,Subject, Expiration time or Audience) are empty in the given JSON Web Token." });
        return jwtError;
    }
    var signatureValidationResult = validateSignature(encodedJWTComponents, jwtHeader, config);
    if (signatureValidationResult is error) {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : signatureValidationResult.reason() });
        return jwtError;
    }
    if (!validateIssuer(jwtPayload, config)) {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "JWT contained invalid issuer name : " + jwtPayload.iss });
        return jwtError;
    }
    if (!validateAudience(jwtPayload, config)) {
        //TODO need to set expected audience or available audience list
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "Invalid audience" });
        return jwtError;
    }
    if (!validateExpirationTime(jwtPayload, config)) {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "JWT token is expired" });
        return jwtError;
    }
    //TODO : Validate nbf field of jwtPayload availability first
    if (!validateNotBeforeTime(jwtPayload)) {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "JWT token is used before Not_Before_Time" });
        return jwtError;
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return true;
}

function validateMandatoryJwtHeaderFields(JwtHeader jwtHeader) returns (boolean) {
    if (jwtHeader.alg == "") {
        return false;
    }
    return true;
}

function validateMandatoryFields(JwtPayload jwtPayload) returns (boolean) {
    if (jwtPayload.iss == "" || jwtPayload.sub == "" || jwtPayload.exp == 0 || jwtPayload.aud.length() == 0) {
        return false;
    }
    return true;
}

function validateSignature(string[] encodedJWTComponents, JwtHeader jwtHeader, JWTValidatorConfig config)
returns error? {
    string assertion = encodedJWTComponents[0] + "." + encodedJWTComponents[1];
    string signPart = encodedJWTComponents[2];
    TrustStore trustStore = {
        certificateAlias : config.certificateAlias,
        trustStoreFilePath : config.trustStoreFilePath,
        trustStorePassword : config.trustStorePassword
    };
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
    return time:currentTime().time > (jwtPayload["nbf"] ?: 0);
}

function convertToStringArray(json jsonData) returns (string[]) {
    string[] outData = [];
    if (jsonData.length() > 0) {
        int i = 0;
        while (i < jsonData.length()) {
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

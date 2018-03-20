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

package ballerina.jwt;

import ballerina.log;
import ballerina.time;
import ballerina.util;

@Description {value:"Represents JWT validator configurations"}
public struct JWTValidatorConfig {
    string issuer;
    string audience;
    string certificateAlias;
}

@Description {value:"Validity given JWT token"}
@Param {value:"jwtToken: JWT token that need to validate"}
@Param {value:"config: JWTValidatorConfig object"}
@Return {value:"boolean: If JWT token is valied true , else false"}
@Return {value:"Payload: If JWT token is valied return the JWT payload"}
@Return {value:"error: If token validation fails"}
public function validate (string jwtToken, JWTValidatorConfig config) (boolean, Payload, error) {
    error err;
    string[] encodedJWTComponents;
    encodedJWTComponents, err = getJWTComponents(jwtToken);
    if (err == null) {
        Header header;
        Payload payload;
        header, payload, err = parseJWT(encodedJWTComponents);
        if (err == null) {
            boolean isValid;
            isValid, err = validateJWT(encodedJWTComponents, header, payload, config);
            if (isValid) {
                return true, payload, null;
            }
        }
    }
    return false, null, err;
}

function getJWTComponents (string jwtToken) (string[], error) {
    string[] jwtComponents = jwtToken.split("\\.");
    if (lengthof jwtComponents != 3) {
        log:printDebug("Invalid JWT token :" + jwtToken);
        error err = {message:"Invalid JWT token"};
        return null, err;
    }
    return jwtComponents, null;
}

function parseJWT (string[] encodedJWTComponents) (Header, Payload, error) {
    var headerJson, payloadJson, err = getDecodedJWTComponents(encodedJWTComponents);
    if (err != null) {
        return null, null, err;
    }
    Header jwtHeader = parseHeader(headerJson);
    Payload jwtPayload = parsePayload(payloadJson);
    return jwtHeader, jwtPayload, null;
}

function getDecodedJWTComponents (string[] encodedJWTComponents) (json, json, error) {
    string jwtHeader = util:base64Decode(urlDecode(encodedJWTComponents[0]));
    string jwtPayload = util:base64Decode(urlDecode(encodedJWTComponents[1]));

    var jwtHeaderJson, e1 = <json>jwtHeader;
    var jwtPayloadJson, e2 = <json>jwtPayload;
    if (e1 != null || e2 != null) {
        error err = {message:"Invalid JWT payload"};
        return null, null, err;
    }
    return jwtHeaderJson, jwtPayloadJson, null;
}

function parseHeader (json jwtHeaderJson) (Header) {
    Header jwtHeader = {};
    map customClaims = {};
    foreach key in jwtHeaderJson.getKeys() {
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

function parsePayload (json jwtPayloadJson) (Payload) {
    Payload jwtPayload = {};
    map customClaims = {};
    foreach key in jwtPayloadJson.getKeys() {
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
            var intVal, _ = <int>value;
            jwtPayload.exp = intVal;
        } else if (key == NBF) {
            var value = jwtPayloadJson[key].toString();
            var intVal, _ = <int>value;
            jwtPayload.nbf = intVal;
        } else if (key == IAT) {
            var value = jwtPayloadJson[key].toString();
            var intVal, _ = <int>value;
            jwtPayload.iat = intVal;
        } else {
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

function validateJWT (string[] encodedJWTComponents, Header jwtHeader, Payload jwtPayload, JWTValidatorConfig config)
(boolean, error) {
    if (!validateMandatoryFields(jwtPayload)) {
        error err = {message:"Mandatory fields(Issuer, Subject, Expiration time or Audience) are empty in the given JSON Web Token."};
        return false, err;
    }
    if (!validateSignature(encodedJWTComponents, jwtHeader, config)) {
        error err = {message:"Invalid signature"};
        return false, err;
    }
    if (!validateIssuer(jwtPayload, config)) {
        error err = {message:"No Registered IDP found for the JWT with issuer name : " + jwtPayload.iss};
        return false, err;
    }
    if (!validateAudience(jwtPayload, config)) {
        //TODO need to set expected audience or available audience list
        error err = {message:"Invalid audience"};
        return false, err;
    }
    if (!validateExpirationTime(jwtPayload)) {
        error err = {message:"JWT token is expired"};
        return false, err;
    }
    if (!validateNotBeforeTime(jwtPayload)) {
        error err = {message:"JWT token is used before Not_Before_Time"};
        return false, err;
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return true, null;
}

function validateMandatoryFields (Payload jwtPayload) (boolean) {
    if (jwtPayload.iss == null || jwtPayload.sub == null || jwtPayload.exp == 0 || jwtPayload.aud == null) {
        return false;
    }
    return true;
}

function validateSignature (string[] encodedJWTComponents, Header jwtHeader, JWTValidatorConfig config) (boolean) {
    string assertion = encodedJWTComponents[0] + "." + encodedJWTComponents[1];
    string signPart = encodedJWTComponents[2];
    return signature:verify(assertion, signPart, jwtHeader.alg, config.certificateAlias);
}

function validateIssuer (Payload jwtPayload, JWTValidatorConfig config) (boolean) {
    return jwtPayload.iss == config.issuer;
}

function validateAudience (Payload jwtPayload, JWTValidatorConfig config) (boolean) {
    foreach audience in jwtPayload.aud {
        if (audience == config.audience) {
            return true;
        }
    }
    return false;
}

function validateExpirationTime (Payload jwtPayload) (boolean) {
    return jwtPayload.exp > time:currentTime().time;
}

function validateNotBeforeTime (Payload jwtPayload) (boolean) {
    return time:currentTime().time > jwtPayload.nbf;
}

function convertToStringArray (json jsonData) (string[]) {
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

function urlDecode (string encodedString) (string) {
    string decodedString = encodedString.replaceAll("-", "+");
    decodedString = decodedString.replaceAll("_", "/");
    return decodedString;
}

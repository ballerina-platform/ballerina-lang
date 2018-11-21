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

import ballerina/io;

# Represents JWT issuer configurations.
# + keyAlias - Key alias used for signing
# + keyPassword - Key password used for signing
# + keyStoreFilePath - Key store file path
# + keyStorePassword - Key store password
public type JWTIssuerConfig record {
    string keyAlias = "";
    string keyPassword = "";
    string keyStoreFilePath = "";
    string keyStorePassword = "";
    !...
};

# Issue a JWT token.
#
# + header - JwtHeader object
# + payload - JwtPayload object
# + config - JWTIssuerConfig object
# + return - JWT token string or an error if token validation fails
public function issue(JwtHeader header, JwtPayload payload, JWTIssuerConfig config) returns (string|error) {
    string jwtHeader = check createHeader(header);
    string jwtPayload = check createPayload(payload);
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    KeyStore keyStore = {
        keyAlias : config.keyAlias,
        keyPassword : config.keyPassword,
        keyStoreFilePath : config.keyStoreFilePath,
        keyStorePassword : config.keyStorePassword
    };
    string signature = sign(jwtAssertion, header.alg, keyStore);
    return (jwtAssertion + "." + signature);
}

function createHeader(JwtHeader header) returns (string|error) {
    json headerJson = {};
    if (!validateMandatoryJwtHeaderFields(header)) {
        error jwtError = error(INTERNAL_ERROR_CODE, { message : "Mandatory field signing algorithm(alg) is empty." });
        return jwtError;
    }
    headerJson[ALG] = header.alg;
    headerJson[TYP] = "JWT";
    var customClaims = header["customClaims"];
    if (customClaims is map) {
        headerJson = addMapToJson(headerJson, customClaims);
    }
    string headerValInString = headerJson.toString();
    string encodedPayload = check headerValInString.base64Encode();
    return encodedPayload;
}

function createPayload(JwtPayload payload) returns (string|error) {
    json payloadJson = {};
    if (!validateMandatoryFields(payload)) {
        error jwtError = error(INTERNAL_ERROR_CODE,
                            { message : "Mandatory fields(Issuer, Subject, Expiration time or Audience) are empty." });
        return jwtError;
    }
    payloadJson[SUB] = payload.sub;
    payloadJson[ISS] = payload.iss;
    payloadJson[EXP] = payload.exp;
    var iat = payload["iat"];
    if (iat is int) {
        payloadJson[IAT] = iat;
    }
    var jti = payload["jti"];
    if (jti is string) {
        payloadJson[JTI] = jti;
    }
    payloadJson[AUD] = convertStringArrayToJson(payload.aud);
    var customClaims = payload["customClaims"];
    if (customClaims is map) {
        payloadJson = addMapToJson(payloadJson, customClaims);
    }
    string payloadInString = payloadJson.toString();
    return payloadInString.base64Encode();
}

function addMapToJson(json inJson, map mapToConvert) returns (json) {
    if (mapToConvert.length() != 0) {
        foreach key in mapToConvert.keys() {
            var customClaims = mapToConvert[key];
            if (customClaims is string[]) {
                inJson[key] = convertStringArrayToJson(customClaims);
            } else if (customClaims is int[]) {
                inJson[key] = convertIntArrayToJson(customClaims);
            } else if (customClaims is string) {
                inJson[key] = customClaims;
            } else if (customClaims is int) {
                inJson[key] = customClaims;
            } else if (customClaims is boolean) {
                inJson[key] = customClaims;
            }
        }
    }
    return inJson;
}

function convertStringArrayToJson(string[] arrayToConvert) returns (json) {
    json jsonPayload = [];
    int i = 0;
    while (i < arrayToConvert.length()) {
        jsonPayload[i] = arrayToConvert[i];
        i = i + 1;
    }
    return jsonPayload;
}

function convertIntArrayToJson(int[] arrayToConvert) returns (json) {
    json jsonPayload = [];
    int i = 0;
    while (i < arrayToConvert.length()) {
        jsonPayload[i] = arrayToConvert[i];
        i = i + 1;
    }
    return jsonPayload;
}

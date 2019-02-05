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

import ballerina/encoding;
import ballerina/crypto;

# Issue a JWT token.
#
# + header - JwtHeader object
# + payload - JwtPayload object
# + keyStore - Keystore to be used in JWT signing
# + keyAlias - Signing key alias
# + keyPassword - Signing key password
# + return - JWT token string or an error if token validation fails
public function issueJwt(JwtHeader header, JwtPayload payload, crypto:KeyStore keyStore, string keyAlias,
                         string keyPassword) returns (string|error) {
    string jwtHeader = check buildHeaderString(header);
    string jwtPayload = check buildPayloadString(payload);
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    string signature = sign(jwtAssertion, header.alg, keyStore, keyAlias, keyPassword);
    return (jwtAssertion + "." + signature);
}

function buildHeaderString(JwtHeader header) returns (string|error) {
    json headerJson = {};
    if (!validateMandatoryJwtHeaderFields(header)) {
        error jwtError = error(AUTH_ERROR_CODE, { message : "Mandatory field signing algorithm(alg) is empty." });
        return jwtError;
    }
    headerJson[ALG] = header.alg;
    headerJson[TYP] = "JWT";
    var customClaims = header["customClaims"];
    if (customClaims is map<any>) {
        headerJson = addMapToJson(headerJson, customClaims);
    }
    string headerValInString = headerJson.toString();
    string encodedPayload = encoding:encodeBase64(headerValInString.toByteArray("UTF-8"));
    return encodedPayload;
}

function buildPayloadString(JwtPayload payload) returns (string|error) {
    json payloadJson = {};
    if (!validateMandatoryFields(payload)) {
        error jwtError = error(AUTH_ERROR_CODE,
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
    if (customClaims is map<any>) {
        payloadJson = addMapToJson(payloadJson, customClaims);
    }
    string payloadInString = payloadJson.toString();
    return encoding:encodeBase64(payloadInString.toByteArray("UTF-8"));
}

function addMapToJson(json inJson, map<any> mapToConvert) returns (json) {
    if (mapToConvert.length() != 0) {
        foreach var key in mapToConvert.keys() {
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

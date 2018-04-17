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

import ballerina/util;
import ballerina/io;

@Description {value:"Represents JWT issuer configurations"}
public type JWTIssuerConfig {
    string certificateAlias;
    string keyPassword;
};

@Description {value:"Issue a JWT token"}
@Param {value:"header: Header object"}
@Param {value:"payload: Payload object"}
@Param {value:"config: JWTIssuerConfig object"}
@Return {value:"string: JWT token string"}
@Return {value:"error: If token validation fails "}
public function issue (Header header, Payload payload, JWTIssuerConfig config) returns (string|error) {
    string jwtHeader = createHeader(header);
    string jwtPayload = "";
    match createPayload(payload) {
        error e => return e;
        string result => jwtPayload = result;
    }
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    string signature = sign(jwtAssertion, header.alg, config.certificateAlias, config.keyPassword);
    return (jwtAssertion + "." + signature);
}

function createHeader (Header header) returns (string) {
    json headerJson = {};
    headerJson[ALG] = header.alg;
    headerJson[TYP] = "JWT";
    headerJson = addMapToJson(headerJson, header.customClaims);
    string headerValInString = headerJson.toString();
    string encodedPayload = check util:base64EncodeString(headerValInString);
    return encodedPayload;
}

function createPayload (Payload payload) returns (string|error) {
    json payloadJson = {};
    if (!validateMandatoryFields(payload)) {
        error err = {message:"Mandatory fields(Issuer, Subject, Expiration time or Audience) are empty."};
        return err;
    }
    payloadJson[SUB] = payload.sub;
    payloadJson[ISS] = payload.iss;
    payloadJson[EXP] = payload.exp;
    payloadJson[IAT] = payload.iat;
    if (payload.jti != "") {
        payloadJson[JTI] = payload.jti;
    }
    payloadJson[AUD] = convertStringArrayToJson(payload.aud);
    payloadJson = addMapToJson(payloadJson, payload.customClaims);
    string payloadInString = payloadJson.toString();
    return util:base64EncodeString(payloadInString);
}

function addMapToJson (json inJson, map mapToConvert) returns (json) {
    if (lengthof mapToConvert != 0) {
        foreach key in mapToConvert.keys() {
            match mapToConvert[key]{
                string[] value => inJson[key] = convertStringArrayToJson(value);
                int[] value => inJson[key] = convertIntArrayToJson(value);
                string value => inJson[key] = value;
                int value => inJson[key] = value;
                boolean value => inJson[key] = value;
                any => {}
            }
        }
    }
    return inJson;
}

function convertStringArrayToJson (string[] arrayToConvert) returns (json) {
    json jsonPayload = [];
    int i = 0;
    while (i < lengthof arrayToConvert) {
        jsonPayload[i] = arrayToConvert[i];
        i = i + 1;
    }
    return jsonPayload;
}

function convertIntArrayToJson (int[] arrayToConvert) returns (json) {
    json jsonPayload = [];
    int i = 0;
    while (i < lengthof arrayToConvert) {
        jsonPayload[i] = arrayToConvert[i];
        i = i + 1;
    }
    return jsonPayload;
}

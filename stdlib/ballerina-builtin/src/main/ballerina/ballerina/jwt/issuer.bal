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

import ballerina.util;
import ballerina.jwt.signature;

@Description {value:"Represents JWT issuer configurations"}
public struct JWTIssuerConfig {
    string certificateAlias;
    string keyPassword;
}

@Description {value:"Issue a JWT token"}
@Param {value:"header: Header object"}
@Param {value:"payload: Payload object"}
@Param {value:"config: JWTIssuerConfig object"}
@Return {value:"string: JWT token string"}
@Return {value:"error: If token validation fails "}
public function issue (Header header, Payload payload, JWTIssuerConfig config) (string, error) {
    string jwtHeader = createHeader(header);
    var jwtPayload, err = createPayload(payload);
    if (err != null) {
        return null, err;
    }
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    string signature = signature:sign(jwtAssertion, header.alg, config.certificateAlias, config.keyPassword);
    return jwtAssertion + "." + signature, null;
}

function createHeader (Header header) (string) {
    json headerJson = {};
    headerJson[ALG] = header.alg;
    headerJson[TYP] = "JWT";
    headerJson = addMapToJson(headerJson, header.customClaims);
    return urlEncode(util:base64Encode(headerJson.toString()));
}

function createPayload (Payload payload) (string, error) {
    json payloadJson = {};
    if (!validateMandatoryFields(payload)) {
        error err = {message:"Mandatory fields(Issuer, Subject, Expiration time or Audience) are empty."};
        return null, err;
    }
    payloadJson[SUB] = payload.sub;
    payloadJson[ISS] = payload.iss;
    payloadJson[EXP] = payload.exp;
    payloadJson[IAT] = payload.iat;
    if (payload.jti != null) {
        payloadJson[JTI] = payload.jti;
    }
    payloadJson[AUD] = convertStringArrayToJson(payload.aud);
    payloadJson = addMapToJson(payloadJson, payload.customClaims);
    return urlEncode(util:base64Encode(payloadJson.toString())), null;
}

function urlEncode (string data) (string) {
    string encodedString = data.replaceAll("\\+", "-");
    encodedString = encodedString.replaceAll("/", "_");
    return encodedString;
}

function addMapToJson (json inJson, map mapToConvert) (json) {
    if (mapToConvert != null) {
        foreach key in mapToConvert.keys() {
            if (typeof mapToConvert[key] == typeof string[]) {
                var inputArray, e = (string[])mapToConvert[key];
                inJson[key] = convertStringArrayToJson(inputArray);
            } else if (typeof mapToConvert[key] == typeof int[]) {
                var inputArray, e = (int[])mapToConvert[key];
                inJson[key] = convertIntArrayToJson(inputArray);
            } else if (typeof mapToConvert[key] == typeof string) {
                var inputString, _ = (string)mapToConvert[key];
                inJson[key] = inputString;
            } else if (typeof mapToConvert[key] == typeof int) {
                var inputInt, _ = (int)mapToConvert[key];
                inJson[key] = inputInt;
            } else if (typeof mapToConvert[key] == typeof boolean) {
                var inputBool, _ = (boolean)mapToConvert[key];
                inJson[key] = inputBool;
            }
        }
    }
    return inJson;
}

function convertStringArrayToJson (string[] arrayToConvert) (json) {
    json jsonPayload = [];
    int i = 0;
    while (i < lengthof arrayToConvert) {
        jsonPayload[i] = arrayToConvert[i];
        i = i + 1;
    }
    return jsonPayload;
}

function convertIntArrayToJson (int[] arrayToConvert) (json) {
    json jsonPayload = [];
    int i = 0;
    while (i < lengthof arrayToConvert) {
        jsonPayload[i] = arrayToConvert[i];
        i = i + 1;
    }
    return jsonPayload;
}

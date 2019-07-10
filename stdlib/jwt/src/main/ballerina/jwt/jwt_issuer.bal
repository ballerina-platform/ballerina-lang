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

import ballerina/crypto;
import ballerina/encoding;

# Represents JWT issuer configurations.
#
# + keyStore - Keystore to be used in JWT signing
# + keyAlias - Signing key alias
# + keyPassword - Signing key password
public type JwtIssuerConfig record {|
    crypto:KeyStore keyStore;
    string keyAlias;
    string keyPassword;
|};

# Issue a JWT token based on provided header and payload. JWT will be signed (JWS) if `keyStore` information is provided
# in the `JwtIssuerConfig` and the `alg` field of `JwtHeader` is not `NONE`.
#
# + header - JwtHeader object
# + payload - JwtPayload object
# + config - JWT issuer config record
# + return - JWT token string or an error if token validation fails
public function issueJwt(JwtHeader header, JwtPayload payload, JwtIssuerConfig? config) returns string|error {
    string jwtHeader = check buildHeaderString(header);
    string jwtPayload = check buildPayloadString(payload);
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    JwtSigningAlgorithm? alg = header?.alg;
    if (alg is JwtSigningAlgorithm) {
        if (alg == NONE) {
            return jwtAssertion;
        } else {
            if (config is JwtIssuerConfig) {
                crypto:KeyStore keyStore = config.keyStore;
                string keyAlias = config.keyAlias;
                string keyPassword = config.keyPassword;
                var privateKey = check crypto:decodePrivateKey(keyStore = keyStore, keyAlias = keyAlias,
                                                               keyPassword = keyPassword);
                string signature = "";
                if (alg == RS256) {
                    signature = encoding:encodeBase64Url(check crypto:signRsaSha256(jwtAssertion.toBytes(),
                                                                                    privateKey));
                } else if (alg == RS384) {
                    signature = encoding:encodeBase64Url(check crypto:signRsaSha384(jwtAssertion.toBytes(),
                                                                                    privateKey));
                } else if (alg == RS512) {
                    signature = encoding:encodeBase64Url(check crypto:signRsaSha512(jwtAssertion.toBytes(),
                                                                                    privateKey));
                } else {
                    return prepareError("Unsupported JWS algorithm.");
                }
                return jwtAssertion + "." + signature;
            } else {
                return prepareError("Signing JWT requires JwtIssuerConfig with keystore information.");
            }
        }
    }
    //TODO: Define a proper error
    return prepareError("JwtSigningAlgorithm is not found.");
}

function buildHeaderString(JwtHeader header) returns string|error {
    map<json> headerJson = {};
    if (!validateMandatoryJwtHeaderFields(header)) {
        return prepareError("Mandatory field signing algorithm (alg) is empty.");
    }
    JwtSigningAlgorithm? alg = header?.alg;
    if (alg is JwtSigningAlgorithm) {
        if (alg == RS256) {
            headerJson[ALG] = "RS256";
        } else if (alg == RS384) {
            headerJson[ALG] = "RS384";
        } else if (alg == RS512) {
            headerJson[ALG] = "RS512";
        } else if (alg == NONE) {
            headerJson[ALG] = "none";
        } else {
            return prepareError("Unsupported JWS algorithm.");
        }
        if (header["typ"] is string) {
            string? typ = header?.typ;
            if (typ is string) {
                headerJson[TYP] = typ;
            }
        }
        if (header["cty"] is string) {
            string? cty = header?.cty;
            if (cty is string) {
                headerJson[CTY] = cty;
            }
        }
        if (header["kid"] is string) {
            string? kid = header?.kid;
            if (kid is string) {
                headerJson[KID] = kid;
            }
        }
    }
    string headerValInString = headerJson.toString();
    string encodedPayload = encoding:encodeBase64Url(headerValInString.toBytes());
    return encodedPayload;
}

function buildPayloadString(JwtPayload payload) returns string|error {
    map<json> payloadJson = {};
    var sub = payload["sub"];
    if (sub is string) {
        payloadJson[SUB] = sub;
    }
    var iss = payload["iss"];
    if (iss is string) {
        payloadJson[ISS] = iss;
    }
    var exp = payload["exp"];
    if (exp is int) {
        payloadJson[EXP] = exp;
    }
    var iat = payload["iat"];
    if (iat is int) {
        payloadJson[IAT] = iat;
    }
    var jti = payload["jti"];
    if (jti is string) {
        payloadJson[JTI] = jti;
    }
    var aud = payload["aud"];
    if (aud is string) {
        payloadJson[AUD] = aud;
    } else if (aud is string[]) {
        payloadJson[AUD] = aud;
    }
    var customClaims = payload["customClaims"];
    if (customClaims is map<json>) {
        payloadJson = addMapToJson(payloadJson, customClaims);
    }
    string payloadInString = payloadJson.toString();
    return encoding:encodeBase64Url(payloadInString.toBytes());
}

function addMapToJson(map<json> inJson, map<json> mapToConvert) returns map<json> {
    if (mapToConvert.length() != 0) {
        foreach var key in mapToConvert.keys() {
            inJson[key] = mapToConvert[key];
        }
    }
    return inJson;
}

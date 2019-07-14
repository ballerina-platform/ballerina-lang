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
# + return - JWT token string or an `JwtError` if token validation fails
public function issueJwt(JwtHeader header, JwtPayload payload, JwtIssuerConfig? config) returns string|JwtError {
    string jwtHeader = check buildHeaderString(header);
    string jwtPayload = check buildPayloadString(payload);
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    if (header.alg == NONE) {
        return jwtAssertion;
    } else {
        if (config is JwtIssuerConfig) {
            crypto:KeyStore keyStore = config.keyStore;
            string keyAlias = config.keyAlias;
            string keyPassword = config.keyPassword;
            var privateKey = crypto:decodePrivateKey(keyStore = keyStore, keyAlias = keyAlias,
                                                     keyPassword = keyPassword);
            if (privateKey is crypto:PrivateKey) {
                if (header.alg == RS256) {
                    var signature = crypto:signRsaSha256(jwtAssertion.toByteArray("UTF-8"), privateKey);
                    if (signature is byte[]) {
                        return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                    } else {
                        return prepareJwtError("Private key signing failed for SHA256 algorithm.", err = signature);
                    }
                } else if (header.alg == RS384) {
                    var signature = crypto:signRsaSha384(jwtAssertion.toByteArray("UTF-8"), privateKey);
                    if (signature is byte[]) {
                        return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                    } else {
                        return prepareJwtError("Private key signing failed for SHA384 algorithm.", err = signature);
                    }
                } else if (header.alg == RS512) {
                    var signature = crypto:signRsaSha512(jwtAssertion.toByteArray("UTF-8"), privateKey);
                    if (signature is byte[]) {
                        return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                    } else {
                        return prepareJwtError("Private key signing failed for SHA512 algorithm.", err = signature);
                    }
                } else {
                    return prepareJwtError("Unsupported JWS algorithm.");
                }
            } else {
                return prepareJwtError("Private key decoding failed.", err = privateKey);
            }
        } else {
            return prepareJwtError("Signing JWT requires JwtIssuerConfig with keystore information.");
        }
    }
}

function buildHeaderString(JwtHeader header) returns string|JwtError {
    json headerJson = {};
    if (!validateMandatoryJwtHeaderFields(header)) {
        return prepareJwtError("Mandatory field signing algorithm (alg) is empty.");
    }
    if (header.alg == RS256) {
        headerJson[ALG] = "RS256";
    } else if (header.alg == RS384) {
        headerJson[ALG] = "RS384";
    } else if (header.alg == RS512) {
        headerJson[ALG] = "RS512";
    } else if (header.alg == NONE) {
        headerJson[ALG] = "none";
    } else {
        return prepareJwtError("Unsupported JWS algorithm.");
    }
    if (header["typ"] is string) {
        headerJson[TYP] = header.typ;
    }
    if (header["cty"] is string) {
        headerJson[CTY] = header.cty;
    }
    if (header["kid"] is string) {
        headerJson[KID] = header.kid;
    }
    string headerValInString = headerJson.toString();
    string encodedPayload = encoding:encodeBase64Url(headerValInString.toByteArray("UTF-8"));
    return encodedPayload;
}

function buildPayloadString(JwtPayload payload) returns string|JwtError {
    json payloadJson = {};
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
    return encoding:encodeBase64Url(payloadInString.toByteArray("UTF-8"));
}

function addMapToJson(json inJson, map<json> mapToConvert) returns json {
    if (mapToConvert.length() != 0) {
        foreach var key in mapToConvert.keys() {
            inJson[key] = mapToConvert[key];
        }
    }
    return inJson;
}

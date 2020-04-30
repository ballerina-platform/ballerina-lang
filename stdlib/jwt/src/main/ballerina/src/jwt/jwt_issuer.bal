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

# Represents JWT validator configurations.
#
# + username - JWT username
# + issuer - JWT issuer
# + audience - JWT audience
# + customClaims - Map of custom claims
# + expTimeInSeconds - Expiry time in seconds
# + keyStoreConfig - JWT key store configurations
# + signingAlg - Signing algorithm
public type JwtIssuerConfig record {|
    string username?;
    string issuer;
    string[] audience;
    map<json> customClaims?;
    int expTimeInSeconds = 300;
    JwtKeyStoreConfig keyStoreConfig;
    JwtSigningAlgorithm signingAlg = RS256;
|};

# Represents JWT key store configurations.
#
# + keyStore - Keystore to be used in JWT signing
# + keyAlias - Signing key alias
# + keyPassword - Signing key password
public type JwtKeyStoreConfig record {|
    crypto:KeyStore keyStore;
    string keyAlias;
    string keyPassword;
|};

# Issues a JWT based on the provided header and payload. JWT will be signed (JWS) if `crypto:KeyStore` information is
# provided in the `jwt:JwtKeyStoreConfig` and the `alg` field of the `jwt:JwtHeader` is not `jwt:NONE`.
# ```ballerina
# string|jwt:Error jwt = jwt:issueJwt(header, payload, keyStoreConfig);
# ```
#
# + header - JwtHeader object
# + payload - JwtPayload object
# + config - JWT key store config record
# + return - JWT as a `string` or else a `jwt:Error` if token validation fails
public function issueJwt(JwtHeader header, JwtPayload payload, JwtKeyStoreConfig? config) returns string|Error {
    string jwtHeader = check buildHeaderString(header);
    string jwtPayload = check buildPayloadString(payload);
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    JwtSigningAlgorithm? alg = header?.alg;
    if (alg is ()) {
        return prepareError("Failed to issue JWT since signing algorithm is not specified.");
    }

    JwtSigningAlgorithm algorithm = <JwtSigningAlgorithm>alg;
    match (algorithm) {
        NONE => {
            return jwtAssertion;
        }
        _ => {
            if (config is ()) {
                return prepareError("Signing JWT requires JwtKeyStoreConfig with keystore information.");
            }

            JwtKeyStoreConfig keyStoreConfig = <JwtKeyStoreConfig>config;
            crypto:KeyStore keyStore = keyStoreConfig.keyStore;
            string keyAlias = keyStoreConfig.keyAlias;
            string keyPassword = keyStoreConfig.keyPassword;
            crypto:PrivateKey|crypto:Error decodedResults = crypto:decodePrivateKey(keyStore, keyAlias, keyPassword);
            if (decodedResults is crypto:Error) {
                return prepareError("Private key decoding failed.", decodedResults);
            }
            crypto:PrivateKey privateKey = <crypto:PrivateKey>decodedResults;
            match (algorithm) {
                RS256 => {
                    byte[]|crypto:Error signature = crypto:signRsaSha256(jwtAssertion.toBytes(), privateKey);
                    if (signature is byte[]) {
                        return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                    } else {
                        return prepareError("Private key signing failed for SHA256 algorithm.", signature);
                    }
                }
                RS384 => {
                    byte[]|crypto:Error signature = crypto:signRsaSha384(jwtAssertion.toBytes(), privateKey);
                    if (signature is byte[]) {
                        return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                    } else {
                        return prepareError("Private key signing failed for SHA384 algorithm.", signature);
                    }
                }
                RS512 => {
                    byte[]|crypto:Error signature = crypto:signRsaSha512(jwtAssertion.toBytes(), privateKey);
                    if (signature is byte[]) {
                        return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                    } else {
                        return prepareError("Private key signing failed for SHA512 algorithm.", signature);
                    }
                }
                _ => {
                    return prepareError("Unsupported JWS algorithm.");
                }
            }
        }
    }
}

# Builds the header string from the `jwt:JwtHeader` record.
# ```ballerina
# string|jwt:Error jwtHeader = buildHeaderString(header);
# ```
#
# + header - JWT header record to be built as a string
# + return - The header string or else a `jwt:Error` if building the string fails
public function buildHeaderString(JwtHeader header) returns string|Error {
    map<json> headerJson = {};
    if (!validateMandatoryJwtHeaderFields(header)) {
        return prepareError("Mandatory field signing algorithm (alg) is empty.");
    }
    JwtSigningAlgorithm? alg = header?.alg;
    if (alg is JwtSigningAlgorithm) {
        match (alg) {
            RS256 => {
                headerJson[ALG] = "RS256";
            }
            RS384 => {
                headerJson[ALG] = "RS384";
            }
            RS512 => {
                headerJson[ALG] = "RS512";
            }
            NONE => {
                headerJson[ALG] = "none";
            }
            _ => {
                return prepareError("Unsupported JWS algorithm.");
            }
        }

        string? typ = header?.typ;
        if (typ is string) {
            headerJson[TYP] = typ;
        }
        string? cty = header?.cty;
        if (cty is string) {
            headerJson[CTY] = cty;
        }
        string? kid = header?.kid;
        if (kid is string) {
            headerJson[KID] = kid;
        }
    }
    string headerValInString = headerJson.toJsonString();
    string encodedPayload = encoding:encodeBase64Url(headerValInString.toBytes());
    return encodedPayload;
}

# Builds the payload string from the `jwt:JwtPayload` record.
# ```ballerina
# string|jwt:Error jwtPayload = jwt:buildPayloadString(payload);
# ```
#
# + payload - JWT payload record to be built as a string
# + return - The payload string or else a `jwt:Error` if building the string fails
public function buildPayloadString(JwtPayload payload) returns string|Error {
    map<json> payloadJson = {};
    string? sub = payload?.sub;
    if (sub is string) {
        payloadJson[SUB] = sub;
    }
    string? iss = payload?.iss;
    if (iss is string) {
        payloadJson[ISS] = iss;
    }
    int? exp = payload?.exp;
    if (exp is int) {
        payloadJson[EXP] = exp;
    }
    int? iat = payload?.iat;
    if (iat is int) {
        payloadJson[IAT] = iat;
    }
    string? jti = payload?.jti;
    if (jti is string) {
        payloadJson[JTI] = jti;
    }
    string|string[]? aud = payload?.aud;
    if (aud is string) {
        payloadJson[AUD] = aud;
    } else if (aud is string[]) {
        payloadJson[AUD] = aud;
    }
    int? nbf = payload?.nbf;
    if (nbf is int) {
        payloadJson[NBF] = nbf;
    }
    map<json>? customClaims = payload?.customClaims;
    if (customClaims is map<json> && customClaims.length() > 0) {
        payloadJson = appendToMap(customClaims, payloadJson);
    }
    string payloadInString = payloadJson.toJsonString();
    return encoding:encodeBase64Url(payloadInString.toBytes());
}

function appendToMap(map<json> fromMap, map<json> toMap) returns map<json> {
    foreach json key in fromMap.keys() {
        toMap[key] = fromMap[key];
    }
    return toMap;
}

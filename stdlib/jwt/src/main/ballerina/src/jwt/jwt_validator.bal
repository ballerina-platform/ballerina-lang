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

import ballerina/cache;
import ballerina/crypto;
import ballerina/encoding;
import ballerina/http;
import ballerina/io;
import ballerina/lang.'int as langint;
import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/stringutils;
import ballerina/time;

# Represents JWT validator configurations.
#
# + issuer - Expected issuer
# + audience - Expected audience
# + clockSkewInSeconds - Clock skew in seconds
# + signatureConfig - JWT signature configurations
# + jwtCache - Cache used to store parsed JWT information
public type JwtValidatorConfig record {|
    string issuer?;
    string|string[] audience?;
    int clockSkewInSeconds = 0;
    JwtTrustStoreConfig|JwksConfig signatureConfig?;
    cache:Cache jwtCache = new;
|};

# Represents the JWKs endpoint configurations.
#
# + url - URL of the JWKs endpoint
# + clientConfig - HTTP client configurations which calls the JWKs endpoint
public type JwksConfig record {|
    string url;
    http:ClientConfiguration clientConfig = {};
|};

# Represents JWT trust store configurations.
#
# + trustStore - Trust store used for signature verification
# + certificateAlias - Token signed public key certificate alias
public type JwtTrustStoreConfig record {|
    crypto:TrustStore trustStore;
    string certificateAlias;
|};

# Validates the given JWT string.
#```ballerina
# jwt:JwtPayload|jwt:Error result = jwt:validateJwt(jwt, validatorConfig);
# ```
#
# + jwt - JWT that needs to be validated
# + config - JWT validator config record
# + return - JWT payload or else a `jwt:Error` if token validation fails
public function validateJwt(string jwt, @tainted JwtValidatorConfig config) returns @tainted (JwtPayload|Error) {
    if (config.jwtCache.hasKey(jwt)) {
        JwtPayload? payload = validateFromCache(config.jwtCache, jwt);
        if (payload is JwtPayload) {
            return payload;
        }
    }
    [JwtHeader, JwtPayload] [header, payload] = check decodeJwt(jwt);
    _ = check validateJwtRecords(jwt, header, payload, config);
    addToCache(config.jwtCache, jwt, payload);
    return payload;
}

function validateFromCache(cache:Cache jwtCache, string jwt) returns JwtPayload? {
    JwtPayload payload = <JwtPayload>jwtCache.get(jwt);
    int? expTime = payload?.exp;
    // convert to current time and check the expiry time
    if (expTime is () || expTime > (time:currentTime().time / 1000)) {
        log:printDebug(function() returns string {
            return "JWT validated from the cache. JWT payload: " + payload.toString();
        });
        return payload;
    } else {
        cache:Error? result = jwtCache.invalidate(jwt);
        if (result is cache:Error) {
            log:printDebug(function() returns string {
                return "Failed to invalidate JWT from the cache. JWT payload: " + payload.toString();
            });
        }
    }
}

function addToCache(cache:Cache jwtCache, string jwt, JwtPayload payload) {
    cache:Error? result = jwtCache.put(jwt, payload);
    if (result is cache:Error) {
        log:printDebug(function() returns string {
            return "Failed to add JWT to the cache. JWT payload: " + payload.toString();
        });
        return;
    }
    log:printDebug(function() returns string {
        return "JWT added to the cache. JWT payload: " + payload.toString();
    });
}

# Decodes the given JWT string.
# ```ballerina
# [jwt:JwtHeader, jwt:JwtPayload]|jwt:Error [header, payload] = jwt:decodeJwt(jwt);
# ```
#
# + jwt - JWT that needs to be decoded
# + return - The JWT header and payload tuple or else a `jwt:Error` if token decoding fails
public function decodeJwt(string jwt) returns @tainted ([JwtHeader, JwtPayload]|Error) {
    string[] encodedJwtComponents = check getJwtComponents(jwt);
    JwtHeader jwtHeader = check getJwtHeader(encodedJwtComponents[0]);
    JwtPayload jwtPayload = check getJwtPayload(encodedJwtComponents[1]);
    return [jwtHeader, jwtPayload];
}

function getJwtComponents(string jwt) returns string[]|Error {
    string[] jwtComponents = stringutils:split(jwt, "\\.");
    if (jwtComponents.length() < 2 || jwtComponents.length() > 3) {
        return prepareError("Invalid JWT.");
    }
    return jwtComponents;
}

function getJwtHeader(string encodedHeader) returns @tainted JwtHeader|Error {
    byte[]|error header = encoding:decodeBase64Url(encodedHeader);
    if (header is byte[]) {
        string|error result = strings:fromBytes(header);
        if (result is error) {
            return prepareError(result.message(), result);
        }
        string jwtHeader = <string>result;

        io:StringReader reader = new(jwtHeader);
        json|io:Error jsonHeader = reader.readJson();
        if (jsonHeader is io:Error) {
            return prepareError("String to JSON conversion failed for JWT header.", jsonHeader);
        }
        return parseHeader(<map<json>>jsonHeader);
    } else {
        return prepareError("Base64 url decode failed for JWT header.", header);
    }
}

function getJwtPayload(string encodedPayload) returns @tainted JwtPayload|Error {
    byte[]|error payload = encoding:decodeBase64Url(encodedPayload);
    if (payload is byte[]) {
        string|error result = strings:fromBytes(payload);
        if (result is error) {
            return prepareError(result.message(), result);
        }
        string jwtPayload = <string>result;

        io:StringReader reader = new(jwtPayload);
        json|io:Error jsonPayload = reader.readJson();
        if (jsonPayload is io:Error) {
            return prepareError("String to JSON conversion failed for JWT paylaod.", jsonPayload);
        }
        return parsePayload(<map<json>>jsonPayload);
    } else {
        return prepareError("Base64 url decode failed for JWT payload.", payload);
    }
}

function getJwtSignature(string encodedSignature) returns byte[]|Error {
    byte[]|encoding:Error signature = encoding:decodeBase64Url(encodedSignature);
    if (signature is encoding:Error) {
        return prepareError("Base64 url decode failed for JWT signature.", signature);
    }
    return <byte[]>signature;
}

function parseHeader(map<json> jwtHeaderJson) returns JwtHeader {
    JwtHeader jwtHeader = {};
    string[] keys = jwtHeaderJson.keys();
    foreach string key in keys {
        match (key) {
            ALG => {
                if (jwtHeaderJson[key].toJsonString() == "RS256") {
                    jwtHeader.alg = RS256;
                } else if (jwtHeaderJson[key].toJsonString() == "RS384") {
                    jwtHeader.alg = RS384;
                } else if (jwtHeaderJson[key].toJsonString() == "RS512") {
                    jwtHeader.alg = RS512;
                }
            }
            TYP => {
                jwtHeader.typ = jwtHeaderJson[key].toJsonString();
            }
            CTY => {
                jwtHeader.cty = jwtHeaderJson[key].toJsonString();
            }
            KID => {
                jwtHeader.kid = jwtHeaderJson[key].toJsonString();
            }
        }
    }
    return jwtHeader;
}

function parsePayload(map<json> jwtPayloadJson) returns JwtPayload|Error {
    JwtPayload jwtPayload = {};
    map<json> customClaims = {};
    string[] keys = jwtPayloadJson.keys();
    foreach string key in keys {
        match (key) {
            ISS => {
                jwtPayload.iss = jwtPayloadJson[key].toJsonString();
            }
            SUB => {
                jwtPayload.sub = jwtPayloadJson[key].toJsonString();
            }
            AUD => {
                jwtPayload.aud = check convertToStringArray(jwtPayloadJson[key]);
            }
            JTI => {
                jwtPayload.jti = jwtPayloadJson[key].toJsonString();
            }
            EXP => {
                string exp = jwtPayloadJson[key].toJsonString();
                int|error value = langint:fromString(exp);
                if (value is int) {
                    jwtPayload.exp = value;
                } else {
                    jwtPayload.exp = 0;
                }
            }
            NBF => {
                string nbf = jwtPayloadJson[key].toJsonString();
                int|error value = langint:fromString(nbf);
                if (value is int) {
                    jwtPayload.nbf = value;
                } else {
                    jwtPayload.nbf = 0;
                }
            }
            IAT => {
                string iat = jwtPayloadJson[key].toJsonString();
                int|error value = langint:fromString(iat);
                if (value is int) {
                    jwtPayload.iat = value;
                } else {
                    jwtPayload.iat = 0;
                }
            }
            _ => {
                customClaims[key] = jwtPayloadJson[key];
            }
        }
    }
    jwtPayload.customClaims = customClaims;
    return jwtPayload;
}

function validateJwtRecords(string jwt, JwtHeader jwtHeader, JwtPayload jwtPayload, JwtValidatorConfig config)
                            returns @tainted Error? {
    if (!validateMandatoryJwtHeaderFields(jwtHeader)) {
        return prepareError("Mandatory field signing algorithm (alg) is not provided in JOSE header.");
    }
    JwtSigningAlgorithm alg = <JwtSigningAlgorithm>jwtHeader?.alg;  // The `()` value is already validated.

    JwtTrustStoreConfig|JwksConfig? signatureConfig = config?.signatureConfig;
    if (signatureConfig is JwtTrustStoreConfig) {
        _ = check validateSignatureByTrustStore(jwt, alg, signatureConfig);
    } else if (signatureConfig is JwksConfig) {
        string? kid = jwtHeader?.kid;
        if (kid is string) {
            _ = check validateSignatureByJwks(jwt, kid, alg, signatureConfig);
        } else {
            return prepareError("Key ID (kid) is not provided in JOSE header.");
        }
    }

    string? iss = config?.issuer;
    if (iss is string) {
        _ = check validateIssuer(jwtPayload, iss);
    }
    string|string[]? aud = config?.audience;
    if (aud is string || aud is string[]) {
        _ = check validateAudience(jwtPayload, aud);
    }
    int? exp = jwtPayload?.exp;
    if (exp is int) {
        if (!validateExpirationTime(exp, config.clockSkewInSeconds)) {
            return prepareError("JWT is expired.");
        }
    }
    int? nbf = jwtPayload?.nbf;
    if (nbf is int) {
        if (!validateNotBeforeTime(nbf)) {
            return prepareError("JWT is used before Not_Before_Time (nbf).");
        }
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return ();
}

function validateMandatoryJwtHeaderFields(JwtHeader jwtHeader) returns boolean {
    JwtSigningAlgorithm? alg = jwtHeader?.alg;
    return alg is JwtSigningAlgorithm;
}

function validateCertificate(crypto:PublicKey publicKey) returns boolean|Error {
    time:Time|error result = time:toTimeZone(time:currentTime(), "GMT");
    if (result is error) {
        return prepareError(result.message(), result);
    }

    time:Time currTimeInGmt = <time:Time>result;
    int currTimeInGmtMillis = currTimeInGmt.time;

    crypto:Certificate? certificate = publicKey?.certificate;
    if (certificate is crypto:Certificate) {
        int notBefore = certificate.notBefore.time;
        int notAfter = certificate.notAfter.time;
        if (currTimeInGmtMillis >= notBefore && currTimeInGmtMillis <= notAfter) {
            return true;
        }
    }
    return false;
}

function validateSignatureByTrustStore(string jwt, JwtSigningAlgorithm alg, JwtTrustStoreConfig trustStoreConfig)
                                       returns Error? {
    crypto:PublicKey|crypto:Error publicKey = crypto:decodePublicKey(trustStoreConfig.trustStore,
                                                                     trustStoreConfig.certificateAlias);
    if (publicKey is crypto:Error) {
       return prepareError("Public key decode failed.", publicKey);
    }

    if (!check validateCertificate(<crypto:PublicKey>publicKey)) {
       return prepareError("Public key certificate validity period has passed.");
    }

    _ = check validateSignature(jwt, alg, <crypto:PublicKey>publicKey);
}

function validateSignatureByJwks(string jwt, string kid, JwtSigningAlgorithm alg, JwksConfig jwksConfig)
                                 returns @tainted Error? {
    json jwk = check getJwk(kid, jwksConfig);
    if (jwk is ()) {
        return prepareError("No JWK found for kid: " + kid);
    }
    string modulus = <string>jwk.n;
    string exponent = <string>jwk.e;
    crypto:PublicKey|crypto:Error publicKey = crypto:buildRsaPublicKey(modulus, exponent);
    if (publicKey is crypto:Error) {
       return prepareError("Public key generation failed.", publicKey);
    }
    _ = check validateSignature(jwt, alg, <crypto:PublicKey>publicKey);
}

function validateSignature(string jwt, JwtSigningAlgorithm alg, crypto:PublicKey publicKey) returns Error? {
    match (alg) {
        NONE => {
            return prepareError("Not a valid JWS. Signature algorithm is NONE.");
        }
        _ => {
            string[] encodedJwtComponents = check getJwtComponents(jwt);
            if (encodedJwtComponents.length() == 2) {
                return prepareError("Not a valid JWS. Signature is required.");
            }
            byte[] signature = check getJwtSignature(encodedJwtComponents[2]);
            string jwtHeaderPayloadPart = encodedJwtComponents[0] + "." + encodedJwtComponents[1];
            byte[] assertion = jwtHeaderPayloadPart.toBytes();
            boolean signatureValidation = check verifySignature(alg, assertion, signature, publicKey);
            if (!signatureValidation) {
               return prepareError("JWT signature validation has failed.");
            }
        }
    }
}

function getJwk(string kid, JwksConfig jwksConfig) returns @tainted (json|Error) {
    http:Client jwksClient = new(jwksConfig.url, jwksConfig.clientConfig);
    http:Response|http:ClientError response = jwksClient->get("");
    if (response is http:Response) {
        json|http:ClientError result = response.getJsonPayload();
        if (result is http:ClientError) {
            return prepareError(result.message(), result);
        }
        json payload = <json>result;
        json[] jwks = <json[]>payload.keys;
        foreach (json jwk in jwks) {
            if (jwk.kid == kid) {
                return jwk;
            }
        }
    } else {
        return prepareError(response.reason(), response);
    }
}

function verifySignature(JwtSigningAlgorithm alg, byte[] assertion, byte[] signaturePart, crypto:PublicKey publicKey)
                         returns boolean|Error {
    match (alg) {
        RS256 => {
            boolean|crypto:Error result = crypto:verifyRsaSha256Signature(assertion, signaturePart, publicKey);
            if (result is boolean) {
                return result;
            } else {
                return prepareError("SHA256 singature verification failed.", result);
            }
        }
        RS384 => {
            boolean|crypto:Error result = crypto:verifyRsaSha384Signature(assertion, signaturePart, publicKey);
            if (result is boolean) {
                return result;
            } else {
                return prepareError("SHA384 singature verification failed.", result);
            }
        }
        RS512 => {
            boolean|crypto:Error result = crypto:verifyRsaSha512Signature(assertion, signaturePart, publicKey);
            if (result is boolean) {
                return result;
            } else {
                return prepareError("SHA512 singature verification failed.", result);
            }
        }
        _ => {
            return prepareError("Unsupported JWS algorithm.");
        }
    }
}

function validateIssuer(JwtPayload jwtPayload, string issuerConfig) returns Error? {
    string? issuePayload = jwtPayload?.iss;
    if (issuePayload is string) {
        if (issuePayload != issuerConfig) {
            return prepareError("JWT contained invalid issuer name : " + issuePayload);
        }
    } else {
        return prepareError("JWT must contain a valid issuer name.");
    }
}

function validateAudience(JwtPayload jwtPayload, string|string[] audienceConfig) returns Error? {
    string|string[]? audiencePayload = jwtPayload?.aud;
    if (audiencePayload is string) {
        if (audienceConfig is string) {
            if (audiencePayload == audienceConfig) {
                return ();
            }
        } else {
            foreach string audience in audienceConfig {
                if (audience == audiencePayload) {
                    return ();
                }
            }
        }
        return prepareError("Invalid audience.");
    } else if (audiencePayload is string[]) {
        if (audienceConfig is string) {
            foreach string audience in audiencePayload {
                if (audience == audienceConfig) {
                    return ();
                }
            }
        } else {
            foreach string audienceC in audienceConfig {
                foreach string audienceP in audiencePayload {
                    if (audienceC == audienceP) {
                        return ();
                    }
                }
            }
        }
        return prepareError("Invalid audience.");
    } else {
        return prepareError("JWT must contain a valid audience.");
    }
}

function validateExpirationTime(int expTime, int clockSkew) returns boolean {
    //Convert current time which is in milliseconds to seconds.
    if (clockSkew > 0) {
        return expTime + clockSkew > time:currentTime().time / 1000;
    } else {
        return expTime > time:currentTime().time / 1000;
    }
}

function validateNotBeforeTime(int nbf) returns boolean {
    return time:currentTime().time > nbf;
}

function convertToStringArray(json jsonData) returns string[]|Error {
    if (jsonData is json[]) {
        string[] values = [];
        int i = 0;
        foreach json jsonVal in jsonData {
            values[i] = jsonVal.toJsonString();
            i = i + 1;
        }
        return values;
    } else {
        return [jsonData.toJsonString()];
    }
}

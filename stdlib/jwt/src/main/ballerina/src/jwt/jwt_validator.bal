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
import ballerina/internal;
import ballerina/io;
import ballerina/time;
import ballerina/'lang\.int as langint;

# Represents JWT validator configurations.
# + issuer - Expected issuer
# + audience - Expected audience
# + clockSkewInSeconds - Clock skew in seconds
# + trustStore - Trust store used for signature verification
# + certificateAlias - Token signed public key certificate alias
# + validateCertificate - Validate public key certificate notBefore and notAfter periods
# + jwtCache - Cache used to store parsed JWT information as CachedJwt
public type JwtValidatorConfig record {|
    string issuer?;
    string|string[] audience?;
    int clockSkewInSeconds = 0;
    crypto:TrustStore trustStore?;
    string certificateAlias?;
    boolean validateCertificate?;
    cache:Cache jwtCache = new(900000, 1000, 0.25);
|};

# Represents parsed and cached JWT.
#
# + jwtPayload - Parsed JWT payload
# + expiryTime - Expiry time of the JWT
public type CachedJwt record {|
    JwtPayload jwtPayload;
    int expiryTime;
|};

# Validity given JWT string.
#
# + jwtToken - JWT token that need to validate
# + config - JWT validator config record
# + return - If JWT token is valied return the JWT payload. An `Error` if token validation fails.
public function validateJwt(string jwtToken, JwtValidatorConfig config) returns @tainted (JwtPayload|Error) {
    string[] encodedJWTComponents = [];
    var jwtComponents = getJWTComponents(jwtToken);
    if (jwtComponents is string[]) {
        encodedJWTComponents = jwtComponents;
    } else {
        return jwtComponents;
    }

    string[] aud = [];
    JwtHeader header = {};
    JwtPayload payload = {};
    var decodedJwt = parseJWT(encodedJWTComponents);
    if (decodedJwt is [JwtHeader, JwtPayload]) {
        [header, payload] = decodedJwt;
    } else {
        return decodedJwt;
    }

    var jwtValidity = validateJwtRecords(encodedJWTComponents, header, payload, config);
    if (jwtValidity is Error) {
        return jwtValidity;
    } else {
        if (jwtValidity) {
            return payload;
        } else {
            return prepareError("Invalid JWT token.");
        }
    }
}

function getJWTComponents(string jwtToken) returns string[]|Error {
    string[] jwtComponents = internal:split(jwtToken, "\\.");
    if (jwtComponents.length() < 2 || jwtComponents.length() > 3) {
        return prepareError("Invalid JWT token.");
    }
    return jwtComponents;
}

function parseJWT(string[] encodedJWTComponents) returns @tainted ([JwtHeader, JwtPayload]|Error) {
    map<json> headerJson = {};
    map<json> payloadJson = {};
    var decodedJWTComponents = getDecodedJWTComponents(encodedJWTComponents);
    if (decodedJWTComponents is [map<json>, map<json>]) {
        [headerJson, payloadJson] = decodedJWTComponents;
    } else {
        return decodedJWTComponents;
    }

    JwtHeader jwtHeader = parseHeader(headerJson);
    JwtPayload jwtPayload = check parsePayload(payloadJson);
    return [jwtHeader, jwtPayload];
}

function getDecodedJWTComponents(string[] encodedJWTComponents) returns @tainted ([map<json>, map<json>]|Error) {
    string jwtHeader = "";
    string jwtPayload = "";

    var decodeResult = encoding:decodeBase64Url(encodedJWTComponents[0]);
    if (decodeResult is byte[]) {
        jwtHeader = encoding:byteArrayToString(decodeResult);
    } else {
        return prepareError("Base64 url decode failed for JWT header.", decodeResult);
    }

    decodeResult = encoding:decodeBase64Url(encodedJWTComponents[1]);
    if (decodeResult is byte[]) {
        jwtPayload = encoding:byteArrayToString(decodeResult);
    } else {
        return prepareError("Base64 url decode failed for JWT payload.", decodeResult);
    }

    json jwtHeaderJson = {};
    json jwtPayloadJson = {};

    io:StringReader reader = new(jwtHeader);
    var jsonHeader = reader.readJson();
    if (jsonHeader is json) {
        jwtHeaderJson = jsonHeader.cloneReadOnly();
    } else {
        return prepareError("String to JSON conversion failed for JWT header.", jsonHeader);
    }

    reader = new(jwtPayload);
    var jsonPayload = reader.readJson();
    if (jsonPayload is json) {
        jwtPayloadJson = jsonPayload.cloneReadOnly();
    } else {
        return prepareError("String to JSON conversion failed for JWT paylaod.", jsonPayload);
    }
    return [<map<json>>jwtHeaderJson, <map<json>>jwtPayloadJson];
}

function parseHeader(map<json> jwtHeaderJson) returns JwtHeader {
    JwtHeader jwtHeader = {};
    string[] keys = jwtHeaderJson.keys();
    foreach var key in keys {
        if (key == ALG) {
            if (jwtHeaderJson[key].toString() == "RS256") {
                jwtHeader.alg = RS256;
            } else if (jwtHeaderJson[key].toString() == "RS384") {
                jwtHeader.alg = RS384;
            } else if (jwtHeaderJson[key].toString() == "RS512") {
                jwtHeader.alg = RS512;
            }
        } else if (key == TYP) {
            jwtHeader.typ = jwtHeaderJson[key].toString();
        } else if (key == CTY) {
            jwtHeader.cty = jwtHeaderJson[key].toString();
        } else if (key == KID) {
            jwtHeader.kid = jwtHeaderJson[key].toString();
        }
    }
    return jwtHeader;
}

function parsePayload(map<json> jwtPayloadJson) returns JwtPayload|Error {
    string[] aud = [];
    JwtPayload jwtPayload = {};
    map<json> customClaims = {};
    string[] keys = jwtPayloadJson.keys();
    foreach var key in keys {
        if (key == ISS) {
            jwtPayload.iss = jwtPayloadJson[key].toString();
        } else if (key == SUB) {
            jwtPayload.sub = jwtPayloadJson[key].toString();
        } else if (key == AUD) {
            jwtPayload.aud = check convertToStringArray(jwtPayloadJson[key]);
        } else if (key == JTI) {
            jwtPayload.jti = jwtPayloadJson[key].toString();
        } else if (key == EXP) {
            string exp = jwtPayloadJson[key].toString();
            var value = langint:fromString(exp);
            if (value is int) {
                jwtPayload.exp = value;
            } else {
                jwtPayload.exp = 0;
            }
        } else if (key == NBF) {
            string nbf = jwtPayloadJson[key].toString();
            var value = langint:fromString(nbf);
            if (value is int) {
                jwtPayload.nbf = value;
            } else {
                jwtPayload.nbf = 0;
            }
        } else if (key == IAT) {
            string iat = jwtPayloadJson[key].toString();
            var value = langint:fromString(iat);
            if (value is int) {
                jwtPayload.iat = value;
            } else {
                jwtPayload.iat = 0;
            }
        } else {
            customClaims[key] = jwtPayloadJson[key];
        }
    }
    jwtPayload.customClaims = customClaims;
    return jwtPayload;
}

function validateJwtRecords(string[] encodedJWTComponents, JwtHeader jwtHeader, JwtPayload jwtPayload,
                            JwtValidatorConfig config) returns boolean|Error {
    if (!validateMandatoryJwtHeaderFields(jwtHeader)) {
        return prepareError("Mandatory field signing algorithm(alg) is empty in the given JWT.");
    }
    if (config?.validateCertificate is ()) {
        config.validateCertificate = true;
    }
    if (config?.validateCertificate == true && !check validateCertificate(config)) {
        return prepareError("Public key certificate validity period has passed.");
    }
    var trustStore = config?.trustStore;
    if (trustStore is crypto:TrustStore) {
        var signatureValidationResult = validateSignature(encodedJWTComponents, jwtHeader, config);
        if (signatureValidationResult is Error) {
            return signatureValidationResult;
        }
    }
    var iss = config?.issuer;
    if (iss is string) {
        var issuerStatus = validateIssuer(jwtPayload, config);
        if (issuerStatus is Error) {
            return issuerStatus;
        }
    }
    var aud = config?.audience;
    if (aud is string || aud is string[]) {
        var audienceStatus = validateAudience(jwtPayload, config);
        if (audienceStatus is Error) {
            return audienceStatus;
        }
    }
    var exp = jwtPayload?.exp;
    if (exp is int) {
        if (!validateExpirationTime(jwtPayload, config)) {
            return prepareError("JWT token is expired.");
        }
    }
    var nbf = jwtPayload?.nbf;
    if (nbf is int) {
        if (!validateNotBeforeTime(jwtPayload)) {
            return prepareError("JWT token is used before Not_Before_Time.");
        }
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return true;
}

function validateMandatoryJwtHeaderFields(JwtHeader jwtHeader) returns boolean {
    JwtSigningAlgorithm? alg = jwtHeader?.alg;
    if (alg is JwtSigningAlgorithm) {
        if (alg == "") {
            return false;
        }
        return true;
    }
    return false;
}

function validateCertificate(JwtValidatorConfig config) returns boolean|Error {
    var publicKey = crypto:decodePublicKey(config?.trustStore, config?.certificateAlias);
    if (publicKey is crypto:PublicKey) {
        time:Time currTimeInGmt = check time:toTimeZone(time:currentTime(), "GMT");
        int currTimeInGmtMillis = currTimeInGmt.time;

        var certificate = publicKey.certificate;
        if (certificate is crypto:Certificate) {
            int notBefore = certificate.notBefore.time;
            int notAfter = certificate.notAfter.time;
            if (currTimeInGmtMillis >= notBefore && currTimeInGmtMillis <= notAfter) {
                return true;
            }
        }
        return false;
    } else {
        return prepareError("Public key decode failed.", publicKey);
    }
}

function validateSignature(string[] encodedJWTComponents, JwtHeader jwtHeader, JwtValidatorConfig config)
                           returns boolean|Error {
    JwtSigningAlgorithm? alg = jwtHeader?.alg;
    if (alg is ()) {
        return prepareError("JwtSigningAlgorithm is not defined");
    }
    if (alg == NONE) {
        return prepareError("Not a valid JWS. Signature algorithm is NONE.");
    } else {
        if (encodedJWTComponents.length() == 2) {
            return prepareError("Not a valid JWS. Signature is required.");
        } else {
            string assertion = encodedJWTComponents[0] + "." + encodedJWTComponents[1];
            var signPart = encoding:decodeBase64Url(encodedJWTComponents[2]);
            if (signPart is byte[]) {
                var publicKey = crypto:decodePublicKey(config?.trustStore , config?.certificateAlias);
                if (publicKey is crypto:PublicKey) {
                    if (alg == RS256) {
                        var verification = crypto:verifyRsaSha256Signature(assertion.toBytes(), signPart, publicKey);
                        if (verification is boolean) {
                            return verification;
                        } else {
                            return prepareError("SHA256 singature verification failed.", verification);
                        }
                    } else if (alg == RS384) {
                        var verification = crypto:verifyRsaSha384Signature(assertion.toBytes(), signPart, publicKey);
                        if (verification is boolean) {
                            return verification;
                        } else {
                            return prepareError("SHA384 singature verification failed.", verification);
                        }
                    } else if (alg == RS512) {
                        var verification = crypto:verifyRsaSha512Signature(assertion.toBytes(), signPart, publicKey);
                        if (verification is boolean) {
                            return verification;
                        } else {
                            return prepareError("SHA512 singature verification failed.", verification);
                        }
                    } else {
                        return prepareError("Unsupported JWS algorithm.");
                    }
                } else {
                    return prepareError("Public key decode failed.", publicKey);
                }
            } else {
                return prepareError("Base64 url decode failed for JWT signature.", signPart);
            }
        }
    }
}

function validateIssuer(JwtPayload jwtPayload, JwtValidatorConfig config) returns Error? {
    string? issuePayload = jwtPayload?.iss;
    string? issuerConfig = config?.issuer;
    if (issuePayload is string && issuerConfig is string) {
        if (issuePayload != issuerConfig) {
            return prepareError("JWT contained invalid issuer name : " + issuePayload);
        }
    } else {
        return prepareError("JWT must contain a valid issuer name.");
    }
}

function validateAudience(JwtPayload jwtPayload, JwtValidatorConfig config) returns Error? {
    var audiencePayload = jwtPayload?.aud;
    var audienceConfig = config?.audience;
    if (audiencePayload is string) {
        if (audienceConfig is string) {
            if (audiencePayload == audienceConfig) {
                return ();
            }
        } else if (audienceConfig is string[]) {
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
        } else if (audienceConfig is string[]) {
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

function validateExpirationTime(JwtPayload jwtPayload, JwtValidatorConfig config) returns boolean {
    //Convert current time which is in milliseconds to seconds.
    int? expTime = jwtPayload?.exp;
    if (expTime is int) {
        if (config.clockSkewInSeconds > 0){
            expTime = expTime + config.clockSkewInSeconds;
        }
        return expTime > time:currentTime().time / 1000;
    }
    return false;
}

function validateNotBeforeTime(JwtPayload jwtPayload) returns boolean {
    return time:currentTime().time > (jwtPayload?.nbf ?: 0);
}

function convertToStringArray(json jsonData) returns string[]|Error {
    if (jsonData is json[]) {
        string[] values = [];
        int i = 0;
        foreach json jsonVal in jsonData {
            values[i] = jsonVal.toString();
            i = i + 1;
        }
        return values;
    } else {
        return [jsonData.toString()];
    }
}

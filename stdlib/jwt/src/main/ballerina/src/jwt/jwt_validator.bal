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
import ballerina/io;
import ballerina/lang.'int as langint;
import ballerina/lang.'string as strings;
import ballerina/stringutils;
import ballerina/time;

# Represents JWT validator configurations.
#
# + issuer - Expected issuer
# + audience - Expected audience
# + clockSkewInSeconds - Clock skew in seconds
# + trustStoreConfig - JWT trust store configurations
# + jwtCache - Cache used to store parsed JWT information as CachedJwt
public type JwtValidatorConfig record {|
    string issuer?;
    string|string[] audience?;
    int clockSkewInSeconds = 0;
    JwtTrustStoreConfig trustStoreConfig?;
    cache:Cache jwtCache = new(900000, 1000, 0.25);
|};

# Represents JWT trust store configurations.
#
# + trustStore - Trust store used for signature verification
# + certificateAlias - Token signed public key certificate alias
public type JwtTrustStoreConfig record {|
    crypto:TrustStore trustStore;
    string certificateAlias;
|};

# Represents parsed and cached JWT.
#
# + jwtPayload - Parsed JWT payload
# + expiryTime - Expiry time of the JWT
public type CachedJwt record {|
    JwtPayload jwtPayload;
    int expiryTime;
|};

# Validate the given JWT string.
#
# + jwtToken - JWT token that needs to be validated
# + config - JWT validator config record
# + return - If the JWT token is valid, return the JWT payload. Else, return an `Error` if token validation fails.
public function validateJwt(string jwtToken, JwtValidatorConfig config) returns @tainted (JwtPayload|Error) {
    JwtHeader header;
    JwtPayload payload;
    [header, payload]  = check decodeJwt(jwtToken);

    var jwtValidity = validateJwtRecords(jwtToken, header, payload, config);
    if (jwtValidity is ()) {
        return payload;
    } else {
        return jwtValidity;
    }
}

function getJwtComponents(string jwtToken) returns string[]|Error {
    string[] jwtComponents = stringutils:split(jwtToken, "\\.");
    if (jwtComponents.length() < 2 || jwtComponents.length() > 3) {
        return prepareError("Invalid JWT token.");
    }
    return jwtComponents;
}

# Decode the given JWT string.
#
# + jwtToken - JWT token that needs to be decoded
# + return - The JWT header and payload tuple or an `Error` if token decoding fails
public function decodeJwt(string jwtToken) returns @tainted ([JwtHeader, JwtPayload]|Error) {
    string[] encodedJwtComponents = check getJwtComponents(jwtToken);
    map<json> headerJson;
    map<json> payloadJson;
    var decodedJwtComponents = getDecodedJwtComponents(encodedJwtComponents);
    if (decodedJwtComponents is [map<json>, map<json>]) {
        [headerJson, payloadJson] = decodedJwtComponents;
    } else {
        return decodedJwtComponents;
    }

    JwtHeader jwtHeader = parseHeader(headerJson);
    JwtPayload jwtPayload = check parsePayload(payloadJson);
    return [jwtHeader, jwtPayload];
}

function getDecodedJwtComponents(string[] encodedJwtComponents) returns @tainted ([map<json>, map<json>]|Error) {
    string jwtHeader;
    string jwtPayload;

    var decodeResult = encoding:decodeBase64Url(encodedJwtComponents[0]);
    if (decodeResult is byte[]) {
        string|error result = strings:fromBytes(decodeResult);

        if (result is error) {
            return prepareError(result.reason(), result);
        }

        jwtHeader = <string> result;
    } else {
        return prepareError("Base64 url decode failed for JWT header.", decodeResult);
    }

    decodeResult = encoding:decodeBase64Url(encodedJwtComponents[1]);
    if (decodeResult is byte[]) {
        string|error result = strings:fromBytes(decodeResult);

        if (result is error) {
            return prepareError(result.reason(), result);
        }

        jwtPayload = <string> result;
    } else {
        return prepareError("Base64 url decode failed for JWT payload.", decodeResult);
    }

    json jwtHeaderJson;
    json jwtPayloadJson;

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
            if (jwtHeaderJson[key].toJsonString() == "RS256") {
                jwtHeader.alg = RS256;
            } else if (jwtHeaderJson[key].toJsonString() == "RS384") {
                jwtHeader.alg = RS384;
            } else if (jwtHeaderJson[key].toJsonString() == "RS512") {
                jwtHeader.alg = RS512;
            }
        } else if (key == TYP) {
            jwtHeader.typ = jwtHeaderJson[key].toJsonString();
        } else if (key == CTY) {
            jwtHeader.cty = jwtHeaderJson[key].toJsonString();
        } else if (key == KID) {
            jwtHeader.kid = jwtHeaderJson[key].toJsonString();
        }
    }
    return jwtHeader;
}

function parsePayload(map<json> jwtPayloadJson) returns JwtPayload|Error {
    JwtPayload jwtPayload = {};
    map<json> customClaims = {};
    string[] keys = jwtPayloadJson.keys();
    foreach var key in keys {
        if (key == ISS) {
            jwtPayload.iss = jwtPayloadJson[key].toJsonString();
        } else if (key == SUB) {
            jwtPayload.sub = jwtPayloadJson[key].toJsonString();
        } else if (key == AUD) {
            jwtPayload.aud = check convertToStringArray(jwtPayloadJson[key]);
        } else if (key == JTI) {
            jwtPayload.jti = jwtPayloadJson[key].toJsonString();
        } else if (key == EXP) {
            string exp = jwtPayloadJson[key].toJsonString();
            var value = langint:fromString(exp);
            if (value is int) {
                jwtPayload.exp = value;
            } else {
                jwtPayload.exp = 0;
            }
        } else if (key == NBF) {
            string nbf = jwtPayloadJson[key].toJsonString();
            var value = langint:fromString(nbf);
            if (value is int) {
                jwtPayload.nbf = value;
            } else {
                jwtPayload.nbf = 0;
            }
        } else if (key == IAT) {
            string iat = jwtPayloadJson[key].toJsonString();
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

function validateJwtRecords(string jwtToken, JwtHeader jwtHeader, JwtPayload jwtPayload,
                            JwtValidatorConfig config) returns Error? {
    if (!validateMandatoryJwtHeaderFields(jwtHeader)) {
        return prepareError("Mandatory field signing algorithm(alg) is empty in the given JWT.");
    }
    JwtTrustStoreConfig? trustStoreConfig = config?.trustStoreConfig;
    if (trustStoreConfig is JwtTrustStoreConfig) {
        if (!check validateCertificate(trustStoreConfig)) {
            return prepareError("Public key certificate validity period has passed.");
        }
        var signatureValidationResult = validateSignature(jwtToken, jwtHeader, trustStoreConfig);
        if (signatureValidationResult is Error) {
            return signatureValidationResult;
        }
    }
    var iss = config?.issuer;
    if (iss is string) {
        var issuerStatus = validateIssuer(jwtPayload, iss);
        if (issuerStatus is Error) {
            return issuerStatus;
        }
    }
    var aud = config?.audience;
    if (aud is string || aud is string[]) {
        var audienceStatus = validateAudience(jwtPayload, aud);
        if (audienceStatus is Error) {
            return audienceStatus;
        }
    }
    var exp = jwtPayload?.exp;
    if (exp is int) {
        if (!validateExpirationTime(exp, config.clockSkewInSeconds)) {
            return prepareError("JWT token is expired.");
        }
    }
    var nbf = jwtPayload?.nbf;
    if (nbf is int) {
        if (!validateNotBeforeTime(nbf)) {
            return prepareError("JWT token is used before Not_Before_Time.");
        }
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return ();
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

function validateCertificate(JwtTrustStoreConfig trustStoreConfig) returns boolean|Error {
    var publicKey = crypto:decodePublicKey(trustStoreConfig.trustStore, trustStoreConfig.certificateAlias);
    if (publicKey is crypto:PublicKey) {
        time:Time|error result = time:toTimeZone(time:currentTime(), "GMT");
        if (result is error) {
            return prepareError(result.reason(), result);
        }

        time:Time currTimeInGmt = <time:Time> result;
        int currTimeInGmtMillis = currTimeInGmt.time;

        var certificate = publicKey?.certificate;
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

function validateSignature(string jwtToken, JwtHeader jwtHeader, JwtTrustStoreConfig trustStoreConfig)
                           returns boolean|Error {
    JwtSigningAlgorithm? alg = jwtHeader?.alg;
    if (alg is ()) {
        return prepareError("JwtSigningAlgorithm is not defined");
    }
    if (alg == NONE) {
        return prepareError("Not a valid JWS. Signature algorithm is NONE.");
    } else {
        string[] encodedJwtComponents = check getJwtComponents(jwtToken);
        if (encodedJwtComponents.length() == 2) {
            return prepareError("Not a valid JWS. Signature is required.");
        } else {
            string assertion = encodedJwtComponents[0] + "." + encodedJwtComponents[1];
            var signPart = encoding:decodeBase64Url(encodedJwtComponents[2]);
            if (signPart is byte[]) {
                var publicKey = crypto:decodePublicKey(trustStoreConfig.trustStore , trustStoreConfig.certificateAlias);
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
    var audiencePayload = jwtPayload?.aud;
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
    if (clockSkew > 0){
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

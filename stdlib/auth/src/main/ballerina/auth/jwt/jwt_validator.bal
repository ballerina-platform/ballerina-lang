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
import ballerina/io;
import ballerina/log;
import ballerina/time;

# Represents JWT validator configurations.
# + issuer - Expected issuer
# + audience - Expected audience
# + clockSkew - Clock skew in seconds
# + trustStore - Trust store used for signature verification
# + certificateAlias - Token signed public key certificate alias
# + validateCertificate - Validate public key certificate notBefore and notAfter periods
public type JWTValidatorConfig record {|
    string issuer?;
    string[] audience?;
    int clockSkew = 0;
    crypto:TrustStore trustStore?;
    string certificateAlias?;
    boolean validateCertificate?;
|};

# Validity given JWT string.
#
# + jwtToken - JWT token that need to validate
# + config - JWT validator config record
# + return - If JWT token is valied return the JWT payload.
#            An error if token validation fails.
public function validateJwt(string jwtToken, JWTValidatorConfig config) returns JwtPayload|error {
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
    if (decodedJwt is (JwtHeader, JwtPayload)) {
        (header, payload) = decodedJwt;
    } else {
        return decodedJwt;
    }

    var jwtValidity = validateJwtRecords(encodedJWTComponents, header, payload, config);
    if (jwtValidity is error) {
        return jwtValidity;
    } else {
        if (jwtValidity) {
            return payload;
        } else {
            return prepareError("Invalid JWT token.");
        }
    }
}

function getJWTComponents(string jwtToken) returns (string[])|error {
    string[] jwtComponents = jwtToken.split("\\.");
    if (jwtComponents.length() < 2 || jwtComponents.length() > 3) {
        return prepareError("Invalid JWT token.");
    }
    return jwtComponents;
}

function parseJWT(string[] encodedJWTComponents) returns ((JwtHeader, JwtPayload)|error) {
    json headerJson = {};
    json payloadJson = {};
    var decodedJWTComponents = getDecodedJWTComponents(encodedJWTComponents);
    if (decodedJWTComponents is (json, json)) {
        (headerJson, payloadJson) = decodedJWTComponents;
    } else {
        return decodedJWTComponents;
    }

    JwtHeader jwtHeader = parseHeader(headerJson);
    JwtPayload jwtPayload = parsePayload(payloadJson);
    return (jwtHeader, jwtPayload);
}

function getDecodedJWTComponents(string[] encodedJWTComponents) returns ((json, json)|error) {
    string jwtHeader = encoding:byteArrayToString(check
        encoding:decodeBase64Url(encodedJWTComponents[0]));
    string jwtPayload = encoding:byteArrayToString(check
        encoding:decodeBase64Url(encodedJWTComponents[1]));
    json jwtHeaderJson = {};
    json jwtPayloadJson = {};

    io:StringReader reader = new(jwtHeader);
    var jsonHeader = reader.readJson();
    if (jsonHeader is json) {
        jwtHeaderJson = jsonHeader;
    } else {
        return jsonHeader;
    }

    reader = new(jwtPayload);
    var jsonPayload = reader.readJson();
    if (jsonPayload is json) {
        jwtPayloadJson = jsonPayload;
    } else {
        return jsonPayload;
    }
    return (jwtHeaderJson, jwtPayloadJson);
}

function parseHeader(json jwtHeaderJson) returns (JwtHeader) {
    JwtHeader jwtHeader = {};
    string[] keys = jwtHeaderJson.getKeys();
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

function parsePayload(json jwtPayloadJson) returns (JwtPayload) {
    string[] aud = [];
    JwtPayload jwtPayload = {};
    map<json> customClaims = {};
    string[] keys = jwtPayloadJson.getKeys();
    foreach var key in keys {
        if (key == ISS) {
            jwtPayload.iss = jwtPayloadJson[key].toString();
        } else if (key == SUB) {
            jwtPayload.sub = jwtPayloadJson[key].toString();
        } else if (key == AUD) {
            jwtPayload.aud = convertToStringArray(jwtPayloadJson[key]);
        } else if (key == JTI) {
            jwtPayload.jti = jwtPayloadJson[key].toString();
        } else if (key == EXP) {
            string exp = jwtPayloadJson[key].toString();
            var value = int.convert(exp);
            if (value is int) {
                jwtPayload.exp = value;
            } else {
                jwtPayload.exp = 0;
            }
        } else if (key == NBF) {
            string nbf = jwtPayloadJson[key].toString();
            var value = int.convert(nbf);
            if (value is int) {
                jwtPayload.nbf = value;
            } else {
                jwtPayload.nbf = 0;
            }
        } else if (key == IAT) {
            string iat = jwtPayloadJson[key].toString();
            var value = int.convert(iat);
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
                            JWTValidatorConfig config) returns (boolean|error) {
    if (!validateMandatoryJwtHeaderFields(jwtHeader)) {
        return prepareError("Mandatory field signing algorithm(alg) is empty in the given JWT.");
    }
    if (config["validateCertificate"] is ()) {
        config.validateCertificate = true;
    }
    if (config.validateCertificate == true && !check validateCertificate(config)) {
        return prepareError("Public key certificate validity period has passed.");
    }
    var trustStore = config["trustStore"];
    if (trustStore is crypto:TrustStore) {
        var signatureValidationResult = validateSignature(encodedJWTComponents, jwtHeader, config);
        if (signatureValidationResult is error) {
            return signatureValidationResult;
        }
    }
    var iss = config["issuer"];
    if (iss is string) {
        var issuerStatus = validateIssuer(jwtPayload, config);
        if (issuerStatus is error) {
            return issuerStatus;
        }
    }
    var aud = config["audience"];
    if (aud is string[]) {
        var audienceStatus = validateAudience(jwtPayload, config);
        if (audienceStatus is error) {
            return audienceStatus;
        }
    }
    var exp = jwtPayload["exp"];
    if (exp is int) {
        if (!validateExpirationTime(jwtPayload, config)) {
            return prepareError("JWT token is expired.");
        }
    }
    var nbf = jwtPayload["nbf"];
    if (nbf is int) {
        if (!validateNotBeforeTime(jwtPayload)) {
            return prepareError("JWT token is used before Not_Before_Time.");
        }
    }
    //TODO : Need to validate jwt id (jti) and custom claims.
    return true;
}

function validateMandatoryJwtHeaderFields(JwtHeader jwtHeader) returns boolean {
    if (jwtHeader.alg == "") {
        return false;
    }
    return true;
}

function validateCertificate(JWTValidatorConfig config) returns boolean|error {
    crypto:PublicKey publicKey = check crypto:decodePublicKey(keyStore = config.trustStore,
                                                              keyAlias = config.certificateAlias);
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
}

function validateSignature(string[] encodedJWTComponents, JwtHeader jwtHeader, JWTValidatorConfig config)
returns boolean|error {
    if (jwtHeader.alg == NONE) {
        return prepareError("Not a valid JWS. Signature algorithm is NONE.");
    } else {
        if (encodedJWTComponents.length() == 2) {
            return prepareError("Not a valid JWS. Signature is required.");
        } else {
            string assertion = encodedJWTComponents[0] + "." + encodedJWTComponents[1];
            byte[] signPart = check encoding:decodeBase64Url(encodedJWTComponents[2]);
            crypto:PublicKey publicKey = check crypto:decodePublicKey(keyStore = config.trustStore,
                                                                      keyAlias = config.certificateAlias);
            if (jwtHeader.alg == RS256) {
                return crypto:verifyRsaSha256Signature(assertion.toByteArray("UTF-8"), signPart, publicKey);
            } else if (jwtHeader.alg == RS384) {
                return crypto:verifyRsaSha384Signature(assertion.toByteArray("UTF-8"), signPart, publicKey);
            } else if (jwtHeader.alg == RS512) {
                return crypto:verifyRsaSha512Signature(assertion.toByteArray("UTF-8"), signPart, publicKey);
            } else {
                return prepareError("Unsupported JWS algorithm.");
            }
        }
    }
}

function validateIssuer(JwtPayload jwtPayload, JWTValidatorConfig config) returns error? {
    var iss = jwtPayload["iss"];
    if (iss is string) {
        if (jwtPayload.iss != config.issuer) {
            return prepareError("JWT contained invalid issuer name : " + jwtPayload.iss);
        }
    } else {
        return prepareError("JWT must contain a valid issuer name.");
    }
}

function validateAudience(JwtPayload jwtPayload, JWTValidatorConfig config) returns error? {
    var aud = jwtPayload["aud"];
    if (aud is string[]) {
        boolean validationStatus = false;
        foreach var audiencePayload in jwtPayload.aud {
            foreach var audienceConfig in config.audience {
                if (audiencePayload == audienceConfig) {
                    validationStatus = true;
                    break;
                }
            }
            if (validationStatus) {
                break;
            }
        }
        if (!validationStatus) {
            return prepareError("Invalid audience.");
        }
    } else {
        return prepareError("JWT must contain a valid audience.");
    }
}

function validateExpirationTime(JwtPayload jwtPayload, JWTValidatorConfig config) returns boolean {
    //Convert current time which is in milliseconds to seconds.
    int expTime = jwtPayload.exp;
    if (config.clockSkew > 0){
        expTime = expTime + config.clockSkew;
    }
    return expTime > time:currentTime().time / 1000;
}

function validateNotBeforeTime(JwtPayload jwtPayload) returns boolean {
    return time:currentTime().time > (jwtPayload["nbf"] ?: 0);
}

function convertToStringArray(json jsonData) returns (string[]) {
    string[] outData = [];
    if (jsonData.length() > 0) {
        int i = 0;
        while (i < jsonData.length()) {
            outData[i] = jsonData[i].toString();
            i = i + 1;
        }
    } else {
        outData[0] = jsonData.toString();
    }
    return outData;
}

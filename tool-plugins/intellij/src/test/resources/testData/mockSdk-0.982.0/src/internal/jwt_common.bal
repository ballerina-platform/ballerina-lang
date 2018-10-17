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

//JOSH header parameters
@final string ALG = "alg";
@final string TYP = "typ";
@final string CTY = "cty";
@final string KID = "kid";

//Payload parameters
@final string ISS = "iss";
@final string SUB = "sub";
@final string AUD = "aud";
@final string JTI = "jti";
@final string EXP = "exp";
@final string NBF = "nbf";
@final string IAT = "iat";

# Represents a JWT header.
# + alg - Signing algorithm
# + typ - Media type of the JWT
# + cty - Content type, convey structural information about the JWT
# + kid - Key ID, hint indicating which key was used to secure the JWS
# + customClaims - Map of custom claims
public type JwtHeader record {
    string alg;
    string typ;
    string cty;
    string kid;
    map customClaims;
    !...
};

# Represents a JWT payload.
# + iss - Issuer, identifies the principal that issued the JWT
# + sub - Subject, identifies the principal that is the subject of the JWT
# + aud - Audience, identifies the recipients that the JWT is intended for
# + jti - JWT ID, unique identifier for the JWT
# + exp - Expiration time,  identifies the expiration time on or after which the JWT must not be accepted
# + nbf - Not before, identifies the time before which the JWT must not be accepted
# + iat - Issued at, identifies the time at which the JWT was issued
# + customClaims - Map of custom claims
public type JwtPayload record {
    string iss;
    string sub;
    string[] aud;
    string jti;
    int exp;
    int nbf;
    int iat;
    map customClaims;
    !...
};

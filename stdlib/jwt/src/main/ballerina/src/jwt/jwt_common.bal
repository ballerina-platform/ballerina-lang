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

# The key algorithms supported by crypto module.
public type JwtSigningAlgorithm RS256|RS384|RS512|NONE;

# The `RSA-SHA256` algorithm
public const RS256 = "RS256";

# The `RSA-SHA384` algorithm
public const RS384 = "RS384";

# The `RSA-SHA512` algorithm
public const RS512 = "RS512";

# Unsecured JWTs (no signing)
public const NONE = "NONE";

//JOSH header parameters
const string ALG = "alg";
const string TYP = "typ";
const string CTY = "cty";
const string KID = "kid";

//Payload parameters
const string ISS = "iss";
const string SUB = "sub";
const string AUD = "aud";
const string JTI = "jti";
const string EXP = "exp";
const string NBF = "nbf";
const string IAT = "iat";

# Represents JWT header.
#
# + alg - Signing algorithm
# + typ - Media type of the JWT
# + cty - Content type, convey structural information about the JWT
# + kid - Key ID, hint indicating which key was used to secure the JWS
public type JwtHeader record {|
    JwtSigningAlgorithm alg?;
    string typ?;
    string cty?;
    string kid?;
|};

# Represents JWT payload.
#
# + iss - Issuer, identifies the principal that issued the JWT
# + sub - Subject, identifies the principal that is the subject of the JWT
# + aud - Audience, identifies the recipients that the JWT is intended for
# + jti - JWT ID, unique identifier for the JWT
# + exp - Expiration time, identifies the expiration time (seconds since the Epoch) on or after which the JWT must not be accepted
# + nbf - Not before, identifies the time (seconds since the Epoch) before which the JWT must not be accepted
# + iat - Issued at, identifies the time (seconds since the Epoch) at which the JWT was issued
# + customClaims - Map of custom claims
public type JwtPayload record {|
    string iss?;
    string sub?;
    string|string[] aud?;
    string jti?;
    int exp?;
    int nbf?;
    int iat?;
    map<json> customClaims?;
|};

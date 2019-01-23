// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Record for providing key store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type KeyStore record {
    string path = "";
    string password = "";
    !...;
};

# Record for providing trust store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type TrustStore record {
    string path = "";
    string password = "";
    !...;
};

# Private key used in cryptographic operations.
#
# + algorithm - Key algorithm
public type PrivateKey record {
    string algorithm;
    !...;
};

# Public key used in cryptographic operations.
#
# + algorithm - Key algorithm
public type PublicKey record {
    string algorithm;
    !...;
};

# Error relevant to crypto operations.
#
# + message - Error message
public type CryptoError record {
    string message = "";
    !...;
};

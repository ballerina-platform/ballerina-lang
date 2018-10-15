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

# VerifySignature the signature of a given jwt.
#
# + data - Original data which has signed.
# + signature - Signature string.
# + algorithm - Signature algorithm.
# + trustStore - Truststore.
# + return - Verified status. true or false.
extern function verifySignature(string data, string signature, string algorithm, TrustStore trustStore)
    returns (boolean);
type TrustStore record {
    string certificateAlias;
    string trustStoreFilePath;
    string trustStorePassword;
    !...
};

# Sign the given input jwt data.
#
# + data - Original that need to sign.
# + algorithm - Signature string.
# + keyStore - Keystore.
# + return - Signature. Signed string.
extern function sign(string data, string algorithm, KeyStore keyStore) returns (string);

type KeyStore record {
    string keyAlias;
    string keyPassword;
    string keyStoreFilePath;
    string keyStorePassword;
    !...
};

# Parse JSON string to generate JSON object.
#
# + s - JSON string
# + return - JSON object.
public extern function parseJson(string s) returns (json|error);

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

package ballerina.internal;

@Description {value:"VerifySignature the signature of a given jwt."}
@Param {value:"data: Original data which has signed."}
@Param {value:"signature: Signature string."}
@Param {value:"algorithm: Signature algorithm."}
@Param {value:"keyAlias: Public key alias."}
@Return {value:"Verified status. true or false."}
native function verifySignature (string data, string signature, string algorithm, TrustStore trustStore) returns
                                                                                                       (boolean);
type TrustStore {
    string certificateAlias,
    string trustStoreFilePath,
    string trustStorePassword,
};

@Description {value:"Sign the given input jwt data."}
@Param {value:"data: Original that need to sign."}
@Param {value:"algorithm: Signature string."}
@Param {value:"keyAlias: Private key alias. If this is null use default private key."}
@Param {value:"keyPassword: Private key password."}
@Return {value:"Signature. Signed string."}
native function sign (string data, string algorithm, KeyStore keyStore) returns (string);

type KeyStore {
    string keyAlias,
    string keyPassword,
    string keyStoreFilePath,
    string keyStorePassword,
};

@Description {value:"Parse JSON string to generate JSON object."}
@Param {value:"s: JSON string"}
@Return {value:"JSON object."}
public native function parseJson (string s) returns (json|error);

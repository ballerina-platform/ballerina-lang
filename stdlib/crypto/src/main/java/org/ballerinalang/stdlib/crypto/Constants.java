/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.crypto;

/**
 * Constants related to Ballerina crypto stdlib.
 *
 * @since 0.991.0
 */
public class Constants {

    // Name of the Ballerina crypto module, used to create struct instances.
    public static final String CRYPTO_PACKAGE = "ballerina/crypto";

    // Record used to reference to a key store.
    public static final String KEY_STORE_RECORD = "KeyStore";

    // Record used to reference to a trust store.
    public static final String TRUST_STORE_RECORD = "TrustStore";

    // Record used to reference to a private key.
    public static final String PRIVATE_KEY_RECORD = "PrivateKey";

    // Record used to reference to a public key.
    public static final String PUBLIC_KEY_RECORD = "PublicKey";

    // Native data key for private key within the PrivateKey record.
    public static final String NATIVE_DATA_PRIVATE_KEY = "NATIVE_DATA_PRIVATE_KEY";

    // Native data key for private key within the PublicKey record.
    public static final String NATIVE_DATA_PUBLIC_KEY = "NATIVE_DATA_PUBLIC_KEY";

    // Path field in KEY_STORE_RECORD record.
    public static final String KEY_STORE_RECORD_PATH_FIELD = "path";

    // Password field in KEY_STORE_RECORD record.
    public static final String KEY_STORE_RECORD_PASSWORD_FIELD = "password";

    // Path field in TRUST_STORE_RECORD record.
    public static final String TRUST_STORE_RECORD_PATH_FIELD = "path";

    // Password field in TRUST_STORE_RECORD record.
    public static final String TRUST_STORE_RECORD_PASSWORD_FIELD = "password";

    // Algorithm field in PRIVATE_KEY_RECORD.
    public static final String PRIVATE_KEY_RECORD_ALGORITHM_FIELD = "algorithm";

    // Algorithm field in PUBLIC_KEY_RECORD.
    public static final String PUBLIC_KEY_RECORD_ALGORITHM_FIELD = "algorithm";

    // Error record for crypto module.
    public static final String CRYPTO_ERROR = "CryptoError";

    // Message field within error record.
    public static final String MESSAGE = "message";

    // Error code for crypto error
    public static final String ENCODING_ERROR_CODE = "{ballerina/encoding}CryptoError";

    // PKCS12 keystore type
    public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
}

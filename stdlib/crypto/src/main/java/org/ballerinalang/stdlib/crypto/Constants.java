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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.api.BString;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Constants related to Ballerina crypto stdlib.
 *
 * @since 0.990.3
 */
public class Constants {

    // Name of the Ballerina crypto module, used to create struct instances.
    public static final String CRYPTO_PACKAGE = "ballerina/crypto";

    public static final BPackage CRYPTO_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "crypto", "1.0.0");

    // Record used to reference to a private key.
    public static final String PRIVATE_KEY_RECORD = "PrivateKey";

    // Record used to reference to a public key.
    public static final String PUBLIC_KEY_RECORD = "PublicKey";

    // Record used to reference to a public key certificate.
    public static final String CERTIFICATE_RECORD = "Certificate";

    // Native data key for private key within the PrivateKey record.
    public static final String NATIVE_DATA_PRIVATE_KEY = "NATIVE_DATA_PRIVATE_KEY";

    // Native data key for private key within the PublicKey record.
    public static final String NATIVE_DATA_PUBLIC_KEY = "NATIVE_DATA_PUBLIC_KEY";

    // Native data key for private key within the PublicKey record.
    public static final String NATIVE_DATA_PUBLIC_KEY_CERTIFICATE = "NATIVE_DATA_PUBLIC_KEY_CERTIFICATE";

    // Path field in KEY_STORE_RECORD record.
    public static final BString KEY_STORE_RECORD_PATH_FIELD = StringUtils.fromString("path");

    // Password field in KEY_STORE_RECORD record.
    public static final BString KEY_STORE_RECORD_PASSWORD_FIELD = StringUtils.fromString("password");

    // Algorithm field in PRIVATE_KEY_RECORD.
    public static final String PRIVATE_KEY_RECORD_ALGORITHM_FIELD = "algorithm";

    // Algorithm field in PUBLIC_KEY_RECORD.
    public static final String PUBLIC_KEY_RECORD_ALGORITHM_FIELD = "algorithm";

    // Algorithm field in PUBLIC_KEY_RECORD.
    public static final String PUBLIC_KEY_RECORD_CERTIFICATE_FIELD = "certificate";

    // Version field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_VERSION_FIELD = "version0";

    // Serial field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_SERIAL_FIELD = "serial";

    // Issuer field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_ISSUER_FIELD = "issuer";

    // Subject field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_SUBJECT_FIELD = "subject";

    // NotBefore field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_NOT_BEFORE_FIELD = "notBefore";

    // NotAfter field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_NOT_AFTER_FIELD = "notAfter";

    // Signature field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_SIGNATURE_FIELD = "signature";

    // SigningAlgorithm field in CERTIFICATE_RECORD.
    public static final String CERTIFICATE_RECORD_SIGNATURE_ALG_FIELD = "signingAlgorithm";

    // Crypto error type ID
    static final String CRYPTO_ERROR = "CryptoError";

    // Message field within error record.
    public static final String MESSAGE = "message";

    // PKCS12 keystore type
    public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";

    // GMT timezone name used for X509 validity times
    public static final String TIMEZONE_GMT = "GMT";

    // CBC encryption mode
    public static final String CBC = "CBC";

    // ECB encryption mode
    public static final String ECB = "ECB";

    // GCM encryption mode
    public static final String GCM = "GCM";

    // AES encryption algorithm
    public static final String AES = "AES";

    // RSA encryption algorithm
    public static final String RSA = "RSA";
}

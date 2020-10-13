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

package org.ballerinalang.stdlib.crypto.nativeimpl;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Extern functions ballerina signing and verifying algorithms.
 *
 * @since 0.990.3
 */
public class Sign {

    public static Object signRsaMd5(BArray inputValue, BMap<?, ?> privateKey) {
        byte[] input = inputValue.getBytes();
        PrivateKey key = (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY);
        return CryptoUtils.sign("MD5withRSA", key, input);
    }

    public static Object signRsaSha1(BArray inputValue, BMap<?, ?> privateKey) {
        byte[] input = inputValue.getBytes();
        PrivateKey key = (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY);
        return CryptoUtils.sign("SHA1withRSA", key, input);
    }

    public static Object signRsaSha256(BArray inputValue, BMap<?, ?> privateKey) {
        byte[] input = inputValue.getBytes();
        PrivateKey key = (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY);
        return CryptoUtils.sign("SHA256withRSA", key, input);
    }

    public static Object signRsaSha384(BArray inputValue, BMap<?, ?> privateKey) {
        byte[] input = inputValue.getBytes();
        PrivateKey key = (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY);
        return CryptoUtils.sign("SHA384withRSA", key, input);
    }

    public static Object signRsaSha512(BArray inputValue, BMap<?, ?> privateKey) {
        byte[] input = inputValue.getBytes();
        PrivateKey key = (PrivateKey) privateKey.getNativeData(Constants.NATIVE_DATA_PRIVATE_KEY);
        return CryptoUtils.sign("SHA512withRSA", key, input);
    }

    public static Object verifyRsaMd5Signature(BArray dataValue, BArray signatureValue,
                                               BMap<?, ?> publicKey) {
        byte[] data = dataValue.getBytes();
        byte[] signature = signatureValue.getBytes();
        PublicKey key = (PublicKey) publicKey.getNativeData(Constants.NATIVE_DATA_PUBLIC_KEY);
        return CryptoUtils.verify("MD5withRSA", key, data, signature);
    }

    public static Object verifyRsaSha1Signature(BArray dataValue, BArray signatureValue,
                                                BMap<?, ?> publicKey) {
        byte[] data = dataValue.getBytes();
        byte[] signature = signatureValue.getBytes();
        PublicKey key = (PublicKey) publicKey.getNativeData(Constants.NATIVE_DATA_PUBLIC_KEY);
        return CryptoUtils.verify("SHA1withRSA", key, data, signature);
    }

    public static Object verifyRsaSha256Signature(BArray dataValue, BArray signatureValue,
                                                  BMap<?, ?> publicKey) {
        byte[] data = dataValue.getBytes();
        byte[] signature = signatureValue.getBytes();
        PublicKey key = (PublicKey) publicKey.getNativeData(Constants.NATIVE_DATA_PUBLIC_KEY);
        return CryptoUtils.verify("SHA256withRSA", key, data, signature);
    }

    public static Object verifyRsaSha384Signature(BArray dataValue, BArray signatureValue,
                                                  BMap<?, ?> publicKey) {
        byte[] data = dataValue.getBytes();
        byte[] signature = signatureValue.getBytes();
        PublicKey key = (PublicKey) publicKey.getNativeData(Constants.NATIVE_DATA_PUBLIC_KEY);
        return CryptoUtils.verify("SHA384withRSA", key, data, signature);
    }

    public static Object verifyRsaSha512Signature(BArray dataValue, BArray signatureValue,
                                                  BMap<?, ?> publicKey) {
        byte[] data = dataValue.getBytes();
        byte[] signature = signatureValue.getBytes();
        PublicKey key = (PublicKey) publicKey.getNativeData(Constants.NATIVE_DATA_PUBLIC_KEY);
        return CryptoUtils.verify("SHA512withRSA", key, data, signature);
    }
}

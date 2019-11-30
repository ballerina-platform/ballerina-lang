/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.jvm.values.ArrayValue;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Function for generating CRC32 hashes.
 *
 * @since 0.970.0-alpha1
 */
public class Crc32b {

    public static String crc32b(ArrayValue input) {
        Checksum checksum = new CRC32();
        byte[] bytes = input.getBytes();
        long checksumVal;

        checksum.update(bytes, 0, bytes.length);
        checksumVal = checksum.getValue();
        return Long.toHexString(checksumVal);
    }
}

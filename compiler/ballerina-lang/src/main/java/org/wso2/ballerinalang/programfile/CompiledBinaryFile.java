/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.programfile;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BIR_VERSION_NUMBER;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.VERSION_NUMBER;

/**
 * {@code CompiledBinaryFile} is the super class of {@link BIRPackageFile} and {@link PackageFile}.
 *
 * @since 0.963.0
 */
@Deprecated
public class CompiledBinaryFile {

    /**
     * {@code PackageFile} is the representation of a compiled Ballerina package (BALO).
     *
     * @since 0.963.0
     */
    public static class PackageFile extends CompiledBinaryFile {

        public static final int MAGIC_VALUE = 0xFFFFFFFF;
        public static final int LANG_VERSION = VERSION_NUMBER;

        public byte[] pkgBinaryContent;

        public PackageFile(byte[] pkgBinaryContent) {
            this.pkgBinaryContent = pkgBinaryContent;
        }
    }

    /**
     * {@code BirPackageFile} is the representation of a compiled Ballerina package (BIR).
     *
     * @since 0.995.0
     */
    public static class BIRPackageFile extends CompiledBinaryFile {

        public static final byte[] BIR_MAGIC = {(byte) 0xba, (byte) 0x10, (byte) 0xc0, (byte) 0xde};
        public static final int BIR_VERSION = BIR_VERSION_NUMBER;

        public byte[] pkgBirBinaryContent;

        public BIRPackageFile(byte[] pkgBirBinaryContent) {
            this.pkgBirBinaryContent = pkgBirBinaryContent;
        }
    }
}

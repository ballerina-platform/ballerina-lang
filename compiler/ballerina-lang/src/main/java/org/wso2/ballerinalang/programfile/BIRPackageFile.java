/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import io.ballerina.projects.ModuleContext;

import java.util.Arrays;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BIR_VERSION_NUMBER;

/**
 * {@code BirPackageFile} is the representation of a compiled Ballerina package (BIR).
 *
 * @since 0.995.0
 */
public abstract class BIRPackageFile extends CompiledBinaryFile {

    public static final byte[] BIR_MAGIC = {(byte) 0xba, (byte) 0x10, (byte) 0xc0, (byte) 0xde};
    public static final int BIR_VERSION = BIR_VERSION_NUMBER;

    public abstract byte[] getPkgBirBinaryContent();

    public abstract void setPkgBirBinaryContent(byte[] pkgBirBinaryContent);

    public static class EagerBirPackageFile extends BIRPackageFile {

        private byte[] pkgBirBinaryContent;

        public EagerBirPackageFile(byte[] pkgBirBinaryContent) {
            this.pkgBirBinaryContent = pkgBirBinaryContent;
        }

        @Override
        public byte[] getPkgBirBinaryContent() {
            return pkgBirBinaryContent;
        }

        @Override
        public void setPkgBirBinaryContent(byte[] pkgBirBinaryContent) {
            this.pkgBirBinaryContent = pkgBirBinaryContent;
        }

    }

    public static class LazyBirPackageFile extends BIRPackageFile {

        private final ModuleContext moduleContext;
        private EagerBirPackageFile delegate = null;

        public LazyBirPackageFile(ModuleContext moduleContext) {
            this.moduleContext = moduleContext;
        }

        @Override
        public byte[] getPkgBirBinaryContent() {
            if (delegate != null) {
                return delegate.getPkgBirBinaryContent();
            }
            // Strip magic value (4 bytes) and the version (2 bytes) off from the binary content of the package.
            // Ideally, I would like to return a slice but in java we don't have a way to do that efficiently.
            byte[] bytes = moduleContext.getBirBytes();
            return Arrays.copyOfRange(bytes, 8, bytes.length);
        }

        @Override
        public void setPkgBirBinaryContent(byte[] pkgBirBinaryContent) {
            delegate = new EagerBirPackageFile(pkgBirBinaryContent);
        }
    }
}

/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.util.SourcePackageFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangSourcePackageFile extends BLangPackageFile implements SourcePackageFile {
    public List<BLangSourceFile> sourceFiles;

    public BLangSourcePackageFile(List<BLangSourceFile> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @Override
    public List<BLangSourceFile> getSourceFiles() {
        return sourceFiles;
    }

    /**
     * @since 0.94
     */
    public static class BLangSourceFile implements SourceFile {
        public String filename;
        public InputStream inputStream;

        public BLangSourceFile(String filename, InputStream inputStream) {
            this.filename = filename;
            this.inputStream = inputStream;
        }

        @Override
        public String getName() {
            return filename;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
    }
}

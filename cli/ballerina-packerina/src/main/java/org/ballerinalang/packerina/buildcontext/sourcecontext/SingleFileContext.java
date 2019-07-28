/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.buildcontext.sourcecontext;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dataholder for a single bal file compilation.
 */
public class SingleFileContext {
    /**
     * The absolute path for the bal file.
     */
    private Path balFile;
    private BLangPackage module;
    
    public SingleFileContext(Path balFile) {
        if (Files.exists(balFile) && balFile.isAbsolute()) {
            this.balFile = balFile;
        } else {
            throw new BLangCompilerException("invalid source or does not exists: " + balFile);
        }
    }
    
    public Path getBalFile() {
        return this.balFile;
    }
    
    public BLangPackage getModule() {
        return this.module;
    }
    
    public void setModule(BLangPackage module) {
        this.module = module;
    }
    
    public String getBalFileNameWithoutExtension() {
        Path balFileName = this.balFile.getFileName();
        if (null != balFileName) {
            int index = indexOfExtension(balFileName.toString());
            return index == -1 ? balFileName.toString() :
                   balFileName.toString().substring(0, index);
        } else {
            return null;
        }
    }
    
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }
    
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }
}

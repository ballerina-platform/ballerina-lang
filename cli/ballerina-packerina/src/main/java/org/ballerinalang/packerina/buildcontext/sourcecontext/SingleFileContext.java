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

import static org.ballerinalang.packerina.utils.FileUtils.geFileNameWithoutExtension;

/**
 * Dataholder for a single bal file compilation.
 */
public class SingleFileContext {
    /**
     * The absolute path for the bal file.
     */
    private Path balFile;
    private BLangPackage module;
    private Path executableFilePath = null;
    
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
    
    public Path getExecutableFilePath() {
        return this.executableFilePath;
    }
    
    public void setExecutableFilePath(Path executableFilePath) {
        this.executableFilePath = executableFilePath;
    }
    
    public String getBalFileNameWithoutExtension() {
        return geFileNameWithoutExtension(this.balFile);
    }
}

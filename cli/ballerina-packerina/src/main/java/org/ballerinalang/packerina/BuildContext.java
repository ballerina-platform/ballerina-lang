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

package org.ballerinalang.packerina;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Context to be passed to tasks when they get executed.
 */
public class BuildContext {
    private Path sourceRoot;
    private Path targetDir;
    private Map<String, Object> buildData = new HashMap();
    
    public Path getSourceRoot() {
        return this.sourceRoot;
    }
    
    public void setSourceRoot(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }
    
    public Path getTargetDir() {
        return this.targetDir;
    }
    
    public void setTargetDir(Path targetDir) {
        this.targetDir = targetDir;
    }
    
    public Map<String, Object> getBuildData() {
        return this.buildData;
    }
    
    public void addBuildData(String field, Object value) {
        this.buildData.put(field, value);
    }
}

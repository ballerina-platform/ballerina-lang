/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the LockFile object which is created using the Ballerina.lock file configs.
 *
 * @since 0.973.1
 */
public class LockFile {
    private String org_name = "";
    private String version = "";
    private String lockfile_version = "";
    private String ballerina_version = "";
    private Set<LockFileImport> imports = new HashSet<>();
    
    public String getOrgName() {
        return org_name;
    }
    
    public void setOrgName(String orgName) {
        this.org_name = orgName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getLockfileVersion() {
        return lockfile_version;
    }
    
    public void setLockfileVersion(String lockfileVersion) {
        this.lockfile_version = lockfileVersion;
    }
    
    public String getBallerinaVersion() {
        return ballerina_version;
    }
    
    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerina_version = ballerinaVersion;
    }
    
    public Set<LockFileImport> getImports() {
        return imports;
    }
    
    public void setImports(Set<LockFileImport> imports) {
        this.imports = imports;
    }
}

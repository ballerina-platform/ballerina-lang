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

package org.ballerinalang.toml.model;

import java.util.Objects;

/**
 * An import of a lock file.
 */
public class LockFileImport {
    private String org_name = "";
    private String name = "";
    private String version = "";
    
    public LockFileImport(String orgName, String name, String version) {
        this.org_name = orgName;
        this.name = name;
        this.version = version;
    }
    
    public String getOrgName() {
        return org_name;
    }
    
    public void setOrgName(String orgName) {
        this.org_name = orgName;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "LockFileImport{" + "org_name='" + org_name + '\'' +
               ", name='" + name + '\'' +
               ", version='" + version + '\'' + '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LockFileImport that)) {
            return false;
        }
        return Objects.equals(org_name, that.org_name) &&
               Objects.equals(getName(), that.getName()) &&
               Objects.equals(getVersion(), that.getVersion());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(org_name, getName(), getVersion());
    }
}

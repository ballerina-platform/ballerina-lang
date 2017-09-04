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
package org.ballerinalang.model.elements;

import org.ballerinalang.model.tree.IdentifierNode;

import java.util.List;

/**
 * This represents a specific package and its version.
 * 
 * @since 0.94
 */
public class PackageID {
    
    private List<IdentifierNode> nameComps;
    
    private String stringName;
        
    private IdentifierNode version;

    private String packageName;
    private String packageVersion;

    public PackageID(List<IdentifierNode> nameComps, IdentifierNode version) {
        this.nameComps = nameComps;
        this.version = version;
        this.populateNameCompsAsString();
    }

    public PackageID(String packageName, String packageVersion) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public List<IdentifierNode> getNameComps() {
        return nameComps;
    }
    
    private void populateNameCompsAsString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.nameComps.size();  i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(this.nameComps.get(i).getValue());
        }
        this.stringName = builder.toString();
    }
    
    public String getNameCompsAsString() {
        return stringName;
    }
    
    public IdentifierNode getVersion() {
        return version;
    }

    public void setNameComps(List<IdentifierNode> nameComps) {
        this.nameComps = nameComps;
        this.populateNameCompsAsString();
    }

    public void setVersion(IdentifierNode version) {
        this.version = version;
    }
    
    public int getNameCompCount() {
        return this.nameComps.size();
    }
    
    public IdentifierNode getNameComponent(int index) {
        return this.nameComps.get(index);
    }
    
    @Override
    public String toString() {
        return this.getNameCompsAsString() + 
                (this.getVersion() != null ? "[" + this.getVersion().getValue() + "]" : "");
    }
    
}

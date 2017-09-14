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

import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * This represents a specific package and its version.
 *
 * @since 0.94
 */
public class PackageID {

    public static final PackageID EMPTY = new PackageID(Names.EMPTY, Names.EMPTY);
    
    public static final String PACKAGE_COMP_SEPARATOR = ".";

    public Name name;
    
    public Name version;

    public PackageID(Name name, Name version) {
        this.name = name;
        this.version = version;
    }

    public Name getPackageName() {
        return name;
    }

    public Name getPackageVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageID packageID = (PackageID) o;
        return name.equals(packageID.name) && version.equals(packageID.version);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.name + "[" + this.version + "]";
    }
    
}

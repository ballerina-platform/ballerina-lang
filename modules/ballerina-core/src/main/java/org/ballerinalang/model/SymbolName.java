/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model;

/**
 * {@code SymbolName} represents a package qualified
 * name of a {@link org.ballerinalang.model.symbols.BLangSymbol} in Ballerina.
 *
 * @since 0.8.0
 */
public class SymbolName {
    protected String name;
    protected String pkgPath;

    public SymbolName(String name, String pkgPath) {
        this.name = name;
        this.pkgPath = pkgPath;
    }

    public SymbolName(String name) {
        this(name, null);
    }

    /**
     * Returns the name of this {@code SymbolName}.
     *
     * @return name of the {@code SymbolName}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the package path of this {@code SymbolName}.
     *
     * @return package path of the {@code SymbolName}
     */
    public String getPkgPath() {
        return pkgPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Names cannot be null here.
        SymbolName that = (SymbolName) o;
        if (!name.equals(that.name)) {
            return false;
        }

        // If both package paths are null then return true.
        // If both package paths are not null, then they should be equal.
        return pkgPath != null ? pkgPath.equals(that.pkgPath) : that.pkgPath == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (pkgPath != null ? pkgPath.hashCode() : 0);
        return result;
    }

    public String toString() {
        return (pkgPath == null || pkgPath.equals(".")) ? name : pkgPath + ":" + name;
    }
}

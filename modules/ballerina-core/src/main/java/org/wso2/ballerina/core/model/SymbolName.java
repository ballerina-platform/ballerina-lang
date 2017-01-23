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
package org.wso2.ballerina.core.model;

/**
 * {@code SymbolName} represents an identifier in Ballerina.
 *
 * @since 0.8.0
 */
public class SymbolName {

    private String name;
    private String pkgName;
    private String connectorName;

    public SymbolName(String name) {
        this.name = name;
    }

    public SymbolName(String name, String pkgName) {
        this.name = name;
        this.pkgName = pkgName;
    }

    /**
     * Get the name of the Identifier.
     *
     * @return name of the Identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the package name of this symbol name.
     *
     * @return package name of this symbol name
     */
    public String getPkgName() {
        return pkgName;
    }

    /**
     * Set the package name of this symbol name.
     *
     * @param pkgName package name of this symbol name
     */
    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    @Override
    public boolean equals(Object obj) {
        SymbolName other = (SymbolName) obj;
        return this.name.equals(other.getName());
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result;
        return result;
    }

    public String toString() {
        return (pkgName == null) ? name : pkgName + ":" + name;
    }
}

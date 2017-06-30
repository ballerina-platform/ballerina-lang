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
package org.ballerinalang.model.types;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.WhiteSpaceDescriptor;

/**
 * {@code SimpleTypeName} represents a simple type name(int, boolean, json, Person..) in Ballerina.
 *
 * @since 0.8.0
 */
public class SimpleTypeName {
    protected WhiteSpaceDescriptor whiteSpaceDescriptor;
    protected NodeLocation location;
    protected String name;
    protected String pkgName;
    protected String pkgPath;
    protected SymbolName symbolName;
    protected boolean isArrayType;
    protected int dimensions = 1;

    public SimpleTypeName(String name, String pkgName, String pkgPath) {
        this.name = name;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
    }

    public SimpleTypeName(String name) {
        this(name, null, null);
    }
    
    public SimpleTypeName(String name, boolean isArrayType, int dimensions) {
        this(name, null, null);
        this.isArrayType = isArrayType;
        this.dimensions = dimensions;
    }
    
    public SimpleTypeName(String name, String pkgPath, boolean isArrayType, int dimensions) {
        this(name, null, null);
        this.isArrayType = isArrayType;
        this.pkgPath = pkgPath;
        this.dimensions = dimensions;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return pkgName;
    }

    public String getPackagePath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public SymbolName getSymbolName() {
        if (symbolName == null) {
            this.symbolName = new SymbolName(getNameWithArray(name), pkgPath);
        }

        return symbolName;
    }

    public boolean isArrayType() {
        return isArrayType;
    }

    public String getNameWithPkg() {
        return (pkgName == null || pkgName.equals("")) ? name : pkgName + ":" + name;
    }

    protected String getNameWithArray(String name) {
        if (isArrayType) {
            String arrayName = name;
            for (int i = 0; i < getDimensions(); i++) {
                arrayName = arrayName + TypeConstants.ARRAY_TNAME;
            }
            return arrayName;
        } else {
            return name;
        }
    }

    @Override
    public String toString() {
        return getNameWithArray(getNameWithPkg());
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setArrayType(int dimensions) {
        this.isArrayType = true;
        this.dimensions =  dimensions;
    }

    public void setNodeLocation(NodeLocation location) {
        this.location = location;
    }

    public NodeLocation getNodeLocation() {
        return location;
    }

    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }
}

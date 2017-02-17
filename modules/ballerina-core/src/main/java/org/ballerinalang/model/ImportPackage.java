/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model;

/**
 * {@code Import} represents an import declaration.
 * <p>
 * e.g
 * import ballerina.model.system
 * import ballerina.model.xml
 * import ballerina.net.http
 * <p>
 * A ballerina package has both a name and a path. The last element of the package path is the package name
 * <p>
 * e.g.
 * Package path: ballerina.model.system
 * Package name: system
 *
 * @since 0.8.0
 */
public class ImportPackage implements Node {
    protected NodeLocation location;
    private String path;
    private String name;
    private String asName;
    private SymbolName symbolName;

    private boolean isUsed;

    public ImportPackage(String path) {
        int lastIndex = path.lastIndexOf(".");
        if (lastIndex != -1) {
            this.name = path.substring(lastIndex + 1);
        } else {
            this.name = path;
        }

        this.path = path;
        this.symbolName = new SymbolName(this.path);
    }

    public ImportPackage(NodeLocation location, String path) {
        this(path);
        this.location = location;
    }

    public ImportPackage(NodeLocation location, String path, String asName) {
        this.location = location;
        this.path = path;
        this.name = asName;
        this.asName = asName;

        this.symbolName = new SymbolName(this.path);
    }

    /**
     * Get the name of the import  package.
     *
     * @return name of the import package
     */
    public String getName() {
        return name;
    }

    public String getAsName() {
        return asName;
    }

    /**
     * Get the path of the import package.
     *
     * @return name of the import package
     */
    public String getPath() {
        return path;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void markUsed() {
        isUsed = true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ImportPackage)) {
            return false;
        }

        ImportPackage other = (ImportPackage) obj;
        return this.path.equals(other.path);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }
}

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

package org.wso2.ballerina.core.model;

/**
 * {@code Import} represents an import declaration.
 * <p>
 * e.g
 * import ballerina.lang.system
 * import ballerina.lang.xml
 * import ballerina.net.http
 * <p>
 * A ballerina package has both a name and a path. The last element of the package path is the package name
 * <p>
 * e.g.
 * Package path: ballerina.lang.system
 * Package name: system
 *
 * @since 0.8.0
 */
public class ImportPackage implements Node {

    private String path;
    private String name;
    protected Position sourceLocation;

    public ImportPackage(String path) {
        int lastIndex = path.lastIndexOf(".");
        if (lastIndex != -1) {
            this.name = path.substring(lastIndex + 1);
        } else {
            this.name = path;
        }

        this.path = path;
    }
    
    public ImportPackage(String path, Position location) {
        this(path);
        this.sourceLocation = location;
    }

    public ImportPackage(String path, String name) {
        this.path = path;
        this.name = name;
    }
    
    public ImportPackage(String path, String name, Position location) {
        this.path = path;
        this.name = name;
        this.sourceLocation = location;
    }

    /**
     * Get the name of the import  package
     *
     * @return name of the import package
     */
    public String getName() {
        return name;
    }

    /**
     * Get the path of the import package
     *
     * @return name of the import package
     */
    public String getPath() {
        return path;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ImportPackage)) {
            return false;
        }

        ImportPackage other = (ImportPackage) obj;
        return this.name.equals(other.name) && this.path.equals(other.path);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Get the source location of this import package statement.
     * Return the source file and the line number of this import package statement.
     * 
     * @return  Source location of this import package statement
     */
    public Position getLocation() {
        return sourceLocation;
    }

    /**
     * Set the source location of this import package statement.
     * 
     * @param location  Source location of this import package statement.
     */
    public void setLocation(Position location) {
        this.sourceLocation = location;
    }
}

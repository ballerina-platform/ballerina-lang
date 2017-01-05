/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model;

/**
 * Bean class to hold the position in the file, of a ballerina expression.
 */
public class Position {
    private String fileName;
    private int line = -1;

    public Position(String fileName) {
        this.fileName = fileName;
    }

    public Position(String fileName, int line) {
        this.fileName = fileName;
        this.line = line;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getLine() {
        return this.line;
    }

    public boolean equals(Object obj) {
        Position other = (Position) obj;
        return this.fileName.equals(other.getFileName());
    }

    public int hashCode() {
        int result = this.fileName.hashCode();
        result = 31 * result;
        return result;
    }
}

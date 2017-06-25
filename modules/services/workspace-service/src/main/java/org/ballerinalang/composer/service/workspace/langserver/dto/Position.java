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

package org.ballerinalang.composer.service.workspace.langserver.dto;

/**
 * DTO to represent the position inside a document
 */
public class Position {
    private int line;

    private int character;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    @Override
    public int hashCode() {
        return ("L:" + line + ",COL:" + character).hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position other = (Position) obj;
            return (other.getLine() == this.getLine()) && (other.getCharacter() == this.getCharacter());
        } else {
            return false;
        }
    }
}

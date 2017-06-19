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

package org.ballerinalang.composer.service.workspace.rest.datamodel;

/**
 * Information about a cursor position in source
 */
public class SourcePosition {

    private int lineNumber;

    private int offset;

    public SourcePosition(int lineNumber, int offset) {
        this.lineNumber = lineNumber;
        this.offset = offset;
    }

    public SourcePosition() {
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SourcePosition) {
            SourcePosition other = (SourcePosition) obj;
            return (other.lineNumber == this.lineNumber) && (other.offset == this.offset);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("L:" + lineNumber + ",COL:" + offset).hashCode();
    }
}

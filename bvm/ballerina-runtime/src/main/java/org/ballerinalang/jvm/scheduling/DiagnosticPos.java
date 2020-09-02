/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.scheduling;

/**
 * This represents position data.
 *
 * @since 2.0.0
 */
public class DiagnosticPos {

    /**
     * Source file name.
     */
    private String src;

    /**
     * Start line number.
     */
    private int sLine;

    /**
     * End line number.
     */
    private int eLine;

    /**
     * Start column number.
     */
    private int sCol;

    /**
     * End column number.
     */
    private int eCol;

    public DiagnosticPos(String sourceFileName,
                         int startLine,
                         int endLine,
                         int startCol,
                         int endCol) {
        this.src = sourceFileName;
        this.sLine = startLine;
        this.eLine = endLine;
        this.sCol = startCol;
        this.eCol = endCol;
    }

    /**
     * Gets the source file name.
     *
     * @return Source file name.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Gets the start line number.
     *
     * @return Start line number.
     */
    public int getStartLine() {
        return sLine;
    }

    /**
     * Gets the end line number.
     *
     * @return End line number.
     */
    public int getEndLine() {
        return eLine;
    }

    /**
     * Gets the start column number.
     *
     * @return Start column number.
     */
    public int getStartColumn() {
        return sCol;
    }

    /**
     * Gets the end column number.
     *
     * @return End column number.
     */
    public int getEndColumn() {
        return eCol;
    }
}

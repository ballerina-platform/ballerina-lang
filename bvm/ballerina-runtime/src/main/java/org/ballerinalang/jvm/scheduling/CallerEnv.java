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
 * Holds function caller environment data.
 *
 * @since 2.0.0
 */
public class CallerEnv {

    /**
     * Organization name of module method call was initiated.
     */
    private final String orgName;

    /**
     * Name of module method call was initiated.
     */
    private final String name;

    /**
     * Version of module method call was initiated.
     */
    private final String version;

    /**
     * Position of module method call was initiated.
     */
    private final DiagnosticPos diagnosticPos;

    public CallerEnv(String orgName, String name, String version, String sourceFileName, int startLine, int endLine,
                     int startCol, int endCol) {
        this.orgName = orgName;
        this.name = name;
        this.version = version;
        this.diagnosticPos = new DiagnosticPos(sourceFileName, startLine, endLine, startCol, endCol);
    }

    public String getOrgName() {
        return orgName;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    /**
     * This contains the position info of calling instruction.
     *
     * @since 2.0.0
     */
    public static class DiagnosticPos {

        public String src;
        public int sLine;
        public int eLine;
        public int sCol;
        public int eCol;

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
    }
}

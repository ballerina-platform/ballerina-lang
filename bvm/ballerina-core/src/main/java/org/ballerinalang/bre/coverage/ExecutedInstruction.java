/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.coverage;

import org.ballerinalang.util.codegen.LineNumberInfo;

/**
 * This is the data holder for executed instruction which is used for coverage data.
 *
 * @since 0.985
 */
public class ExecutedInstruction {

    private int ip;

    private String pkgPath;

    private String fileName;

    private String functionName;

    private LineNumberInfo lineNumberInfo;

    public ExecutedInstruction(int ip, String pkgPath, String fileName, String functionName,
                               LineNumberInfo lineNumberInfo) {

        this.ip = ip;
        this.pkgPath = pkgPath;
        this.fileName = fileName;
        this.functionName = functionName;
        this.lineNumberInfo = lineNumberInfo;
    }

    /**
     * Getter for the executed Ip.
     *
     * @return the executed Ip
     */
    public int getIp() {

        return ip;
    }

    /**
     * Setter for the executed Ip.
     *
     * @param ip the executed Ip
     */
    public void setIp(int ip) {

        this.ip = ip;
    }

    /**
     * Getter for the executed Ip's full package path.
     *
     * @return the executed Ip's full package path
     */
    public String getPkgPath() {

        return pkgPath;
    }

    /**
     * Setter for the executed Ip's full package path.
     *
     * @param pkgPath the executed Ip's full package path
     */
    public void setPkgPath(String pkgPath) {

        this.pkgPath = pkgPath;
    }

    /**
     * Getter for the executed Ip's file name.
     *
     * @return the executed Ip's file name
     */
    public String getFileName() {

        return fileName;
    }

    /**
     * Setter for the executed Ip's file name.
     *
     * @param fileName the executed Ip's file name
     */
    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    /**
     * Getter for the executed Ip's function name.
     *
     * @return the executed Ip's function name
     */
    public String getFunctionName() {

        return functionName;
    }

    /**
     * Setter for the executed Ip's function name.
     *
     * @param functionName the executed Ip's function name
     */
    public void setFunctionName(String functionName) {

        this.functionName = functionName;
    }

    /**
     * Getter for the executed Ip's line number info object.
     *
     * @return the executed Ip's line number info object
     */
    public LineNumberInfo getLineNumberInfo() {

        return lineNumberInfo;
    }

    /**
     * Setter for the executed Ip's line number info object.
     *
     * @param lineNumberInfo the executed Ip's line number info object
     */
    public void setLineNumberInfo(LineNumberInfo lineNumberInfo) {

        this.lineNumberInfo = lineNumberInfo;
    }
}

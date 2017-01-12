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
package org.wso2.ballerina.core.model;

/**
 * {@code CallableUnit} represents Functions, Action or Resources
 *
 * @see Function
 * @see Action
 * @see Resource
 * @since 1.0.0
 */
public interface CallableUnit {

    String getName();

    SymbolName getSymbolName();

    void setSymbolName(SymbolName symbolName);

    String getPackageName();

    Annotation[] getAnnotations();

    Parameter[] getParameters();

    VariableDcl[] getVariableDcls();

    Parameter[] getReturnParameters();

    int getStackFrameSize();

    void setStackFrameSize(int frameSize);

    /**
     * Get the location of this function in the ballerina source file.
     * Returns the ballerina file and line number of the function.
     *
     * @return location of this function in the ballerina source file
     */
    Position getLocation();
}

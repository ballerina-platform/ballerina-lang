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

import org.wso2.ballerina.core.model.statements.BlockStmt;

/**
 * {@code CallableUnit} represents Functions, Action or Resources
 *
 * @see Function
 * @see Action
 * @see Resource
 * @since 1.0.0
 */
public interface CallableUnit {

    /**
     * Returns the name of the callable unit
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the symbol name of the callable unit
     *
     * @return the symbol name
     */
    SymbolName getSymbolName();

    /**
     * Replaces the symbol name of this callable unit with the specified symbol name.
     *
     * @param symbolName name of the symbol.
     */
    void setSymbolName(SymbolName symbolName);

    /**
     * Returns the package name of this callable unit
     *
     * @return the package name
     */
    String getPackageName();

    /**
     * Returns an array of annotations attached this callable unit
     *
     * @return an array of annotations
     */
    Annotation[] getAnnotations();

    /**
     * Returns an array of parameters of this callable unit
     *
     * @return an array of parameters
     */
    Parameter[] getParameters();

    /**
     * Returns an array of variable declarations of this callable unit
     *
     * @return an array of variable declarations
     */
    VariableDcl[] getVariableDcls();

    /**
     * Returns an array of return parameters (values) of this callable unit
     *
     * @return an array of return parameters
     */
    Parameter[] getReturnParameters();

    /**
     * Returns size of the stack frame which should be allocated for each invocations
     *
     * @return size of the stack frame
     */
    int getStackFrameSize();

    /**
     * Replaces the size of the current stack frame with the specified size
     *
     * @param frameSize size of the stack frame
     */
    void setStackFrameSize(int frameSize);

    /**
     * Returns the body of the callable unit as a {@code BlockStmt}
     *
     * @return body of the callable unit
     */
    BlockStmt getCallableUnitBody();

    /**
     * Get the location of this function in the ballerina source file.
     * Returns the ballerina file and line number of the function.
     *
     * @return location of this function in the ballerina source file
     */
    Position getLocation();
}

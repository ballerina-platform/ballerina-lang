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
package org.ballerinalang.model;

import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;

/**
 * {@code CallableUnit} represents Functions, Action or Resources.
 *
 * @see Function
 * @see Action
 * @see Resource
 * @since 0.8.0
 */
public interface CallableUnit extends BLangSymbol, Node {

    /**
     * Replaces the symbol name of this callable unit with the specified symbol name.
     *
     * @param symbolName name of the symbol.
     */
    void setSymbolName(SymbolName symbolName);

    /**
     * Returns an arrays of annotations attached this callable unit.
     *
     * @return an arrays of annotations
     */
    Annotation[] getAnnotations();

    /**
     * Returns an arrays of parameters of this callable unit.
     *
     * @return an arrays of parameters
     */
    ParameterDef[] getParameterDefs();

    /**
     * Returns an arrays of variable declarations of this callable unit.
     *
     * @return an arrays of variable declarations
     */
    VariableDef[] getVariableDefs();

    /**
     * Returns an arrays of return parameters (values) of this callable unit.
     *
     * @return an arrays of return parameters
     */
    ParameterDef[] getReturnParameters();

    /**
     * Returns size of the stack frame which should be allocated for each invocations.
     *
     * @return size of the stack frame
     */
    int getStackFrameSize();

    /**
     * Replaces the size of the current stack frame with the specified size.
     *
     * @param frameSize size of the stack frame
     */
    void setStackFrameSize(int frameSize);

    /**
     * Return size of the current stack's temporary value frame.
     *
     * @return size of the temporary value frame
     */
    int getTempStackFrameSize();

    /**
     * Replace size of the current stack's temporary value frame.
     *
     * @param frameSize size of the temporary value frame
     */
    void setTempStackFrameSize(int frameSize);

    /**
     * Returns the body of the callable unit as a {@code BlockStmt}.
     *
     * @return body of the callable unit
     */
    BlockStmt getCallableUnitBody();

    /**
     * Get Types of the return parameters.
     *
     * @return Types of the return parameters
     */
    BType[] getReturnParamTypes();

    /**
     * Sets a {@code BType} arrays containing the types of return parameters of this callable unit.
     *
     * @param returnParamTypes arrays of the return parameters
     */
    void setReturnParamTypes(BType[] returnParamTypes);

    /**
     * Get Types of the return input arguments.
     *
     * @return Types of the return input arguments
     */
    BType[] getArgumentTypes();

    /**
     * Sets a {@code BType} arrays containing the types of input parameters of this callable unit.
     *
     * @param parameterTypes arrays of the input parameters
     */
    void setParameterTypes(BType[] parameterTypes);
}

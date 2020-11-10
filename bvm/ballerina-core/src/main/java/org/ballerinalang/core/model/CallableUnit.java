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
package org.ballerinalang.core.model;

import org.ballerinalang.core.model.symbols.BLangSymbol;
import org.ballerinalang.core.model.types.BType;

/**
 * {@code CallableUnit} represents Functions, Action or Resources.
 *
 * @see Function
 * @see Action
 * @since 0.8.0
 */
public interface CallableUnit extends BLangSymbol, Node {

    /**
     * Returns an arrays of annotations attached this callable unit.
     *
     * @return an arrays of annotations
     */
    AnnotationAttachment[] getAnnotations();

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

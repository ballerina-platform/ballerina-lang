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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.SymbolName;

/**
 * {@code ReferenceExpr} represents a variable reference in Ballerina
 *
 * @since 1.0.0
 */
public interface ReferenceExpr extends Expression {

    /**
     * Return the name of the variable reference
     *
     * @return  variable name
     */
    String getVarName();

    /**
     * Get the symbol name of the reference expression.
     * 
     * @return  Symbolic name
     */
    SymbolName getSymbolName();
}

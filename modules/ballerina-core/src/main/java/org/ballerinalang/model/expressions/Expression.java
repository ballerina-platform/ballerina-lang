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

import org.ballerinalang.model.ExecutableExpr;
import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.types.BType;

/**
 * {@code Expression} represents a generic expression node in Ballerina.
 *
 * @see AddExpression
 * @see VariableRefExpr
 * @see FunctionInvocationExpr
 * @since 0.8.0
 */
public interface Expression extends LinkedNode, ExecutableExpr {

    BType getType();

    void setType(BType type);

    void setOffset(int offset);

    boolean isMultiReturnExpr();

    /**
     * Get Temporary Location of the stack frame of the expression.
     *
     * @return temporary offset in the stack frame.
     */
    int getTempOffset();

    /**
     * Set Temporary Location in stack frame for storing expression result.
     *
     * @param tempOffset calculated temporary offset.
     */
    void setTempOffset(int tempOffset);

    boolean hasTemporaryValues();
}

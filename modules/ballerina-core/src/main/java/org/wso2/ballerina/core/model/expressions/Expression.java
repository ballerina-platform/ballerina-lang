/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.model.ExecutableExpr;
import org.wso2.ballerina.core.model.Node;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.types.BType;

/**
 * {@code Expression} represents a generic expression in Ballerina.
 *
 * @see AddExpression
 * @see VariableRefExpr
 * @see FunctionInvocationExpr
 * @since 0.8.0
 */
public interface Expression extends Node, ExecutableExpr {

    BType getType();

    void setType(BType type);

    int getOffset();

    void setOffset(int offset);
    
    /**
     * Get the source location of this expression.
     * Return the source file and the line number of this expression.
     * 
     * @return  Source location of this expression
     */
    public Position getLocation();
    
    /**
     * Set the source location of this expression.
     * 
     * @param location  Source location of this expression.
     */
    public void setLocation(Position location);
}

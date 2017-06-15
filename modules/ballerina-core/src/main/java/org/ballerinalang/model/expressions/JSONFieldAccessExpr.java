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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

/**
 * <p>
 * {@link JSONFieldAccessExpr} represents JSON field access operation.
 * @since 0.87
 */
public class JSONFieldAccessExpr extends FieldAccessExpr {

    /**
     * Creates a JSON field access expression.
     * 
     * @param location File name and the line number of the field access expression
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param varRefExpr Variable reference represented by the current field
     * @param fieldExpr Reference to the child field of the current field
     */
    public JSONFieldAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression varRefExpr,
                               FieldAccessExpr fieldExpr) {
        super(location, whiteSpaceDescriptor, varRefExpr, fieldExpr);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BType getType() {
        return BTypes.typeJSON;
    }
}

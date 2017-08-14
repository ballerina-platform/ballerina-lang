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
import org.ballerinalang.model.types.SimpleTypeName;

/**
 * {@code ConnectorInitExpr} represents a expression which creates a new connector instance.
 *
 * @since 0.8.0
 */
public class ConnectorInitExpr extends RefTypeInitExpr {
    private SimpleTypeName typeName;
    private ConnectorInitExpr parentConnectorInitExpr;
    // This variable has been introduced to support the filter connector framework
    private BType filterSupportedType;

    public ConnectorInitExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, SimpleTypeName typeName,
                             Expression[] argExprs) {
        super(location, whiteSpaceDescriptor, argExprs);
        this.typeName = typeName;
    }

    public ConnectorInitExpr getParentConnectorInitExpr() {
        return parentConnectorInitExpr;
    }

    public void setParentConnectorInitExpr(ConnectorInitExpr parentConnectorInitExpr) {
        this.parentConnectorInitExpr = parentConnectorInitExpr;
    }

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    public BType getFilterSupportedType() {
        return filterSupportedType;
    }

    public void setFilterSupportedType(BType filterSupportedType) {
        this.filterSupportedType = filterSupportedType;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}

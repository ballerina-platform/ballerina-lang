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
package org.ballerinalang.model.expressions.variablerefs;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.expressions.Expression;

import java.util.Map;

/**
 * {@code XMLAttributesRefExpr} represents an XML attributes reference in Ballerina.
 *
 * @since 0.89
 */
public class XMLAttributesRefExpr extends IndexBasedVarRefExpr {

    // Namespaces visible to this attribute reference expression.
    private Map<String, Expression> namespaces;

    /**
     * @param location Source location of the ballerina file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param varRefExpr Reference to the XML variable 
     * @param indexExpr Attribute expression
     */
    public XMLAttributesRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
            VariableReferenceExpr varRefExpr, Expression indexExpr) {
        super(location, whiteSpaceDescriptor, varRefExpr, indexExpr);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public void setIndexExpr(Expression indexExpr) {
        this.indexExpr = indexExpr;
    }

    public Map<String, Expression> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, Expression> namespaces) {
        this.namespaces = namespaces;
    }
}

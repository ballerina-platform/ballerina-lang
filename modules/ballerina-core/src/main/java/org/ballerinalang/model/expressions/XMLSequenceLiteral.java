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

/**
 * {@link XMLSequenceLiteral} represents an XML Sequence in Ballerina.
 *
 * @since 0.90
 */
public class XMLSequenceLiteral extends XMLLiteral {

    private Expression[] items;
    private Expression concatExpr;
    private boolean hasParent = false;
    
    public XMLSequenceLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression[] items) {
        super(location, whiteSpaceDescriptor);
        this.items = items;
    }

    public Expression[] getItems() {
        return items;
    }

    public void setItems(Expression[] content) {
        this.items = content;
    }
    
    public Expression getConcatExpr() {
        return concatExpr;
    }

    public void setConcatExpr(Expression concatExpr) {
        this.concatExpr = concatExpr;
    }

    public boolean isEmpty() {
        return items.length == 0;
    }

    public boolean hasParent() {
        return hasParent;
    }

    public void setHasParent(boolean hasParent) {
        this.hasParent = hasParent;
    }

    public void setParent(XMLElementLiteral parent) {
        for (Expression item : items) {
            if (item instanceof XMLElementLiteral) {
                ((XMLElementLiteral) item).setParent(parent);
            }
        }
        
        this.hasParent = true;
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}

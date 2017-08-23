/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.expressions.variablerefs.VariableReferenceExpr;

/**
 * Class to hold XML qualified name reference
 *
 * @since 0.89
 */
public class XMLQNameExpr extends AbstractExpression implements VariableReferenceExpr {
    private String localname;
    private Expression namepsaceUri;
    private String prefix;
    private boolean usedInXML = false;
    private boolean isLHSExpr;
    
    public XMLQNameExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String localname,
            Expression namepsaceUri, String prefix) {
        super(location, whiteSpaceDescriptor);
        this.localname = localname;
        this.prefix = prefix == null ? "" : prefix;
        this.namepsaceUri = namepsaceUri;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getLocalname() {
        return localname;
    }

    public void setLocalname(String localname) {
        this.localname = localname;
    }

    public Expression getNamepsaceUri() {
        return namepsaceUri;
    }

    public void setNamepsaceUri(Expression namepsaceUri) {
        this.namepsaceUri = namepsaceUri;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isUsedInXML() {
        return usedInXML;
    }

    public void setUsedInXML(boolean usedInXML) {
        this.usedInXML = usedInXML;
    }

    @Override
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    @Override
    public void setLHSExpr(boolean lhsExpr) {
        this.isLHSExpr = lhsExpr;
    }

    @Override
    public VariableReferenceExpr getParentVarRefExpr() {
        return null;
    }

    @Override
    public void setParentVarRefExpr(VariableReferenceExpr varRefExpr) {

    }
}

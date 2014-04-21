/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.query.api.query.selection.attribute;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.ExpressionValidator;

import java.util.HashSet;
import java.util.Set;

public class ComplexAttribute implements OutputAttribute {
    private String rename;
    private String attributeName;
    private Expression[] expressions;

    public ComplexAttribute(String rename, String attributeName, Expression... expressions) {
        this.rename = rename;
        this.attributeName = attributeName;
        this.expressions = expressions;
    }

    public String getRename() {
        return rename;
    }

//    @Override
//    public Attribute.Type getType() {
//        return null;
//    }

    public String getAttributeName() {
        return attributeName;
    }

    public Expression[] getExpressions() {
        return expressions;
    }

    @Override
    public Set<String> getDependencySet() {
        Set<String> dependencySet = new HashSet<String>();
        for (Expression expression : expressions) {
            dependencySet.addAll(ExpressionValidator.getDependencySet(expression));
        }
        return dependencySet;
    }
}

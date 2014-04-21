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
package org.wso2.siddhi.query.api.query.selection;

import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.exception.AttributeAlreadyExistException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.query.selection.attribute.ComplexAttribute;
import org.wso2.siddhi.query.api.query.selection.attribute.OutputAttribute;
import org.wso2.siddhi.query.api.query.selection.attribute.OutputAttributeExtension;
import org.wso2.siddhi.query.api.query.selection.attribute.SimpleAttribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Selector {
    private List<OutputAttribute> selectionList = new ArrayList<OutputAttribute>();
    private List<Variable> groupByList = new ArrayList<Variable>();
    private Condition havingCondition;

    public Selector select(String rename, Expression expression) {
        OutputAttribute outputAttribute = new SimpleAttribute(rename, expression);
        checkSelection(outputAttribute);
        selectionList.add(outputAttribute);
        return this;
    }

    private void checkSelection(OutputAttribute newAttribute) {
        for (OutputAttribute attribute : selectionList) {
            if (attribute.getRename().equals(newAttribute.getRename())) {
                throw new AttributeAlreadyExistException(attribute.getRename() + " is already defined as an output attribute ");
            }
        }
    }

    public Selector select(String rename, String attributeName, Expression... expressions) {
        OutputAttribute outputAttribute = new ComplexAttribute(rename, attributeName, expressions);
        checkSelection(outputAttribute);
        selectionList.add(outputAttribute);
        return this;
    }

    public Selector select(String rename, String extensionNamespace, String extensionFunctionName, Expression... expressions) {
        OutputAttribute outputAttribute = new OutputAttributeExtension(rename, extensionNamespace, extensionFunctionName, expressions);
        checkSelection(outputAttribute);
        selectionList.add(outputAttribute);
        return this;
    }

    public Selector having(Condition condition) {
        havingCondition = condition;
        return this;
    }

    public Selector groupBy(String streamId, String attributeName) {
        groupByList.add(new Variable(streamId, attributeName));
        return this;
    }

    public Selector groupBy(String attributeName) {
        groupByList.add(new Variable(attributeName));
        return this;
    }

    public Selector addGroupByList(List<Variable> list) {
        if (list != null) {
            groupByList.addAll(list);
        }
        return this;
    }

    public List<OutputAttribute> getSelectionList() {
        return selectionList;
    }

    public List<Variable> getGroupByList() {
        return groupByList;
    }

    public Condition getHavingCondition() {
        return havingCondition;
    }

    public Selector addSelectionList(List<OutputAttribute> projectionList) {
        for (OutputAttribute outputAttribute : projectionList) {
            checkSelection(outputAttribute);
            this.selectionList.add(outputAttribute);
        }
        return this;
    }

    public Set<String> getDependencySet() {
        Set<String> dependencySet = new HashSet<String>();
        for (OutputAttribute outputAttribute : selectionList) {
            dependencySet.addAll(outputAttribute.getDependencySet());
        }
        return dependencySet;
    }
}

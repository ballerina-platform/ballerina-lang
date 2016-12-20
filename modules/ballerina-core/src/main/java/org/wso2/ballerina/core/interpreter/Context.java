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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 1.0.0
 */
public class Context {

    //TODO: Rename this into BContext and move this to runtime package

    private SymbolTable table;
    private ControlStack controlStack;
    private CarbonMessage cMsg;
    private BalCallback balCallback;

    private  NodeVisitor currentNodeVisitor;
    private Statement currentStatement;

    private int currentResultOffset;

    protected Map<String, Object> properties = new HashMap();

    public Context() {
        table = new SymbolTable(null);
        controlStack = new ControlStack();
    }

    public Context(CarbonMessage cMsg) {
        this.cMsg = cMsg;
        table = new SymbolTable(null);
        controlStack = new ControlStack();
    }

    public ControlStack getControlStack() {
        return controlStack;
    }

    public BValue lookup(SymbolName id) {
        return table.get(id);
    }

    public void put(SymbolName id, BValue value) {
        table.put(id, value);
    }

    public void createChild() {
        SymbolTable child = new SymbolTable(table);
        this.table = child;
    }

    public void switchToParent() {
        table = table.getParent();
    }

    public CarbonMessage getCarbonMessage() {
        return cMsg;
    }

    public void setCarbonMessage(CarbonMessage cMsg) {
        this.cMsg = cMsg;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public BalCallback getBalCallback() {
        return balCallback;
    }

    public void setBalCallback(BalCallback balCallback) {
        this.balCallback = balCallback;
    }

    public NodeVisitor getCurrentNodeVisitor() {
        return currentNodeVisitor;
    }

    public void setCurrentNodeVisitor(NodeVisitor currentNodeVisitor) {
        this.currentNodeVisitor = currentNodeVisitor;
    }

    public Statement getCurrentStatement() {
        return currentStatement;
    }

    public void setCurrentStatement(Statement currentStatement) {
        this.currentStatement = currentStatement;
    }

    public int getCurrentResultOffset() {
        return currentResultOffset;
    }

    public void setCurrentResultOffset(int currentResultOffset) {
        this.currentResultOffset = currentResultOffset;
    }
}

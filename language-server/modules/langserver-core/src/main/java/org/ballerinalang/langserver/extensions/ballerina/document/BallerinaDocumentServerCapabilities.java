/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.document;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the ballerinaDocument service.
 *
 * @since 2.0.0
 */
public class BallerinaDocumentServerCapabilities extends BallerinaServerCapability {

    private boolean syntaxApiCalls;

    private boolean syntaxTree;

    private boolean syntaxTreeByName;

    private boolean syntaxTreeByRange;

    private boolean syntaxTreeLocate;

    private boolean syntaxTreeModify;

    private boolean triggerModify;

    private boolean diagnostics;

    private boolean syntaxTreeNode;

    private boolean executorPositions;

    private boolean resolveMissingDependencies;

    public boolean isSyntaxApiCalls() {
        return syntaxApiCalls;
    }

    public void setSyntaxApiCalls(boolean syntaxApiCalls) {
        this.syntaxApiCalls = syntaxApiCalls;
    }

    public boolean getSyntaxTree() {
        return syntaxTree;
    }

    public void setSyntaxTree(boolean syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public boolean getSyntaxTreeByRange() {
        return syntaxTreeByRange;
    }

    public void setSyntaxTreeByRange(boolean syntaxTreeByRange) {
        this.syntaxTreeByRange = syntaxTreeByRange;
    }

    public boolean getSyntaxTreeByName() {
        return syntaxTreeByName;
    }

    public void setSyntaxTreeByName(boolean syntaxTreeByName) {
        this.syntaxTreeByName = syntaxTreeByName;
    }

    public boolean getSyntaxTreeLocate() {
        return syntaxTreeLocate;
    }

    public void setSyntaxTreeLocate(boolean syntaxTreeLocate) {
        this.syntaxTreeLocate = syntaxTreeLocate;
    }

    public boolean getSyntaxTreeModify() {
        return syntaxTreeModify;
    }

    public void setSyntaxTreeModify(boolean syntaxTreeModify) {
        this.syntaxTreeModify = syntaxTreeModify;
    }

    public boolean getTriggerModify() {
        return triggerModify;
    }

    public void setTriggerModify(boolean triggerModify) {
        this.triggerModify = triggerModify;
    }

    public boolean getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(boolean diagnostics) {
        this.diagnostics = diagnostics;
    }

    public boolean getSyntaxTreeNode() {
        return syntaxTreeNode;
    }

    public void setSyntaxTreeNode(boolean syntaxTreeNode) {
        this.syntaxTreeNode = syntaxTreeNode;
    }

    public boolean getExecutorPositions() {
        return executorPositions;
    }

    public void setExecutorPositions(boolean executorPositions) {
        this.executorPositions = executorPositions;
    }

    public BallerinaDocumentServerCapabilities() {
        super(Constants.CAPABILITY_NAME);
    }

    public boolean isResolveMissingDependencies() {
        return resolveMissingDependencies;
    }

    public void setResolveMissingDependencies(boolean resolveMissingDependencies) {
        this.resolveMissingDependencies = resolveMissingDependencies;
    }
}

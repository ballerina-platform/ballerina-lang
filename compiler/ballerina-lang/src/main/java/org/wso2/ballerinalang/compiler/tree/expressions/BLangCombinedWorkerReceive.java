/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;

/**
 * Represents both alternate-receive and multiple-receive in worker communication.
 *
 * @since 2201.9.0
 */
public class BLangCombinedWorkerReceive extends BLangExpression implements ActionNode {

    private final NodeKind nodeKind;
    private List<BLangWorkerReceive> workerReceives;

    /**
     * Constructs a BLangCombinedWorkerReceive with NodeKind.
     *
     * @param nodeKind Combined worker kind. Either {@link NodeKind#ALTERNATE_WORKER_RECEIVE} or
     *                 {@link NodeKind#MULTIPLE_WORKER_RECEIVE}
     */
    public BLangCombinedWorkerReceive(NodeKind nodeKind) {
        this.nodeKind = nodeKind;
    }

    @Override
    public NodeKind getKind() {
        return nodeKind;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    public List<BLangWorkerReceive> getWorkerReceives() {
        return workerReceives;
    }

    public void setWorkerReceives(List<BLangWorkerReceive> workerReceives) {
        this.workerReceives = workerReceives;
    }
}

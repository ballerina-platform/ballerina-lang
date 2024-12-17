/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;

/**
 * Represents multiple-receive in worker communication.
 *
 * @since 2201.9.0
 */
public class BLangMultipleWorkerReceive extends BLangExpression implements ActionNode {

    private List<BLangReceiveField> receiveFields;

    @Override
    public NodeKind getKind() {
        return NodeKind.MULTIPLE_WORKER_RECEIVE;
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

    public List<BLangReceiveField> getReceiveFields() {
        return receiveFields;
    }

    public void setReceiveFields(List<BLangReceiveField> receiveFields) {
        this.receiveFields = receiveFields;
    }

    /**
     * This static inner class represents key-value pair in a multiple worker.
     *
     * @since 2201.9.0
     */
    public static class BLangReceiveField {
        private BLangIdentifier key;
        private BLangWorkerReceive workerReceive;

        public BLangIdentifier getKey() {
            return key;
        }

        public void setKey(BLangIdentifier key) {
            this.key = key;
        }

        public BLangWorkerReceive getWorkerReceive() {
            return workerReceive;
        }

        public void setWorkerReceive(BLangWorkerReceive workerReceive) {
            this.workerReceive = workerReceive;
        }
    }
}

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
package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.expressions.Expression;

/**
 * {@code TransformStmt} represents a transform statement.
 *
 * @since 0.8.7
 */
public class TransformStmt extends AbstractStatement {
    private BlockStmt transformBody;
    private Expression inputReference;
    private Expression outputReference;

    private TransformStmt(NodeLocation location, Expression inputReference, Expression outputReference,
                          BlockStmt transformBody) {
        super(location);
        this.transformBody = transformBody;
        this.inputReference = inputReference;
        this.outputReference = outputReference;
    }

    public BlockStmt getBody() {
        return transformBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    public Expression getInputReference() {
        return inputReference;
    }

    public Expression getOutputReference() {
        return outputReference;
    }

    /**
     * Builds a {@code TransformStmt} statement.
     *
     * @since 0.8.7
     */
    public static class TransformStmtBuilder {
        private NodeLocation location;
        private BlockStmt transformBody;
        private Expression inputReference;
        private Expression outputReference;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setTransformBody(BlockStmt transformBody) {
            this.transformBody = transformBody;
        }

        public void setInputReference(Expression inputReference) {
            this.inputReference = inputReference;
        }

        public void setOutputReference(Expression outputReference) {
            this.outputReference = outputReference;
        }

        public TransformStmt build() {
            return new TransformStmt(location, inputReference, outputReference, transformBody);
        }
    }
}

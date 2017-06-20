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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.expressions.Expression;

/**
 * {@code TransformStmt} represents a transform statement.
 *
 * @since 0.8.7
 */
public class TransformStmt extends AbstractStatement {
    private BlockStmt transformBody;
    private Expression[] inputExprs;
    private Expression[] outputExprs;

    private TransformStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression[] inputExprs,
                          Expression[] outputExprs, BlockStmt transformBody) {
        super(location);
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        this.transformBody = transformBody;
        this.inputExprs = inputExprs;
        this.outputExprs = outputExprs;
    }

    public BlockStmt getBody() {
        return transformBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Expression[] getOutputExprs() {
        return outputExprs;
    }

    public Expression[] getInputExprs() {
        return inputExprs;
    }

    /**
     * Builds a {@code TransformStmt} statement.
     *
     * @since 0.8.7
     */
    public static class TransformStmtBuilder {
        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private BlockStmt transformBody;
        private Expression[] inputExprs;
        private Expression[] outputExprs;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setTransformBody(BlockStmt transformBody) {
            this.transformBody = transformBody;
        }

        public void setOutputExprs(Expression[] outputExprs) {
            this.outputExprs = outputExprs;
        }

        public void setInputExprs(Expression[] inputExprs) {
            this.inputExprs = inputExprs;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        public TransformStmt build() {
            return new TransformStmt(location, whiteSpaceDescriptor, inputExprs, outputExprs, transformBody);
        }
    }
}

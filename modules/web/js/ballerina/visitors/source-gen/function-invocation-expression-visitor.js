/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import ASTFactory from '../../ast/ballerina-ast-factory';
import FunctionDefinitionVisitor from './function-definition-visitor';

/**
 * Constructor for Function invocation expression visitor
 * @param {ASTNode} parent - parent node
 * @constructor
 */
class FunctionInvocationExpressionVisitor extends AbstractStatementSourceGenVisitor {

    canVisitFuncInvocationExpression(functionInvocation) {
        return functionInvocation.getParent() !== this.visiting;
    }

    /**
     *
     * @param {FunctionInvocationExpression} functionInvocation
     */
    beginVisitFuncInvocationExpression(functionInvocation) {
        this.visiting = functionInvocation;
        let constructedSourceSegment = '';
        const packageName = functionInvocation.getPackageName();
        if (!_.isNil(packageName) && !_.isEmpty(packageName) && !_.isEqual(packageName, 'Current Package')) {
            constructedSourceSegment += packageName + functionInvocation.getChildWSRegion('nameRef', 1) + ':';
        }
        constructedSourceSegment += functionInvocation.getChildWSRegion('nameRef', 2)
            + functionInvocation.getFunctionName() + functionInvocation.getWSRegion(1);
        constructedSourceSegment += '(' + functionInvocation.getWSRegion(2);

        this.appendSource(constructedSourceSegment);
        functionInvocation.children.forEach((child, index) => {
            if (index !== 0) {
                const paramSource = ',' + (child.whiteSpace.useDefault ? ' ' : child.getWSRegion(0));
                this.appendSource(paramSource);
                constructedSourceSegment += paramSource;
            }
            if (ASTFactory.isFunctionInvocationExpression(child)) {
                child.accept(new FunctionInvocationExpressionVisitor(this));
            } else if (ASTFactory.isLambdaExpression(child)) {
                child.getLambdaFunction().accept(new FunctionDefinitionVisitor(this));
            } else {
                const childSource = child.getExpressionString();
                this.appendSource(childSource);
                constructedSourceSegment += childSource;
            }
        });
        const endingSource = ')' + functionInvocation.getWSRegion(3);
        this.appendSource(endingSource);
        constructedSourceSegment += endingSource;

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);

        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
    }

    visitFuncInvocationExpression(functionInvocation) {
    }

    endVisitFuncInvocationExpression() {
        this.visiting = null;
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default FunctionInvocationExpressionVisitor;

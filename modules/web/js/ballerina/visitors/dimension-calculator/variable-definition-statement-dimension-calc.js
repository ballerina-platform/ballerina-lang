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
import { util } from './../sizing-utils';
<<<<<<< HEAD
import ASTFactory from '../../ast/ballerina-ast-factory';
=======
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';
>>>>>>> Arrow conflict resolving.

/**
 * Dimension visitor class for Variable Definition Statement.
 *
 * @class VariableDefinitionStatementDimensionCalculatorVisitor
 * */
class VariableDefinitionStatementDimensionCalculatorVisitor {

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf VariableDefinitionStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf VariableDefinitionStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf VariableDefinitionStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - Variable Definition Statement node.
     *
     * @memberOf VariableDefinitionStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        const viewState = node.getViewState();
        util.populateSimpleStatementBBox(node.getStatementString(), viewState);

        const lambdaChildren = node.filterChildren(child => ASTFactory.isLambdaExpression(child));
        if (lambdaChildren.length > 0) {
            const funcViewState = lambdaChildren[0].getLambdaFunction().getViewState();
            viewState.bBox.h += funcViewState.bBox.h;
            viewState.bBox.w = funcViewState.bBox.w;
        }

        // check if it is an action invocation statement if so initialize it as an arrow.
        const statementChildren = node.filterChildren(BallerinaASTFactory.isActionInvocationExpression);
        if (statementChildren instanceof Array && statementChildren.length > 0) {
            viewState.components['statement-box'].arrow = true;
        }
    }
}

export default VariableDefinitionStatementDimensionCalculatorVisitor;

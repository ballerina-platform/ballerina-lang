/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';
import ASTFactory from '../../ast/ballerina-ast-factory';
import FunctionInvocationVisitor from './function-invocation-visitor';
import SimpleVariableReferenceExpressionVisitor from './simple-variable-reference-expression-visitor';
import ReferenceTypeInitExpressionVisitor from './reference-type-init-expression-visitor';
import TypeCastExpressionVisitor from './type-cast-expression-visitor';
import LeftOperandExpressionVisitor from './left-operand-expression-visitor';
import RightOperandExpressionVisitor from './right-operand-expression-visitor';

class ExpressionViewFactory {
    getExpressionView(args) {
        const expression = _.get(args, 'model');
        if (ASTFactory.isFunctionInvocation(expression)) {
            return new FunctionInvocationVisitor(_.get(args, 'parent'));
        } else if (ASTFactory.isSimpleVariableReferenceExpression(expression)) {
            return new SimpleVariableReferenceExpressionVisitor(_.get(args, 'parent'));
        } else if (ASTFactory.isReferenceTypeInitExpression(expression)) {
            return new ReferenceTypeInitExpressionVisitor(_.get(args, 'parent'));
        } else if (ASTFactory.isTypeCastExpression(expression)) {
            return new TypeCastExpressionVisitor(_.get(args, 'parent'));
        } else if (ASTFactory.isLeftOperandExpression(expression)) {
            return new LeftOperandExpressionVisitor(expression);
        } else if (ASTFactory.isRightOperandExpression(expression)) {
            return new RightOperandExpressionVisitor(expression);
        }
        return undefined;
    }
}

export default ExpressionViewFactory;

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
import _ from 'lodash';
import TypeMapperFunctionInvocationVisitor from './type-mapper-function-invocation-visitor';
import TypeMapperStructFieldAccessExpressionVisitor from './type-mapper-struct-field-access-expression-visitor';
import TypeMapperVariableReferenceExpressionVisitor from './type-mapper-variable-reference-expression-visitor';
import TypeMapperReferenceTypeInitExpressionVisitor from './type-mapper-reference-type-init-expression-visitor';
import TypeMapperTypeCastExpressionVisitor from './type-mapper-type-cast-expression-visitor';
import ASTFactory from './../../ast/ballerina-ast-factory';

class TypeMapperExpressionFactory {
    getExpressionVisitor(args) {
        var expression  = _.get(args, "model");
        if (ASTFactory.isFunctionInvocation(expression)) {
            return new TypeMapperFunctionInvocationVisitor(_.get(args, "parent"));
        } else if (ASTFactory.isStructFieldAccessExpression(expression)) {
            return new TypeMapperStructFieldAccessExpressionVisitor(_.get(args, "parent"));
        } else if (ASTFactory.isVariableReferenceExpression(expression)) {
            return new TypeMapperVariableReferenceExpressionVisitor(_.get(args, "parent"));
        } else if (ASTFactory.isReferenceTypeInitExpression(expression)) {
            return new TypeMapperReferenceTypeInitExpressionVisitor(_.get(args, "parent"));
        } else if (ASTFactory.isTypeCastExpression(expression)) {
            return new TypeMapperTypeCastExpressionVisitor(_.get(args, "parent"));
        }
    }
}

export default TypeMapperExpressionFactory;

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
define(['lodash', 'log', 'event_channel', '../../ast/module', './type-mapper-function-invocation-visitor',
        './type-mapper-arithmetic-expression-visitor', './type-mapper-logical-expression-visitor',
        './type-mapper-struct-field-access-expression-visitor', './type-mapper-variable-reference-expression-visitor',
        './type-mapper-reference-type-init-expression-visitor',
        './type-mapper-type-cast-expression-visitor'],
    function (_, log, EventChannel, AST, TypeMapperFunctionInvocationVisitor, TypeMapperArithmeticExpressionVisitor,
              TypeMapperLogicalExpressionVisitor, TypeMapperStructFieldAccessExpressionVisitor,
              TypeMapperVariableReferenceExpressionVisitor, TypeMapperReferenceTypeInitExpressionVisitor, 
              TypeMapperTypeCastExpressionVisitor) {

        var TypeMapperExpressionFactory = function () {
        };

        TypeMapperExpressionFactory.prototype.getExpressionVisitor = function (args) {
            var expression  = _.get(args, "model");
            if (expression instanceof AST.FunctionInvocation) {
                return new TypeMapperFunctionInvocationVisitor(_.get(args, "parent"));
            } else if (expression instanceof AST.ArithmeticExpression) {
                return new TypeMapperArithmeticExpressionVisitor(_.get(args, "parent"));
            } else if (expression instanceof AST.LogicalExpression) {
                return new TypeMapperLogicalExpressionVisitor(_.get(args, "parent"));
            } else if (expression instanceof AST.StructFieldAccessExpression) {
                return new TypeMapperStructFieldAccessExpressionVisitor(_.get(args, "parent"));
            } else if (expression instanceof AST.VariableReferenceExpression) {
                return new TypeMapperVariableReferenceExpressionVisitor(_.get(args, "parent"));
            } else if (expression instanceof AST.ReferenceTypeInitExpression) {
                return new TypeMapperReferenceTypeInitExpressionVisitor(_.get(args, "parent"));
            } else if (expression instanceof AST.TypeCastExpression) {
                return new TypeMapperTypeCastExpressionVisitor(_.get(args, "parent"));
            }
        };

        return TypeMapperExpressionFactory;
    });
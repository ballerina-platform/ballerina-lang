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
import log from 'log';
import EventChannel from 'event_channel';
import AST from '../../ast/module';
import FunctionInvocationVisitor from './function-invocation-visitor';
import StructFieldAccessExpressionVisitor from './struct-field-access-expression-visitor';
import VariableReferenceExpressionVisitor from './variable-reference-expression-visitor';
import ReferenceTypeInitExpressionVisitor from './reference-type-init-expression-visitor';
import TypeCastExpressionVisitor from './type-cast-expression-visitor';

class ExpressionViewFactory {
    getExpressionView(args) {
        var expression  = _.get(args, "model");
        if (expression instanceof AST.FunctionInvocation) {
            return new FunctionInvocationVisitor(_.get(args, "parent"));
        } else if (expression instanceof AST.StructFieldAccessExpression) {
            return new StructFieldAccessExpressionVisitor(_.get(args, "parent"));
        } else if (expression instanceof AST.VariableReferenceExpression) {
            return new VariableReferenceExpressionVisitor(_.get(args, "parent"));
        } else if (expression instanceof AST.ReferenceTypeInitExpression) {
            return new ReferenceTypeInitExpressionVisitor(_.get(args, "parent"));
        } else if (expression instanceof AST.TypeCastExpression) {
            return new TypeCastExpressionVisitor(_.get(args, "parent"));
        }
    }
}

export default ExpressionViewFactory;
    
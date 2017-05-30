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
import log from 'log';
import AbstractExpressionSourceGenVisitor from './abstract-expression-source-gen-visitor';
import TypeMapperVariableDefinitionVisitor from './type-mapper-variable-definition-visitor';

class TypeMapperVariableReferenceExpressionVisitor extends AbstractExpressionSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitVariableReferenceExpression(expression) {
        return true;
    }

    beginVisitVariableReferenceExpression(expression) {
        log.debug('Begin Visit Type Mapper Variable Reference Expression');
    }

    visitVariableReferenceExpression(expression) {
        log.debug('Visit Type Mapper Variable Reference Expression');
    }

    endVisitVariableReferenceExpression(expression) {
        if (expression.getVariableName()) {
            this.appendSource(expression.getVariableName());
        }
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Type Mapper Variable Reference Expression');
    }

    visitVariableDefinition(variableDefinition) {
        var variableDefinitionVisitor = new TypeMapperVariableDefinitionVisitor(this);
        variableDefinition.accept(variableDefinitionVisitor);
    }
}

export default TypeMapperVariableReferenceExpressionVisitor;

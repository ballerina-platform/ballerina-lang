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
import log from 'log';
import EventChannel from 'event_channel';
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import TransformStatement from '../../ast/statements/transform-statement';
import StatementVisitorFactory from './statement-visitor-factory';
import TypeMapperBlockStatementVisitor from './type-mapper-block-statement-visitor';

class TransformStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitTransformStatement(typeMapperDefinition) {
        return true;
    }

    beginVisitTransformStatement(typeMapperDefinition) {
        /**
         * set the configuration start for the type mapper definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */

        var constructedSourceSegment = '\n' + this.getIndentation() + 'transform ' +
            typeMapperDefinition.getInputParamAndIdentifier() + ' -> ' + typeMapperDefinition.getReturnType() +
            '  {\n';
        this.appendSource(constructedSourceSegment);
        this.indent();
        log.debug('Begin Visit TypeMapperDefinition');
    }

    visitTransformStatement(typeMapperDefinition) {
        log.debug('Visit TypeMapperDefinition');
    }

    endVisitTransformStatement(typeMapperDefinition) {
        this.appendSource( "}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit TypeMapperDefinition');
    }

    visitBlockStatement(blockStatement) {
        var blockStatementVisitor = new TypeMapperBlockStatementVisitor(this);
        blockStatement.accept(blockStatementVisitor);
    }
}

export default TransformStatementVisitor;

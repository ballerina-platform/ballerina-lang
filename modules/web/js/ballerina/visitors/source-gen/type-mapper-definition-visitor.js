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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import TypeMapperBlockStatementVisitor from './type-mapper-block-statement-visitor';

/**
 * @param parent
 * @constructor
 */
class TypeMapperDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitTypeMapperDefinition(typeMapperDefinition) {
        return true;
    }

    beginVisitTypeMapperDefinition(typeMapperDefinition) {
        /**
         * set the configuration start for the type mapper definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */

        var constructedSourceSegment = '\n' + this.getIndentation() + 'typemapper ' + typeMapperDefinition.getTypeMapperName() +
            ' (' + typeMapperDefinition.getInputParamAndIdentifier() + ' ) (' + typeMapperDefinition.getReturnType() +
            ' ) {\n';
        this.appendSource(constructedSourceSegment);
        this.indent();
        log.debug('Begin Visit TypeMapperDefinition');
    }

    visitTypeMapperDefinition(typeMapperDefinition) {
        log.debug('Visit TypeMapperDefinition');
    }

    endVisitTypeMapperDefinition(typeMapperDefinition) {
        this.outdent();
        this.appendSource(this.getIndentation() + '}\n');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit TypeMapperDefinition');
    }

    visitBlockStatement(blockStatement) {
        var blockStatementVisitor = new TypeMapperBlockStatementVisitor(this);
        blockStatement.accept(blockStatementVisitor);
    }
}

export default TypeMapperDefinitionVisitor;

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
import ASTNode from './node';

/**
 * @param connectionDeclarations
 * @param variableDeclarations
 * @param workerDeclarations
 * @param statements
 * @param configStart
 * @param configEnd
 * @constructor
 */
class CallableDefinition extends ASTNode {
    constructor(args) {
        super(args);
        this.connectionDeclarations = [];
        this.variableDeclarations = [];
        this.workerDeclarations = [];
        this.statements = [];
    }

    setConnectionDeclarations(connectionDeclarations, options) {
        if (!_.isNil(connectionDeclarations)) {
            this.setAttribute('connectionDeclarations', connectionDeclarations, options);
        }
    }

    setVariableDeclarations(variableDeclarations, options) {
        if (!_.isNil(variableDeclarations)) {
            this.setAttribute('variableDeclarations', variableDeclarations, options);
        }
    }

    setWorkerDeclarations(workerDeclarations, options) {
        if (!_.isNil(workerDeclarations)) {
            this.setAttribute('workerDeclarations', workerDeclarations, options);
        }
    }

    setStatements(statements, options) {
        if (!_.isNil(statements)) {
            this.setAttribute('statements', statements, options);
        }
    }

    getConnectionDeclarations() {
        return this.connectionDeclarations;
    }

    getVariableDeclarations() {
        return this.variableDeclarations;
    }

    getWorkerDeclarations() {
        return this.workerDeclarations;
    }

    getStatements() {
        return this.statements;
    }
}

export default CallableDefinition;

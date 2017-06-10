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
import Statement from './statement';
import FragmentUtils from './../../utils/fragment-utils';

/**
 * Class for Join clause in ballerina.
 * Must always be added to ForkJoinStatement as a child
 * @param args {{joinType:string, param: ParameterDefinition}} join type and the param definition.
 * @constructor
 */
class JoinStatement extends Statement {
    constructor(args) {
        super('JoinStatement');
        this._joinType = _.get(args, 'joinType', 'all');
        this._joinWorkers = _.get(args, 'joinWorkers', []);
        const parameterDefinition = this.getFactory().createParameterDefinition({typeName: 'map', name: 'm'});
        this._joinParameter = _.get(args, 'joinParam', parameterDefinition);
    }

    getWorkerDeclarations() {
        const workerDeclarations = [];
        const self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    }

    setJoinType(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('_joinType', type, options);
        }
    }

    setJoinConditionFromString(cond, callback) {
        const fragment = FragmentUtils.createJoinCondition(cond);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        this.initFromJson(parsedJson);
    }

    getJoinConditionString() {
        return (this._joinType === 'any' ? 'some ' + this._joinCount : this._joinType) + ' ' + this._joinWorkers.join(',');
    }

    getJoinType() {
        return this._joinType;
    }

    setJoinCount(count, options) {
        if (!_.isNil(count)) {
            this.setAttribute('_joinCount', count, options);
        }
    }

    getJoinCount() {
        return this._joinCount;
    }

    setJoinWorkers(workers, options) {
        if (!_.isNil(workers)) {
            this.setAttribute('_joinWorkers', workers, options);
        }
    }

    getJoinWorkers() {
        return this._joinWorkers;
    }

    setParameter(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('_joinParameter', type, options);
        }
    }

    getParameter() {
        return this._joinParameter;
    }

    getParameterAsString() {
        return this.getParameter().getParameterDefinitionAsString();
    }

    setParameterAsString(str) {
        const myRegexp = /^\s*(map\s*)([^\s\[\]]+)\s*$/g;
        const match = myRegexp.exec(str);
        if (match) {
            const factory = this.getFactory();
            const typeName = match[1];
            const name = match[2];
            const parameterDefinition = factory.createParameterDefinition({typeName, name});
            this.setParameter(parameterDefinition);
        }
    }

    initFromJson(jsonNode) {
        const self = this;
        self.setJoinType(jsonNode['join_type']);
        self.setJoinCount(jsonNode['join_count']);
        self.setJoinWorkers(jsonNode['join_workers']);

        const paramJsonNode = jsonNode['join_parameter'];
        if (paramJsonNode) {
            const paramASTNode = self.getFactory().createFromJson(paramJsonNode);
            paramASTNode.initFromJson(paramJsonNode);
            self.setParameter(paramASTNode);
        }

        if (jsonNode.children) {
            _.each(jsonNode.children, function (childNode) {
                let child = self.getFactory().createFromJson(childNode);
                self.addChild(child);
                child.initFromJson(childNode);
            });
        }
    }
}

export default JoinStatement;

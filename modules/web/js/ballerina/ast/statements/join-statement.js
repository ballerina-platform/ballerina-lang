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
 */
class JoinStatement extends Statement {
    /**
     * Constructor for Join statement
     * @param {object} args {{joinType:string, param: ParameterDefinition}} join type and the param definition.
     * @constructor
     */
    constructor(args) {
        super('JoinStatement');
        this._joinType = _.get(args, 'joinType', 'all');
        this._joinWorkers = _.get(args, 'joinWorkers', []);
        const parameterDefinition = this.getFactory().createParameterDefinition({ typeName: 'map', name: 'm' });
        this._joinParameter = _.get(args, 'joinParam', parameterDefinition);
        this._joinCount = _.get(args, 'joinCount');

        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: ' ',
            3: ' ',
            4: ' ',
            5: ' ',
            6: '\n',
            7: '\n',
        };

        this.whiteSpace.defaultDescriptor.children = {
            joinCondition: {
                0: '',
                1: '',
                2: '',
            },
        };
    }

    /**
     * Get the worker declarations
     * @return {Array} array of worker declarations
     */
    getWorkerDeclarations() {
        const workerDeclarations = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    }

    /**
     * Set the join type
     * @param {object} type join type
     * @param {object} options set attribute options
     * @returns {void}
     */
    setJoinType(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('_joinType', type, options);
        }
    }

    /**
     * Set join condition from expression string
     * @param {string} cond condition expression
     * @returns {void}
     */
    setJoinConditionFromString(cond) {
        const fragment = FragmentUtils.createJoinCondition(cond);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        this.initFromJson(parsedJson);
    }

    /**
     * Get join condition as a string
     * @return {string} join condition string
     */
    getJoinConditionString() {
        const workerWS = _.get(this.getWhiteSpaceDescriptor(), 'children.joinWorkers.children', {});
        let workers = '';
        for (let i = 0; i < this._joinWorkers.length; i++) {
            const workerName = this._joinWorkers[i];
            const workerW = workerWS[workerName].regions;
            workers += workerW[0] + workerName;
            if (i < this._joinWorkers.length - 1) {
                workers += workerW[1] + ',';
            }
        }
        let condition;
        if (this._joinType === 'any') {
            condition = 'some' + this.getChildWSRegion('joinCondition', 1) + this._joinCount + workers;
        } else {
            condition = this._joinType + workers;
        }
        return condition;
    }

    /**
     * Get join type
     * @return {object} join type
     */
    getJoinType() {
        return this._joinType;
    }

    /**
     * Set join count
     * @param {number} count join count
     * @param {object} options set attribute options
     * @returns {void}
     */
    setJoinCount(count, options) {
        if (!_.isNil(count)) {
            this.setAttribute('_joinCount', count, options);
        }
    }

    /**
     * Get join count
     * @return {number} join count
     */
    getJoinCount() {
        return this._joinCount;
    }

    /**
     * Set the workers inside join statement
     * @param {WorkerDeclaration[]} workers worker declarations
     * @param {object} options set attribute options
     * @returns {void}
     */
    setJoinWorkers(workers, options) {
        if (!_.isNil(workers)) {
            this.setAttribute('_joinWorkers', workers, options);
        }
    }

    /**
     * Get the join workers
     * @return {WorkerDeclaration[]} worker declaration array
     */
    getJoinWorkers() {
        return this._joinWorkers;
    }

    /**
     * Set parameter
     * @param {object} type parameter type
     * @param {object} options set attribute options
     * @returns {void}
     */
    setParameter(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('_joinParameter', type, options);
        }
    }

    /**
     * Get join parameter
     * @return {ParameterDefinition} join parameter
     */
    getParameter() {
        return this._joinParameter;
    }

    /**
     * Get parameter as string
     * @return {string} parameter as a string
     */
    getParameterAsString() {
        return this.getParameter().getParameterDefinitionAsString();
    }

    /**
     * Set parameter as string
     * @param {string} str parameter string
     * @returns {void}
     */
    setParameterAsString(str) {
        const myRegexp = /^\s*(map\s*)([^\s\[\]]+)\s*$/g;
        const match = myRegexp.exec(str);
        if (match) {
            const factory = this.getFactory();
            const typeName = match[1];
            const name = match[2];
            const parameterDefinition = factory.createParameterDefinition({ typeName, name });
            this.setParameter(parameterDefinition);
        }
    }

    /**
     * initialize JoinStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        const self = this;
        self.setJoinType(jsonNode.join_type);
        self.setJoinCount(jsonNode.join_count);
        self.setJoinWorkers(jsonNode.join_workers);

        const paramJsonNode = jsonNode.join_parameter;
        if (paramJsonNode) {
            const paramASTNode = self.getFactory().createFromJson(paramJsonNode);
            paramASTNode.initFromJson(paramJsonNode);
            self.setParameter(paramASTNode);
        }

        if (jsonNode.children) {
            _.each(jsonNode.children, (childNode) => {
                const child = self.getFactory().createFromJson(childNode);
                self.addChild(child);
                child.initFromJson(childNode);
            });
        }
    }
}

export default JoinStatement;

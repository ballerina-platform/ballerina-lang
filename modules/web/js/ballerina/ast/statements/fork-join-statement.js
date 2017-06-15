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

/**
 * Class for ForkJoin node in ballerina.
 * Must add a JoinStatement child to work properly.
 */
class ForkJoinStatement extends Statement {
    /**
     * Constructor for fork join statement
     */
    constructor() {
        super('ForkJoinStatement');
    }

    /**
     * Has timeout
     * @returns {boolean} has timeout
     */
    hasTimeout() {
        if (this.children.length === 0) {
            return false;
        }
        const child = this.children[this.children.length - 1];
        const factory = this.getFactory();
        return factory.isTimeoutStatement(child);
    }

    /**
     * Get Worker declarations
     * @return {[WorkerDeclaration]} worker declarations
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
     * Validates possible immediate child types.
     *
     * @param {ASTNode} node child node
     * @return {boolean} whether node is worker declaration
     * @override
     */
    canBeParentOf(node) {
        return this.getFactory().isWorkerDeclaration(node);
    }

    /**
     * initialize ForkJoinStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * AddChild to fork join
     * @param {ASTNode} child child node to add
     * @param {number} index node index
     * @param {boolean} ignoreTreeModifiedEvent ignore tree modified
     * @param {boolean} ignoreChildAddedEvent ignore child added
     * @param {boolean} generateId generate Id for event
     * @returns {void}
     */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId) {
        if (_.isUndefined(index)) {
            const factory = this.getFactory();
            let newIndex = this.children.length;
            if (factory.isWorkerDeclaration(child)) {
                while (factory.isJoinStatement(this.children[newIndex - 1]) ||
                    factory.isTimeoutStatement(this.children[newIndex - 1])) {
                    newIndex--;
                }
            } else if (factory.isJoinStatement(child)) {
                while (factory.isTimeoutStatement(this.children[newIndex - 1])) {
                    newIndex--;
                }
            } else if (!factory.isTimeoutStatement(child)) {
                throw new Error('Illegal child type in join');
            }
            super.addChild(child, newIndex, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        } else {
            super.addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        }
    }
}

export default ForkJoinStatement;

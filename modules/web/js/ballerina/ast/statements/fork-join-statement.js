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
 * @param args none.
 * @constructor
 */
class ForkJoinStatement extends Statement {
    constructor(args) {
        super('ForkJoinStatement');
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

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return this.getFactory().isWorkerDeclaration(node);
    }

    initFromJson(jsonNode) {
        let self = this;
        _.each(jsonNode.children, function (childNode) {
            let child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId) {
        if (_.isUndefined(index)) {
            const factory = this.getFactory();
            let index = this.children.length;
            if (factory.isWorkerDeclaration(child)) {
                while (factory.isJoinStatement(this.children[index - 1]) || factory.isTimeoutStatement(this.children[index - 1])) {
                    index--;
                }
            } else if (factory.isJoinStatement(child)) {
                while (factory.isTimeoutStatement(this.children[index - 1])) {
                    index--;
                }
            } else if (!factory.isTimeoutStatement(child)) {
                throw "Illegal child type in join";
            }
            super.addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        } else {
            super.addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        }
    }
}

export default ForkJoinStatement;

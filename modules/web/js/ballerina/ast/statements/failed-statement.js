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
import BallerinaASTFactory from './../ballerina-ast-factory';

/**
 * Class for Failed Statement.
 *
 * @class FailedStatement
 * */
class FailedStatement extends Statement {
    /**
     * Constructor for Failed statement.
     * @param {object} args - argument for the failed statement
     * */
    constructor(args) {
        super(args);
        this.type = 'FailedStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
            3: ' ',
        };
    }

    /**
     * Override the addChild function as needed to render retry always last.
     * @param {ASTNode} child - AST node
     * @param {number} index - index to insert into
     * @param {boolean} ignoreTreeModifiedEvent - ignore tree modified event
     * @param {boolean} ignoreChildAddedEvent - ignore child added event
     * @param {boolean} generateId - generate id
     * */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent = false, generateId = false) {
        if (BallerinaASTFactory.isRetry(child)) {
            Object.getPrototypeOf(this.constructor.prototype)
                .addChild.call(this, child, this.getChildren().length, ignoreTreeModifiedEvent,
                ignoreChildAddedEvent, generateId);
        } else {
            const retryChild = this.filterChildren(retry => BallerinaASTFactory.isRetry(retry));
            Object.getPrototypeOf(this.constructor.prototype)
                .addChild.call(this, child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
            if (retryChild.length !== 0) {
                this.removeChild(retryChild[0], true);
                Object.getPrototypeOf(this.constructor.prototype)
                    .addChild.call(this, retryChild[0], this.getChildren().length, ignoreTreeModifiedEvent,
                    ignoreChildAddedEvent, generateId);
            }
        }
    }

    /**
     * Check whether this can be a parent of mentioned child.
     * @param {ASTNode} child - AST Node.
     * @retrun {boolean} true if node can be a child else false.
     * */
    canBeParentOf(child) {
        let canBeAParent = true;
        if (BallerinaASTFactory.isRetry(child)) {
            canBeAParent = this.filterChildren(retry => BallerinaASTFactory.isRetry(retry)).length === 0;
        } else {
            canBeAParent = this.getFactory().isStatement(child);
        }
        return canBeAParent;
    }

    /**
     * Initialize the node from the json node.
     * @param {object} jsonNode - Json model for the node.
     * */
    initFromJson(jsonNode) {
        const self = this;
        let retryChild;
        let retryChildNode;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            if (BallerinaASTFactory.isRetry(child)) {
                retryChild = child;
                retryChildNode = childNode;
            } else {
                self.addChild(child, undefined, true, true);
                child.initFromJson(childNode);
            }
        });
        if (retryChild && retryChildNode) {
            self.addChild(retryChild, jsonNode.children.length, true, true);
            retryChild.initFromJson(retryChildNode);
        }
    }
}

export default FailedStatement;

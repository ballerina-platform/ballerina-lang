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
import ASTNode from '../node';

/**
 * Class to represent a block statement in ballerina.
 * @constructor
 */
class BlockStatement extends ASTNode {
    /**
     * Constructor for block statement
     */
    constructor() {
        super('BlockStatement');
    }

    /**
     * Add variable declaration
     * @param {string} name variable name
     * @param {string} type variable type name
     * @returns {void}
     */
    addVariableDeclaration(name, type) {
        const ballerinaASTFactory = this.getFactory();
        const leftExpression = type + ' ' + name;
        const args = {
            leftExpression,
            rightExpression: '',
            variableName: name,
            typeName: type,
            name,
        };
        const variableDefStmt = ballerinaASTFactory.createVariableDefinitionStatement(args);
        const leftStatement = ballerinaASTFactory.createLeftOperandExpression(args);
        leftStatement.setLeftOperandExpressionString('');
        const simpleVariableReferenceExpression = ballerinaASTFactory.createSimpleVariableReferenceExpression(args);
        const variableDefinition = ballerinaASTFactory.createVariableDefinition(args);
        simpleVariableReferenceExpression.addChild(variableDefinition);
        leftStatement.addChild(simpleVariableReferenceExpression);
        variableDefStmt.addChild(leftStatement);
        const index = _.findLastIndex(this.getChildren(), (child) => {
            return ballerinaASTFactory.isVariableDefinitionStatement(child);
        });
        this.addChild(variableDefStmt, index + 1);
    }

    /**
     * Initialize BlockStatement from json object
     * @param {Object} jsonNode - JSON object for initialization.
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
     * Remove child from model
     * @param {ASTNode} child child node
     * @param {boolean} ignoreModifiedTreeEvent whether ignore the tree modified event
     * @param {boolean} willVisit whether visit or not
     * @returns {void}
     */
    removeChild(child, ignoreModifiedTreeEvent, willVisit) {
        if (!_.isUndefined(willVisit) && willVisit !== true) {
            const parentModelChildren = this.children;
            for (let itr = 0; itr < parentModelChildren.length; itr++) {
                if (parentModelChildren[itr].id === child.id) {
                    parentModelChildren.splice(itr, 1);
                    break;
                }
            }
        } else {
            Object.getPrototypeOf(this.constructor.prototype).removeChild.call(this, child, ignoreModifiedTreeEvent);
        }
    }
}

export default BlockStatement;

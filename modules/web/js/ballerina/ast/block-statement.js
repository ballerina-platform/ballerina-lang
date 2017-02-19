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
define(['lodash', './node'], function (_, ASTNode) {

    /**
     * Class to represent a block statement in ballerina.
     * @constructor
     */
    var BlockStatement = function () {
        ASTNode.call(this, 'BlockStatement');
    };

    BlockStatement.prototype = Object.create(ASTNode.prototype);
    BlockStatement.prototype.constructor = BlockStatement;

    BlockStatement.prototype.addVariableDeclaration = function (name, type) {
        var self = this;
        var ballerinaASTFactory = this.getFactory();
        var leftExpression = type + ' ' + name;
        var args = {
            leftExpression: leftExpression,
            rightExpression: '',
            variableReferenceName:  name,
            typeName: type,
            name: name
        };
        var variableDefStmt = ballerinaASTFactory.createVariableDefinitionStatement(args);
        var leftStatement = ballerinaASTFactory.createLeftOperandExpression(args);
        leftStatement.setLeftOperandExpressionString('');
        var variableReferenceExpression = ballerinaASTFactory.createVariableReferenceExpression(args);
        var variableDefinition = ballerinaASTFactory.createVariableDefinition(args);
        variableReferenceExpression.addChild(variableDefinition);
        leftStatement.addChild(variableReferenceExpression);
        variableDefStmt.addChild(leftStatement);
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableDefinitionStatement(child);
        });
        this.addChild(variableDefStmt, index + 1);
    };

    /**
     * Initialize BlockStatement from json object
     * @param {Object} jsonNode - JSON object for initialization.
     */
    BlockStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    /**
     * Override the super call to addChild
     * @param {ASTNode} child
     * @param {number} index
     */
    BlockStatement.prototype.addChild = function (child, index,isModified,willVisit) {
        var self = this;

        if(self.getFactory().isAssignmentStatement(child)){
            index = _.findLastIndex(self.getChildren());
        }
        if (!_.isUndefined(willVisit) && willVisit != true){

            if (_.isUndefined(index)) {
                self.children.push(child);
            } else {
                self.children.splice(index, 0, child);
            }
            //setting the parent node - doing silently avoid subsequent change events
            child.setParent(self, {doSilently:true});
            child.generateUniqueIdentifiers();
        }else{
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child,index);
        }
    };

    BlockStatement.prototype.removeChild = function (child, ignoreModifiedTreeEvent, willVisit) {
        if (!_.isUndefined(willVisit) && willVisit != true) {
            var parentModelChildren = this.children;
            for (var itr = 0; itr < parentModelChildren.length; itr++) {
                if (parentModelChildren[itr].id === child.id) {
                    parentModelChildren.splice(itr, 1);
                    break;
                }
            }
        } else {
            Object.getPrototypeOf(this.constructor.prototype).removeChild.call(this, child, ignoreModifiedTreeEvent);
        }
    };

    return BlockStatement;
});

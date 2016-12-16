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
define(['require', 'event_channel', 'lodash'], function(require, EventChannel, _){

    /**
     * Constructor for the ASTNode
     * @class ASTNode
     * @augments EventChannel
     * @param type
     * @constructor
     */
    var ASTNode = function(type) {
        this.object = undefined;
        this.parent = undefined;
        this.children = [];
        this.type = type;
        this.id = uuid();
    };

    ASTNode.prototype = Object.create(EventChannel.prototype);
    ASTNode.prototype.constructor = ASTNode;

    ASTNode.prototype.getParent = function () {
        return this.parent;
    };

    ASTNode.prototype.setParent = function (parent) {
        this.parent = parent;
    };

    ASTNode.prototype.getChildren = function () {
        return this.children;
    };

    ASTNode.prototype.getType = function () {
        return this.type;
    };

    ASTNode.prototype.getLength = function () {
        return this.length;
    };

    ASTNode.prototype.getStartIndex = function () {
        return this.startIndex;
    };

    /**
     * Insert a given child to the children array for a given index or otherwise to the array normally
     * @param child
     * @param index
     * @fires  ASTNode#child-added
     */
    ASTNode.prototype.addChild = function (child, index) {
        if (_.isUndefined(index)) {
            this.children.push(child);
        } else {
            this.children.splice(index, 0, child);
        }
        /**
         * @event ASTNode#child-added
         */
        this.trigger('child-added', child, index);
        //setting the parent node
        child.setParent(this);
    };

    /**
     * Remove a given child from the AST tree
     * @param child
     * @param index
     * @fires  ASTNode#child-removed
     */
    ASTNode.prototype.removeChild = function (child) {
        var parentModelChildren = this.children;
        for (var itr = 0; itr < parentModelChildren.length; itr++) {
            if (parentModelChildren[itr].id === child.id) {
                parentModelChildren.splice(itr, 1);
                this.trigger("childRemovedEvent", child);
                break;
            }
        }
    };

    /**
     * Accept function in visitor pattern
     * @param visitor {ASTVisitor}
     */
    ASTNode.prototype.accept = function (visitor) {
        if(visitor.canVisit(this)) {
            visitor.beginVisit(this);
            var self = this;
            _.forEach(this.children, function (child) {
                // visit current child
                visitor.visit(child);
                // firing childVisitedEvent
                self.trigger("childVisitedEvent", child);
                // forward visitor down the hierarchy to visit children of current child
                // if visitor doesn't support visiting children of current child, it will break
                child.accept(visitor);
            });
            visitor.endVisit(this);
        }
    };

    /**
     * Indicates whether this can be the parent node for the give node
     * Used for drag and drop validations. Override as required.
     * @param node {ASTNode}
     * @return {boolean} Default is true
     */
    ASTNode.prototype.canBeParentOf = function (node) {
        return true;
    };

    /**
     * Indicates whether this can be a child node of given node.
     * Used for drag and drop validations. Override as required
     * @param node {ASTNode}
     * @return {boolean} Default is true
     */
    ASTNode.prototype.canBeAChildOf = function (node) {
        return true;
    };

    /**
     * Get factory.
     * @return {BallerinaASTFactory}
     */
    ASTNode.prototype.getFactory  = function() {
        return require('./ballerina-ast-factory');
    };

    /**
     * Get index of child.
     * @param {ASTNode}
     * @return {number}
     */
    ASTNode.prototype.getIndexOfChild  = function(child) {
        return _.findIndex(this.children, ['id', child.id]);
    };

    // Auto generated Id for service definitions (for accordion views)
    var uuid =  function (){
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };

    return ASTNode;

});
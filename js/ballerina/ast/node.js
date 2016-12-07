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
define(['event_channel', 'lodash'], function(EventChannel, _){

    var ASTNode = function(startIndex, length, parent, type) {
        this.object = undefined;
        this.parent = undefined;
        this.children = [];
        this.startIndex = startIndex;
        this.length = length;
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
     * Insert a guven child to the children array for a given index or otherwise to the array normally
     * @param child
     * @param index
     */
    ASTNode.prototype.addChild = function (child, index) {
        if (_.isUndefined(index)) {
            this.children.push(child);
        } else {
            this.children.splice(index, 0, child);
        }
    };

    /**
     * Accept function in visitor pattern
     * @param visitor {ASTVisitor}
     */
    ASTNode.prototype.accept = function (visitor) {
        if(visitor.canVisit(this)) {
            visitor.beginVisit(this);
            _.forEach(this.children, function (child) {
                // visit current child
                visitor.visit(child);
                // forward visitor down the hierarchy to visit children of current child
                // if visitor doesn't support visiting children of current child, it will break
                child.accept(visitor);
            });
            visitor.endVisit(this);
        }
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
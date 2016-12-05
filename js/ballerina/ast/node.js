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
define(['event_channel'], function(EventChannel){

    var ASTNode = function(startIndex, length, parent, type) {
        this.object = undefined;
        this.parent = undefined;
        this.children = [];
        this.startIndex = startIndex;
        this.length = length;
        this.type = type;
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

    ASTNode.prototype.accept = function (visitor) {
    };

    return ASTNode;

});
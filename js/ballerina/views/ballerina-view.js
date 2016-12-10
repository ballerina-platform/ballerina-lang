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
define(['lodash', 'log', './../visitors/ast-visitor'], function (_, log, ASTVisitor) {

    /**
     * A common class which consists functions of moving or resizing views.
     * @constructor
     */
    var BallerinaView = function (parent) {
        this._parent = parent;
        ASTVisitor.call(this);
    };

    BallerinaView.prototype = Object.create(ASTVisitor.prototype);
    BallerinaView.prototype.constructor = BallerinaView;

    BallerinaView.prototype.setWidth = function (newWidth) {
    };
    BallerinaView.prototype.setHeight = function (newHeight) {
    };
    BallerinaView.prototype.setXPosition = function (xPosition) {
    };
    BallerinaView.prototype.setYPosition = function (yPosition) {
    };
    BallerinaView.prototype.getWidth = function () {
    };
    BallerinaView.prototype.getHeight = function () {
    };
    BallerinaView.prototype.getXPosition = function () {
    };
    BallerinaView.prototype.getYPosition = function () {
    };
    BallerinaView.prototype.setParent = function (parent) {
        this._parent = parent;
    };
    BallerinaView.prototype.getParent = function () {
        return this._parent;
    };

    return BallerinaView;
});
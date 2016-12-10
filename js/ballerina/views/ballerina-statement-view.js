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
define(['lodash', 'log', './../visitors/statement-visitor'], function (_, log, StatementVisitor) {

    /**
     * A common class which consists functions of moving or resizing views.
     * @constructor
     */
    var BallerinaStatementView = function (parent) {
        this._parent = parent;
        StatementVisitor.call(this);
    };

    BallerinaStatementView.prototype = Object.create(StatementVisitor.prototype);
    BallerinaStatementView.prototype.constructor = BallerinaStatementView;

    BallerinaStatementView.prototype.setWidth = function (newWidth) {
    };
    BallerinaStatementView.prototype.setHeight = function (newHeight) {
    };
    BallerinaStatementView.prototype.setXPosition = function (xPosition) {
    };
    BallerinaStatementView.prototype.setYPosition = function (yPosition) {
    };
    BallerinaStatementView.prototype.getWidth = function () {
    };
    BallerinaStatementView.prototype.getHeight = function () {
    };
    BallerinaStatementView.prototype.getXPosition = function () {
    };
    BallerinaStatementView.prototype.getYPosition = function () {
    };
    BallerinaStatementView.prototype.setParent = function (parent) {
        this._parent = parent;
    };
    BallerinaStatementView.prototype.getParent = function () {
        return this._parent;
    };

    return BallerinaStatementView;
});
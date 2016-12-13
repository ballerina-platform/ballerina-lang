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
    var BallerinaStatementView = function (args) {
        StatementVisitor.call(this, args);
        this._parent = _.get(args, "parent");
        this._model = _.get(args, "model");
        this._container = _.get(args, "container");
        this._viewOptions = _.get(args, "viewOptions");
        this.toolPalette = _.get(args, "toolPalette");
        this._statementGroup = undefined;
        this._width = 0;
        this._height = 0;
        this._xPosition = 0;
        this._yPosition = 0;
    };

    BallerinaStatementView.prototype = Object.create(StatementVisitor.prototype);
    BallerinaStatementView.prototype.constructor = BallerinaStatementView;

    BallerinaStatementView.prototype.setWidth = function (newWidth) {
        this._width = newWidth;
    };
    BallerinaStatementView.prototype.setHeight = function (newHeight) {
        this._height = newHeight;
    };
    BallerinaStatementView.prototype.setXPosition = function (xPosition) {
        this._xPosition = xPosition;
    };
    BallerinaStatementView.prototype.setYPosition = function (yPosition) {
        this._yPosition = yPosition;
    };
    BallerinaStatementView.prototype.getWidth = function () {
        return this._width;
    };
    BallerinaStatementView.prototype.getHeight = function () {
        return this._height;
    };
    BallerinaStatementView.prototype.getXPosition = function () {
        return this._xPosition;
    };
    BallerinaStatementView.prototype.getYPosition = function () {
        return this._yPosition;
    };
    BallerinaStatementView.prototype.setParent = function (parent) {
        this._parent = parent;
    };
    BallerinaStatementView.prototype.getParent = function () {
        return this._parent;
    };
    BallerinaStatementView.prototype.getStatementGroup = function () {
        return this._statementGroup;
    };
    BallerinaStatementView.prototype.setStatementGroup = function (getStatementGroup) {
        this._statementGroup = getStatementGroup;
    };

    return BallerinaStatementView;
});
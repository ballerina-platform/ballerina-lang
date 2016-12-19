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
define(['lodash', './node'], function(_, ASTNode){

    var ConnectorDeclaration = function(options) {
        this._connectionOptions = options || {};
        this._connectorName = '';
        this._connectorType = '';
        this._timeout = '';
        this._uri = '';
        ASTNode.call(this);
    };

    ConnectorDeclaration.prototype = Object.create(ASTNode.prototype);
    ConnectorDeclaration.prototype.constructor = ConnectorDeclaration;

    ConnectorDeclaration.prototype.getOptions = function () {
        return this._connectionOptions;
    };

    ConnectorDeclaration.prototype.canBeConnectorOf = function (action) {
        var BallerinaASTFactory = this.getFactory();
    };

    ConnectorDeclaration.prototype.setConnectionOptions = function (opts) {
        this._connectionOptions = opts;
    };
    ConnectorDeclaration.prototype.setConnectorName = function (name) {
        this._connectorName = name;
    };
    ConnectorDeclaration.prototype.setConnectorType = function (type) {
        this._connectorType = type;
    };
    ConnectorDeclaration.prototype.setUri = function (uri) {
        this._uri = uri;
    };
    ConnectorDeclaration.prototype.setTimeout = function (timeout) {
        this._timeout = timeout;
    };

    ConnectorDeclaration.prototype.getConnectorName = function () {
        return this._connectorName;
    };
    ConnectorDeclaration.prototype.getConnectorType = function () {
        return this._connectorType;
    };
    ConnectorDeclaration.prototype.getUri = function () {
        return this._uri;
    };
    ConnectorDeclaration.prototype.getTimeout = function () {
        return this._timeout;
    };

    return ConnectorDeclaration;
});
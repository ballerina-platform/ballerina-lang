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

    /**
     * Defines a connector declaration AST node.
     * Default source will be as follows : http:HTTPConnector ep = new http:HTTPConnector("http://localhost:9090");
     * @param {Object} options - arguments for a connector.
     * @param {string} [options.connectorName="HTTPConnector"] - The name of the connector.
     * @param {string} [options.connectorVariable="ep"] - Variable identifier for the connector.
     * @param {string} [options.connectorPackageName="http"] - Package name of the connector.
     * @param {number} [options.timeout=""] - Timeout value.
     * @param {string} [options.uri="http://localhost:9090"] - The endpoint url.
     * @constructor
     * @augments ASTNode
     */
    var ConnectorDeclaration = function(options) {
        ASTNode.call(this, "ConnectorDeclaration");
        this._connectorName = _.get(options, "connectorName", "HTTPConnector");
        this._connectorVariable = _.get(options, "connectorVariable", "endpoint1");
        this._connectorType = _.get(options, "connectorType", "ConnectorDeclaration");
        this._connectorPkgName = _.get(options, "connectorPackageName", "http");
        this._timeout = _.get(options, "timeout", "");
        this._uri = _.get(options, "uri", "http://localhost:9090");
    };

    ConnectorDeclaration.prototype = Object.create(ASTNode.prototype);
    ConnectorDeclaration.prototype.constructor = ConnectorDeclaration;

    ConnectorDeclaration.prototype.canBeConnectorOf = function (action) {
        var BallerinaASTFactory = this.getFactory();
    };
    ConnectorDeclaration.prototype.setConnectorName = function (name) {
        this.setAttribute('_connectorName', name);
    };

    ConnectorDeclaration.prototype.setConnectorVariable = function (connectorVariable) {
        this.setAttribute('_connectorVariable', connectorVariable);
    };

    ConnectorDeclaration.prototype.setConnectorType = function (type) {
        this.setAttribute('_connectorType', type);
    };
    ConnectorDeclaration.prototype.setConnectorPkgName = function (pkgName) {
        this.setAttribute('_connectorPkgName', pkgName);
    };
    ConnectorDeclaration.prototype.setUri = function (uri) {
        // TODO: need a proper way of extracting the protocol
        if (this.validateUri(uri)) {
            var tokens = uri.split(":");
            this.setConnectorPkgName(tokens[0]);
            this.setConnectorName('HTTPConnector');
        }
        this.setAttribute('_uri', uri);
    };
    ConnectorDeclaration.prototype.setTimeout = function (timeout) {
        this._timeout = timeout;
    };

    ConnectorDeclaration.prototype.getConnectorName = function () {
        return this._connectorName;
    };

    ConnectorDeclaration.prototype.getConnectorVariable = function () {
        return this._connectorVariable;
    }

    ConnectorDeclaration.prototype.getConnectorType = function () {
        return this._connectorType;
    };
    ConnectorDeclaration.prototype.getUri = function () {
        return this._uri;
    };
    ConnectorDeclaration.prototype.getConnectorPkgName = function () {
        return this._connectorPkgName;
    };
    ConnectorDeclaration.prototype.getTimeout = function () {
        return this._timeout;
    };

    ConnectorDeclaration.prototype.validateUri = function (uri) {
        var response = uri.search(/^http[s]?\:\/\//);
        return response !== -1;
    };

    /**
     * initialize ConnectorDeclaration from json object
     * @param {Object} jsonNode to initialize from
     */
    ConnectorDeclaration.prototype.initFromJson = function (jsonNode) {
        this._connectorName = jsonNode.connector_name;
        this._connectorPkgName = jsonNode.connector_pkg_name;
        this._connectorVariable = jsonNode.connector_variable;
        this._uri = jsonNode.children[0].basic_literal_value;
        this.setConnectorType('ConnectorDeclaration');
    };

    return ConnectorDeclaration;
});
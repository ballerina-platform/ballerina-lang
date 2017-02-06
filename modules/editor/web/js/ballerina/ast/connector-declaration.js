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
        this._connectorActionsReference = _.get(options, "connectorActionsReference", []);
    };

    ConnectorDeclaration.prototype = Object.create(ASTNode.prototype);
    ConnectorDeclaration.prototype.constructor = ConnectorDeclaration;

    ConnectorDeclaration.prototype.canBeConnectorOf = function (action) {
        var BallerinaASTFactory = this.getFactory();
    };
    ConnectorDeclaration.prototype.setConnectorName = function (name, options) {
        this.setAttribute('_connectorName', name, options);
    };
    ConnectorDeclaration.prototype.addConnectorActionReference = function (model) {
        this._connectorActionsReference.push(model);
    };
    ConnectorDeclaration.prototype.removeConnectorActionReference = function (id) {
        _.pullAllBy(this._connectorActionsReference, [{ 'id': id }], 'id');
    };
    ConnectorDeclaration.prototype.setConnectorVariable = function (connectorVariable, options) {
        this.setAttribute('_connectorVariable', connectorVariable, options);
    };

    ConnectorDeclaration.prototype.setConnectorType = function (type, options) {
        this.setAttribute('_connectorType', type, options);
    };
    ConnectorDeclaration.prototype.setConnectorPkgName = function (pkgName, options) {
        this.setAttribute('_connectorPkgName', pkgName, options);
    };
    ConnectorDeclaration.prototype.setUri = function (uri, options) {
        // TODO: need a proper way of extracting the protocol
        if (this.validateUri(uri)) {
            var tokens = uri.split(":");
            this.setConnectorPkgName(tokens[0], options);
            this.setConnectorName('HTTPConnector', options);
        }
        this.setAttribute('_uri', uri, options);
    };
    ConnectorDeclaration.prototype.setTimeout = function (timeout, options) {
        this.setAttribute('_timeout', timeout, options);
    };

    ConnectorDeclaration.prototype.getConnectorName = function () {
        return this._connectorName;
    };

    ConnectorDeclaration.prototype.getConnectorActionsReference = function () {
        return this._connectorActionsReference;
    };

    ConnectorDeclaration.prototype.getConnectorVariable = function () {
        return this._connectorVariable;
    };

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
        this.setConnectorName(jonNode.connector_name, {doSilently: true});
        this.setConnectorPkgName(jonNode.connector_pkg_name, {doSilently: true});
        this.setConnectorVariable(jsonNode.connector_variable, {doSilently: true});
        this.setUri(jsonNode.children[0].basic_literal_value, {doSilently: true});
        this.setConnectorType('ConnectorDeclaration', {doSilently: true});
    };

    return ConnectorDeclaration;
});

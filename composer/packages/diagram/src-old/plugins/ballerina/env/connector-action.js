/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';

/**
 * @class ConnectorAction
 * @param args {Object} - args.name: name of the package
 * @constructor
 */
class ConnectorAction {
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this.action = _.get(args, 'action', '');
        this._parameters = _.get(args, 'parameters', []);
        this._returnParams = _.get(args, 'returnParams', []);
    }

    setName(name) {
        this._name = name;
    }

    getName() {
        return this._name;
    }

    /**
     * sets the id
     * @param {string} id
     */
    setId(id) {
        this._id = id;
    }

    /**
     * returns the id
     * @returns {string}
     */
    getId() {
        return this._id;
    }

    setAction(action) {
        this.action = action;
    }

    getAction() {
        return this.action;
    }

    /**
     * sets the parameters
     * @param [object] parameters
     */
    setParameters(parameters) {
        this._parameters = parameters;
    }

    /**
     * returns the parameters
     * @returns [object]
     */
    getParameters() {
        return this._parameters;
    }

    /**
     * sets the returnParams
     * @param [object] returnParams
     */
    setReturnParams(returnParams) {
        this._returnParams = returnParams;
    }

    /**
     * returns the returnParams
     * @returns [object]
     */
    getReturnParams() {
        return this._returnParams;
    }

    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setAction(jsonNode.name);
        this.setReturnParams(jsonNode.returnParams);
        this.setParameters(jsonNode.parameters);
    }

    initFromASTModel(connectorActionModel) {
        this.setName(connectorActionModel.getActionName());
        this.setId(`${connectorActionModel.getParent().getConnectorName()}-${connectorActionModel.getActionName()}`);
        this.setParameters(connectorActionModel.getArguments());
        this.setReturnParams(connectorActionModel.getReturnTypes());
    }
}

export default ConnectorAction;

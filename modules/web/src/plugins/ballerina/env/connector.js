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

import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * @class Connector
 * @augments
 * @param args {Object} - args.name: name of the package
 * @constructor
 */
class Connector {
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._actions = _.get(args, 'actions', []);
        this._params = _.get(args, 'params', []);
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

    addAction(action) {
        this._actions.push(action);
    }

    /**
     * remove the provided action item from the actions array
     * @param {ConnectorActionDefinition} actionDef - ConnectorActionDefinition to be removed
     */
    removeAction(actionDef) {
        _.remove(this._actions, (action) => {
            return _.isEqual(action.getName(), actionDef.getActionName());
        });
    }

    /**
     * remove all the action items of the provided connector definition
     * @param {ConnectorDefinition} connectorDef - ConnectorDefinition whose children need to be removed
     */
    removeAllActions(connectorDef) {
        _.each(connectorDef.getChildren(), (child) => {
            // TODO : Fix check
            // if (ASTFactory.isConnectorAction(child)) {
            this.removeAction(child);
            // }
        });
    }

    getActions() {
        return this._actions;
    }

    /**
     * returns the action by name
     * @param {string} actionName - name of the action
     */
    getActionByName(actionName) {
        return _.find(this.getActions(), action => _.isEqual(action.getName(), actionName));
    }

    addParam(param) {
        this._params.push(param);
    }

    getParams() {
        return this._params;
    }

    initFromJson(jsonNode) {
        this.setName(jsonNode.name);

        _.each(jsonNode.actions, (actionNode) => {
            const action = BallerinaEnvFactory.createConnectorAction();
            action.initFromJson(actionNode);
            this.addAction(action);
        });

        // Parameters
        _.each(jsonNode.parameters, (paramNode) => {
            this.addParam(paramNode);
        });
    }
}

export default Connector;

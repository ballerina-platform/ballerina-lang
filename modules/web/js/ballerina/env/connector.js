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
import log from 'log';
import _ from 'lodash';

import EventChannel from 'event_channel';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * @class Connector
 * @augments
 * @param args {Object} - args.name: name of the package
 * @constructor
 */
class Connector extends EventChannel {
    constructor(args) {
        super(args);
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._actions = _.get(args, 'actions', []);
        this._params = _.get(args, 'params', []);
    }

    setName(name) {
        const oldName = this._name;
        this._name = name;
        this.trigger('name-modified', name, oldName);
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
        this.trigger('connector-action-added', action);
    }

    /**
     * remove the provided action item from the actions array
     * @param {ConnectorActionDefinition} actionDef - ConnectorActionDefinition to be removed
     */
    removeAction(actionDef) {
        _.remove(this._actions, action => _.isEqual(action.getName(), actionDef.getActionName()));
        this.trigger('connector-action-removed', actionDef);
    }

    /**
     * remove all the action items of the provided connector definition
     * @param {ConnectorDefinition} connectorDef - ConnectorDefinition whose children need to be removed
     */
    removeAllActions(connectorDef) {
        const self = this;
        _.each(connectorDef.getChildren(), (child) => {
            if (BallerinaASTFactory.isConnectorAction(child)) {
                self.removeAction(child);
            }
        });
    }

    getActions(action) {
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
        this.trigger('param-added', param);
    }

    getParams(param) {
        return this._params;
    }

    initFromJson(jsonNode) {
        const self = this;

        this.setName(jsonNode.name);

        _.each(jsonNode.actions, (actionNode) => {
            const action = BallerinaEnvFactory.createConnectorAction();
            action.initFromJson(actionNode);
            self.addAction(action);
        });
    }
}

export default Connector;

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
import _ from 'lodash';
import log from 'log';
import BallerinaView from './ballerina-view';
import ActionDefinition from './../ast/action-definition';
import D3Utils from 'd3utils';

/**
 * The view to represent a action definition which is an AST visitor.
 * @param {Object} args - Arguments for creating the view.
 * @param {ActionDefinition} args.model - The action statement model.
 * @param {Object} args.container - The HTML container to which the view should be added to.
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @constructor
 */
class ActionDefinitionView extends BallerinaView {
    constructor(args) {

        super(args);

        if (_.isNil(this._model) || !(this._model instanceof ActionDefinition)) {
            log.error("Action definition undefined or is of different type." + this._model);
            throw "Action definition undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for action definition is undefined." + this._container);
            throw "Container for action definition is undefined." + this._container;
        }

    }

    setModel(model) {
        if (!_.isNil(model) && model instanceof ActionDefinition) {
            this._model = model;
        } else {
            log.error("Action definition undefined or is of different type." + model);
            throw "Action definition undefined or is of different type." + model;
        }
    }

    setContainer(container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error("Container for action definition is undefined." + container);
            throw "Container for action definition is undefined." + container;
        }
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    getModel() {
        return this._model;
    }

    getContainer() {
        return this._container;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    /**
     * Renders the view for action definition.
     * @returns {group} - The SVG group which holds the elements of the action definition.
     */
    render() {
        var group = D3Utils.group(this._container);
        // TODO : Draw the view of the action definition and add it to the above svg group.
        log.debug("Rendering action view.");
        return group;
    }
}

export default ActionDefinitionView;
    
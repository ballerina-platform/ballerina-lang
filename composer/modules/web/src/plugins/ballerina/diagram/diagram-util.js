/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React from 'react';
import _ from 'lodash';

import * as DefaultConfig from './views/default/designer-defaults';
import * as actionConfig from './views/action/designer-defaults';

import DefaultSizingUtil from './views/default/sizing-util';
import ActionSizingUtil from './views/action/sizing-util';

import DefaultPositioningUtil from './views/default/positioning-util';
import ActionPositioningUtil from './views/action/positioning-util';

import EndpointAggregatorUtil from './views/default/endpoint-aggregator-util';

import DefaultWorkerInvocationSyncUtil from './views/default/worker-invocation-sync-util';
import WorkerInvocationArrowPositionUtil from './views/default/worker-invocation-arrow-position-util';
import DefaultErrorCollectorUtil from './views/default/error-rendering-util';


const components = {};

const CONTROLLER_SUFFIX = 'Ctrl';

// We need to refactor this big time for the time being implementing the functionality.
// extend configs from default
const ActionConfig = _.merge(_.cloneDeep(DefaultConfig), actionConfig);

// initialize the utils.
const defaultSizingUtil = new DefaultSizingUtil();
const actionSizingUtil = new ActionSizingUtil();

const defaultPositioningUtil = new DefaultPositioningUtil();
const actionPositioningUtil = new ActionPositioningUtil();
const endpointAggregatorUtil = new EndpointAggregatorUtil();

const defaultWorkerInvocationSyncUtil = new DefaultWorkerInvocationSyncUtil();
const defaultInvocationArrowPositionUtil = new WorkerInvocationArrowPositionUtil();
const defaultErrorCollectorUtil = new DefaultErrorCollectorUtil();

// assign configs to utils.
defaultPositioningUtil.config = DefaultConfig;
defaultSizingUtil.config = DefaultConfig;

actionPositioningUtil.config = ActionConfig;
actionSizingUtil.config = ActionConfig;

// require all react components
function requireAll(requireContext) {
    const comp = {};
    requireContext.keys().forEach((item) => {
        const module = requireContext(item);
        if (module.default) {
            comp[module.default.name] = module.default;
        }
    });
    return comp;
}

function getComponentForNodeArray(nodeArray, mode = 'default') {
    // if undefined or null is passws return null.
    if (!nodeArray) {
        return null;
    }
    // lets load the view components diffrent modes.
    components.default = requireAll(require.context('./views/default/components/nodes', true, /\.jsx$/));
    components.action = requireAll(require.context('./views/action/components/', true, /\.jsx$/));


    // make sure what is passed is an array.
    nodeArray = _.concat([], nodeArray);

    return nodeArray.filter((child) => {
        const compName = child.constructor.name;
        if (components.default[compName]) {
            return true;
        }
        log.debug(`Unknown element type :${child.constructor.name}`);
        return false;
    }).map((child) => {
        // hide hidden elements
        if (child.viewState && child.viewState.hidden) {
            return undefined;
        }
        const compName = child.viewState.alias || child.constructor.name;

        if (components[mode][compName]) {
            return React.createElement(components[mode][compName], {
                model: child,
                // set the key to prevent warning
                // see: https://facebook.github.io/react/docs/lists-and-keys.html#keys
                key: child.getID(),
            }, null);
        } else if (components.default[compName]) {
            return React.createElement(components.default[compName], {
                model: child,
                // set the key to prevent warning
                // see: https://facebook.github.io/react/docs/lists-and-keys.html#keys
                key: child.getID(),
            }, null);
        }
        return undefined;
    });
}

function getConfig(mode) {
    switch (mode) {
        case 'action':
            return ActionConfig;
        default:
            return DefaultConfig;
    }
}

function getSizingUtil(mode) {
    switch (mode) {
        case 'action':
            return actionSizingUtil;
        default:
            return defaultSizingUtil;
    }
}

function getPositioningUtil(mode) {
    switch (mode) {
        case 'action':
            return actionPositioningUtil;
        default:
            return defaultPositioningUtil;
    }
}

function getEndpointAggregatorUtil(mode) {
    return endpointAggregatorUtil;
}


function getErrorCollectorUtil(mode) {
    return defaultErrorCollectorUtil;
}

function getWorkerInvocationSyncUtil(mode) {
    return defaultWorkerInvocationSyncUtil;
}

function getInvocationArrowPositionUtil(mode) {
    return defaultInvocationArrowPositionUtil;
}

function getOverlayComponent(nodeArray, mode = 'default') {
    // lets load the view components diffrent modes.
    components.default = requireAll(require.context('./views/default/components/utils', true, /\.jsx$/));

    // make sure what is passed is an array.
    nodeArray = _.concat([], nodeArray);

    return nodeArray.filter((child) => {
        const compName = child.kind;
        if (components.default[compName]) {
            return true;
        }
        log.debug(`Unknown element type :${child.constructor.name}`);
        return false;
    }).map((child) => {
        // hide hidden elements
        const compName = child.kind;
        let element;
        if (components[mode][compName]) {
            element = React.createElement(components[mode][compName], {
                model: child,
                key: child.props.key,
            }, null);
        } else if (components.default[compName]) {
            element = React.createElement(components.default[compName], {
                model: child,
                key: child.props.key,
            }, null);
        }
        return element;
    });
}


export {
    getComponentForNodeArray,
    requireAll,
    getSizingUtil,
    getPositioningUtil,
    getEndpointAggregatorUtil,
    getWorkerInvocationSyncUtil,
    getInvocationArrowPositionUtil,
    getOverlayComponent,
    getErrorCollectorUtil,
    getConfig,
};

// WIP please do not remove.
export class DiagramUtil {

    static getNodeControllers(node, mode) {
        // lets load controllers of modes.
        components.default = requireAll(require.context('./views/default/components/controllers', true, /\.jsx$/));
        components.action = requireAll(require.context('./views/action/components/controllers', true, /\.jsx$/));

        // hide hidden elements
        if (node.viewState && node.viewState.hidden) {
            return undefined;
        }

        let compName = node.viewState.alias || node.kind;
        compName += CONTROLLER_SUFFIX;

        const props = {
            model: node,
            // set the key to prevent warning
            // see: https://facebook.githubg.io/react/docs/lists-and-keys.html#keys
            key: node.getID(),
        };

        return DiagramUtil.createComponent(components, mode, compName, props);
    }

    static createComponent(comps, mode, name, props) {
        if (comps[mode][name]) {
            return React.createElement(comps[mode][name], props, null);
        } else if (comps.default[name]) {
            return React.createElement(comps.default[name], props, null);
        }
        return undefined;
    }

}

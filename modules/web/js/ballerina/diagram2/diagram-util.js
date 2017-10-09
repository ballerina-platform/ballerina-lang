import log from 'log';
import React from 'react';
import _ from 'lodash';
import Hidden from './views/action/components/hidden';

import * as DefaultConfig from './views/default/designer-defaults';
import DefaultSizingUtil from './views/default/sizing-util';
import DefaultPositioningUtil from './views/default/positioning-util';
import DefaultWorkerInvocationSyncUtil from './views/default/worker-invocation-sync-util';


const components = {};

// initialize the utils.
const defaultSizingUtil = new DefaultSizingUtil();
const defaultPositioningUtil = new DefaultPositioningUtil();
const defaultWorkerInvocationSyncUtil = new DefaultWorkerInvocationSyncUtil();

defaultSizingUtil.config = DefaultConfig;
defaultPositioningUtil.config = DefaultConfig;

// require all react components
function requireAll(requireContext) {
    const comp = {};
    requireContext.keys().map((item) => {
        const module = requireContext(item);
        if (module.default) {
            comp[module.default.name] = module.default;
        }
    });
    return comp;
}

function getComponentForNodeArray(nodeArray, mode = 'default') {
    // lets load the view components diffrent modes.
    components.default = requireAll(require.context('./views/default/components/nodes', true, /\.jsx$/));
    components.action = requireAll(require.context('./views/action/components/', true, /\.jsx$/));
    // components.compact = requireAll(require.context('./views/compact/components/', true, /\.jsx$/));

    // make sure what is passed is an array.
    nodeArray = _.concat([], nodeArray);

    return nodeArray.filter((child) => {
        const compName = child.constructor.name;
        if (components['default'][compName]) {
            return true;
        }
        log.debug(`Unknown element type :${child.constructor.name}`);
        return false;
    }).map((child) => {
        // hide hidden elements
        if (child.viewState && child.viewState.hidden) {
            return React.createElement(Hidden,{
                model: child,
                key: child.getID(),
            });
        }

        const compName = child.constructor.name;
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
    });
}

function getSizingUtil(mode) {
    if (mode === 'default') {
        return defaultSizingUtil;
    }
    return undefined;
}

function getPositioningUtil(mode) {
    if (mode === 'default') {
        return defaultPositioningUtil;
    }
    return undefined;
}

function getWorkerInvocationSyncUtil(mode) {
    if (mode === 'default') {
        return defaultWorkerInvocationSyncUtil;
    }
    return undefined;
}

function getOverlayComponent(nodeArray, mode = 'default') {
    // lets load the view components diffrent modes.
    components.default = requireAll(require.context('./views/default/components/utils', true, /\.jsx$/));

    // make sure what is passed is an array.
    nodeArray = _.concat([], nodeArray);

    return nodeArray.filter((child) => {
        const compName = child.kind;
        if (components['default'][compName]) {
            return true;
        }
        log.debug(`Unknown element type :${child.constructor.name}`);
        return false;
    }).map((child) => {
        // hide hidden elements
        const compName = child.kind;
        if (components[mode][compName]) {
            return React.createElement(components[mode][compName], {
                model: child,
                key: child.props.key,
            }, null);
        } else if (components.default[compName]) {
            return React.createElement(components.default[compName], {
                model: child,
                key: child.props.key,
            }, null);
        }
    });
}

/**
 * Get the max height among the workers
 * @param {Array} workers - array of workers
 * @returns {number} maximum worker height
 */
function getWorkerMaxHeight(workers) {
    const workerNode = _.maxBy(workers, (worker) => {
        return worker.body.viewState.bBox.h;
    });

    return workerNode.body.viewState.bBox.h + DefaultConfig.lifeLine.head.height + DefaultConfig.lifeLine.footer.height;
}
export {
    getComponentForNodeArray,
    requireAll,
    getSizingUtil,
    getPositioningUtil,
    getWorkerInvocationSyncUtil,
    getOverlayComponent,
    getWorkerMaxHeight,
};

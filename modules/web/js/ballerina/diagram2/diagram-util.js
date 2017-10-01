import log from 'log';
import React from 'react';
import Hidden from './views/action/components/hidden';

import * as DefaultConfig from './views/default/designer-defaults';
import DefaultSizingUtil from './views/default/sizing-util';
import DefaultPositioningUtil from './views/default/positioning-util';


const components = {};

// initialize the utils.
const defaultSizingUtil = new DefaultSizingUtil();
const defaultPositioningUtil = new DefaultPositioningUtil();

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
        const compName = child.viewState.alias ? (child.viewState.alias + 'Node') : child.constructor.name;
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

        const compName = child.viewState.alias ? (child.viewState.alias + 'Node') : child.constructor.name;
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
export {
    getComponentForNodeArray,
    requireAll,
    getSizingUtil,
    getPositioningUtil,
    getOverlayComponent,
};

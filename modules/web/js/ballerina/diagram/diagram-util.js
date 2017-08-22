import log from 'log';
import React from 'react';
import * as DesignerDefaults from './views/designer-defaults';
import * as DesignerAction from './views/compact/designer-defaults';

const components = {};
const diagramVisitors = {};

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

function getComponentForNodeArray(nodeArray, designer, mode = 'default') {
    // lets load the view components diffrent modes.
    components.default = requireAll(require.context('./views/default/components/', true, /\.jsx$/));
    components.action = requireAll(require.context('./views/action/components/', true, /\.jsx$/));
    components.compact = requireAll(require.context('./views/compact/components/', true, /\.jsx$/));

    return nodeArray.filter((child) => {
        const compName = child.constructor.name;
        if (components['default'][compName]) {
            return true;
        }
        log.debug(`Unknown element type :${child.constructor.name}`);
        return false;
    }).map((child) => {
        const compName = child.constructor.name;
        if (components[mode][compName]) {
            return React.createElement(components[mode][compName], {
                model: child,
                // set the key to prevent warning
                // see: https://facebook.github.io/react/docs/lists-and-keys.html#keys
                key: child.getID(),
                designer: designer,
                mode: mode,
            }, null);
        } else if (components.default[compName]) {
            return React.createElement(components.default[compName], {
                model: child,
                // set the key to prevent warning
                // see: https://facebook.github.io/react/docs/lists-and-keys.html#keys
                key: child.getID(),
                designer: designer,
                mode: mode,
            }, null);
        }
    });
}


function getDimentionVisitor(name, mode = 'default') {
    // lets load the view components diffrent modes.
    diagramVisitors.default = requireAll(require.context('./views/default/dimention-visitors/', true, /\.js$/));
    diagramVisitors.action = requireAll(require.context('./views/action/dimention-visitors/', true, /\.js$/));
    diagramVisitors.compact = requireAll(require.context('./views/compact/dimension-visitors/', true, /\.js$/));

    if (diagramVisitors[mode][name]) {
        return diagramVisitors[mode][name];
    } else if (diagramVisitors.default[name]) {
        return diagramVisitors.default[name];
    }
}

function getDesigner(modes) {
    const designer = {};
    Object.assign(designer, DesignerDefaults);
    if (!_.isNil(modes)) {
        modes.forEach((mode) => {
            const modeDesigner = require('./views/' + mode + '/designer-defaults');
            Object.assign(designer, modeDesigner);
        });
    }
    return designer;
}

export {
    getComponentForNodeArray,
    requireAll,
    getDimentionVisitor,
    getDesigner,
};

import log from 'log';
import React from 'react';
import * as DesignerDefaults from './views/default/designer-defaults';
import * as DesignerAction from './views/compact/designer-defaults';
import Hidden from './views/action/components/hidden';

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

function getComponentForNodeArray(nodeArray, mode = 'default') {
    // lets load the view components diffrent modes.
    components.default = requireAll(require.context('./views/default/components/nodes', true, /\.jsx$/));
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


function getDimentionVisitor(name, mode = 'default') {
    // lets load the view components diffrent modes.
    diagramVisitors.default = requireAll(require.context('./views/default/visitors/dimention', true, /\.js$/));
    //diagramVisitors.action = requireAll(require.context('./views/action/visitors/dimention', true, /\.js$/));
    //diagramVisitors.compact = requireAll(require.context('./views/compact/visitors/dimention', true, /\.js$/));

    if (diagramVisitors[mode][name]) {
        return diagramVisitors[mode][name];
    } else if (diagramVisitors.default[name]) {
        return diagramVisitors.default[name];
    }
}

function getPositionVisitor(name) {
    // lets load the view components diffrent modes.
    console.log(name);
    let diagramVisitors = requireAll(require.context('./views/default/visitors/position', true, /\.js$/));
    return diagramVisitors[name];
}

function getDesigner(modes) {
    const designer = {};
    Object.assign(designer, DesignerDefaults);
    if (!_.isNil(modes)) {
        modes.forEach((mode) => {
            if (mode !== 'default') {
                const modeDesigner = require('./views/' + mode + '/designer-defaults');
                Object.assign(designer, modeDesigner);
            }
        });
    }
    return designer;
}

export {
    getComponentForNodeArray,
    requireAll,
    getDimentionVisitor,
    getPositionVisitor,
    getDesigner,
};

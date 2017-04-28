import React from 'react';
import log from 'log';

// require all react components
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item, index) => {
        var module = requireContext(item);
        if (module.default) {
            components[module.default.name] = module.default;
        }
    });
    return components;
}

const components =  requireAll(require.context('./', true, /\.jsx$/));

function getComponentForNodeArray(nodeArray) {
    return nodeArray.map((child) => {
        let compName = child.constructor.name;
        if (components[compName]) {
            return React.createElement(components[compName], {
                model: child
            }, null);
        } else {
            log.error('Unknown element type :' + child.constructor.name)
        }
    });
}

export default getComponentForNodeArray;

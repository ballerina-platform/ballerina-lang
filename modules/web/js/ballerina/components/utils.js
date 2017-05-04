import log from 'log';
import React from 'react';

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

function getComponentForNodeArray(nodeArray) {
    const components = requireAll(require.context('./', true, /\.jsx$/));
    return nodeArray.filter((child) => {
        let compName = child.constructor.name;
        if (components[compName]) {
            return true;
        } else {
            log.error('Unknown element type :' + child.constructor.name);
            return false;
        }
      }).map((child) => {
          let compName = child.constructor.name;
          if (components[compName]) {
              return React.createElement(components[compName], {
                  model: child,
                  // set the key to prevent warning
                  //see: https://facebook.github.io/react/docs/lists-and-keys.html#keys
                  key: child.getID()
              }, null);
          }
      });
}

export {
    getComponentForNodeArray,
    requireAll
};

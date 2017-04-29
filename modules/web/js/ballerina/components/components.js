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
 
import React from 'react';
import log from 'log';

// require all react components
/*
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
                model: child,
                // set the key to prevent warning 
                //see: https://facebook.github.io/react/docs/lists-and-keys.html#keys
                key: child.getID()
            }, null);
        } else {
            log.error('Unknown element type :' + child.constructor.name)
        }
    });
}

export {
  components
};

export default getComponentForNodeArray;
*/

export function getComponentForNodeArray(nodeArray, components) {
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
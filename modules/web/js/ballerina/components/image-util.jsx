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

// require all react images
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item) => {
        var module = requireContext(item);
        if (module) {
            components[_getBasename(item, '.svg')] = module;
        }
    });
    return components;
}

function _getBasename(filename, ext) {
    return filename.substring(2, filename.indexOf(ext));
}

const images = requireAll(require.context('images', true, /\.svg$/));

class ImageUtils {
    static getSVGIconString (iconName) {
        return images[iconName];
    }
}

export default ImageUtils;

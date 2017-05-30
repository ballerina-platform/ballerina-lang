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
import ASTNode from './node';

/**
 * constructs PackageDefinition
 * @param args
 * @constructor
 */
class PackageDefinition extends ASTNode {
    constructor(args) {
        super("PackageDefinition");
        this._packageName = _.get(args, 'packageName');
    }

    /**
     * setter for package name
     * @param name
     */
    setPackageName(packageName, options) {
        if(!_.isNil(packageName)){
            var self = this,
                changeCallBack = function() {
                    self.trigger('package-name-changed');
                };

            options = options || {};
            options.undoCallBack = changeCallBack;
            options.redoCallBack = changeCallBack;

            this.setAttribute('_packageName', packageName, options);
        }
    }

    /**
     * getter for package name
     * @returns {String}
     */
    getPackageName() {
        return this._packageName;
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        //TODO : avoid check for . (current package)
        if (!_.isEqual(jsonNode.package_name, '.')) {
            this.setPackageName(jsonNode.package_name, {doSilently: true});
        }

    }
}

export default PackageDefinition;



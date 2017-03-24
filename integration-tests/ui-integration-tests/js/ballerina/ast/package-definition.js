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
define(['lodash', './node'], function (_, ASTNode) {

    /**
     * constructs PackageDefinition
     * @param args
     * @constructor
     */
    var PackageDefinition = function (args) {
        this._packageName = _.get(args, 'packageName');

        ASTNode.call(this, "PackageDefinition");
    };

    PackageDefinition.prototype = Object.create(ASTNode.prototype);
    PackageDefinition.prototype.constructor = PackageDefinition;

    /**
     * setter for package name
     * @param name
     */
    PackageDefinition.prototype.setPackageName = function (packageName, options) {
      if(!_.isNil(packageName)){
          this.setAttribute('_packageName', packageName, options);

          /**
           * @event ASTNode#tree-modified
           */
          this.trigger('tree-modified', {
              origin: this,
              type: 'node-modified',
              title: 'package name change'
          });
      }
    };

    /**
     * getter for package name
     * @returns {String}
     */
    PackageDefinition.prototype.getPackageName = function () {
      return this._packageName;
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    PackageDefinition.prototype.initFromJson = function (jsonNode) {
        //TODO : avoid check for . (current package)
        if (!_.isEqual(jsonNode.package_name, '.')) {
            this.setPackageName(jsonNode.package_name, {doSilently: true});
        }

    };

    return PackageDefinition;

});

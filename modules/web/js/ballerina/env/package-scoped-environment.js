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
define(['lodash', './package', './environment'], function(_, Package, Environment){

    var PackageScopedEnvironment = function (args) {
        this._packages = _.get(args, 'packages', []);
        this.init();
    };

    PackageScopedEnvironment.prototype.init = function () {
        this._packages = _.union(this._packages, Environment.getPackages());
        this._currentPackage = new Package({name:'Current Package'});
        this._packages.push(this._currentPackage);
    };

    PackageScopedEnvironment.prototype.getCurrentPackage = function () {
      return this._currentPackage;
    };

    PackageScopedEnvironment.prototype.resetCurrentPackage = function () {
        this._currentPackage = new Package({name:'Current Package'});
    };

    /**
     * @return {[Package]}
     */
    PackageScopedEnvironment.prototype.getPackages = function() {
        return this._packages;
    };

    PackageScopedEnvironment.prototype.searchPackage = function(query, exclude){
        var search_text = query;
        var exclude_packages = exclude;
        var result = _.filter(this._packages, function (package) {
            var existing = _.filter(exclude_packages, function (ex) {
                return package.getName() == ex;
            });
            return (existing.length == 0) && (_.includes(package.getName().toUpperCase(), search_text.toUpperCase()));
        });
        return result;
    };

    return PackageScopedEnvironment;

});
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
import _ from 'lodash';
import Package from './package';
import Environment from './environment';

class PackageScopedEnvironment {
    constructor(args) {
        this._packages = _.get(args, 'packages', []);
        this._types = _.get(args, 'types', []);
        this.init();
    }

    init() {
        this._packages = _.union(this._packages, Environment.getPackages());
        this._types = _.union(this._types, Environment.getTypes());
        this._currentPackage = new Package({ name: 'Current Package' });
        this._packages.push(this._currentPackage);
    }

    /**
     * Add given package array to the existing package array
     * @param {Package[]} packages - package array to be added 
     */
    addPackages(packages) {
        this._packages = _.union(this._packages, packages);
    }

    getCurrentPackage() {
        return this._currentPackage;
    }  

    resetCurrentPackage() {
        this._currentPackage = new Package({ name: 'Current Package' });
    }

    /**
     * @return {[Package]}
     */
    getPackages() {
        return this._packages;
    }

    /**
     * @return {[Package]}
     */
    getFilteredPackages(excludes) {
        return this._packages.filter((item)=>{
            let status = _.isNull(_.find(excludes, item.getName()));
            return status;
        });
    }    

    getPackageByName(packageName) {
        if (_.isEqual(packageName, 'Current Package')) {
            return this._currentPackage;
        } else {
            return _.find(this._packages, function (pckg) {
                return pckg.getName() === packageName;
            });
        }
    }

    searchPackage(query, exclude) {
        var search_text = query;
        var exclude_packages = exclude;
        var result = _.filter(this._packages, function (pckg) {
            var existing = _.filter(exclude_packages, function (ex) {
                return pckg.getName() == ex;
            });
            return (existing.length == 0) && new RegExp(search_text.toUpperCase()).exec(pckg.getName().toUpperCase());
        });
        return result;
    }

    /**
     * get available types for this environment including struct types
     * @returns {String[]}
     */
    getTypes() {
        let structs = this.getCurrentPackage().getStructDefinitions().map(function(struct){
            return struct.getStructName();
        });
        return _.union(this._types, structs);
    }
}

export default PackageScopedEnvironment;

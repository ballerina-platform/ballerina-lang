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

    setCurrentPackage(pkg) {
        this._currentPackage = pkg;
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
        return this._packages.filter((item) => {
            for (let i = 0; i < excludes.length; i++) {
                if (excludes[i] === item.getName()) {
                    return false;
                }
            }
            return true;
        });
    }

    getPackageByName(packageName) {
        if (_.isEqual(packageName, 'Current Package')) {
            return this._currentPackage;
        }
        return _.find(this._packages, pckg => pckg.getName() === packageName);
    }

    /**
     * Get packages by identifier.
     * E.g. : for system:println(), system will be the package identifier. This method will
     * go through all the package names and split by last index.
     * @param {any} packageIdentifier package identifier
     * @returns package with identifier
     * @memberof PackageScopedEnvironment
     */
    getPackageByIdentifier(packageIdentifier) {
        // TODO : this will break when imports have custom identifiers
        if (_.isEqual(packageIdentifier, 'Current Package')) {
            return this._currentPackage;
        }
        return _.find(this._packages, pckg => _.last(_.split(pckg.getName(), '.')) === packageIdentifier);
    }

    searchPackage(query, excludePackages) {
        const result = _.filter(this._packages, (pckg) => {
            const existing = _.filter(excludePackages, exclude => pckg.getName() === exclude);
            return (existing.length === 0) && new RegExp(query.toUpperCase()).exec(pckg.getName().toUpperCase());
        });
        return result;
    }

    /**
     * get available types for this environment including struct types
     * @returns {String[]}
     */
    getTypes() {
        const structs = this.getCurrentPackage().getStructDefinitions().map(struct => struct.getStructName());
        return _.union(this._types, structs);
    }
}

export default PackageScopedEnvironment;

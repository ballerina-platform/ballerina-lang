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
import TypeLattice from 'plugins/ballerina/type-lattice/type-lattice';
import Package from './package';
import Environment from './environment';

class PackageScopedEnvironment {
    constructor(args) {
        this._packages = _.get(args, 'packages', []);
        this._types = _.get(args, 'types', []);
        this._annotationAttachmentTypes = [];
        this._typeLattice = _.get(args, 'typeLattice', TypeLattice);
        this._initialized = false;
    }

    init() {
        if (this._initialized) {
            return;
        }
        this._packages = _.union(this._packages, Environment.getPackages());
        this._types = _.union(this._types, Environment.getTypes());
        this._currentPackage = new Package({name: 'Current Package'});
        this._packages.push(this._currentPackage);
        this._annotationAttachmentTypes = Environment.getAnnotationAttachmentTypes();
        this._typeLattice = Environment.getTypeLattice();
        this._builtInPackage = _.find(this._packages, pkg => pkg.getName() === 'ballerina.builtin');
        this._builtInCurrentPackage;
        this._initialized = true;
    }

    /**
     * Add given package array to the existing package array
     * @param {Package[]} packages - package array to be added
     */
    addPackages(packages) {
        this._packages = _.union(this._packages, packages);
    }

    getCurrentPackage() {
        return this._currentPackage || new Package({name: 'Current Package'});
    }

    resetCurrentPackage() {
        this._currentPackage = new Package({name: 'Current Package'});
        this._builtInCurrentPackage = null;
    }

    setCurrentPackage(pkg) {
        this._currentPackage = pkg;
        this._packages = this._packages.filter((item) => {
            return item.getName() !== 'Current Package';
        });
        this._packages.push(pkg);
        this._builtInCurrentPackage = null;
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
        if (!packageName) {
            return this.getBuiltInCurrentPackage();
        }
        return _.find(this._packages, pckg => pckg.getName() === packageName);
    }

    /**
     * Get built in package
     * @returns built in package
     * @memberof PackageScopedEnvironment
     */
    getBuiltInPackage() {
        return this._builtInPackage || new Package({name: 'ballerina.builtin'});
    }

    /**
     * Gets complete inscope package inclusive of current package and built in package.
     * @memberof PackageScopedEnvironment
     */
    getBuiltInCurrentPackage() {
        if (!this._builtInCurrentPackage) {
            this._builtInCurrentPackage = new Package({name: 'Current BuiltIn Package'});
            this._builtInCurrentPackage = this.mergePackages(this.getBuiltInPackage(), this._builtInCurrentPackage);
            this._builtInCurrentPackage = this.mergePackages(this.getCurrentPackage(), this._builtInCurrentPackage);
        }
        return this._builtInCurrentPackage;
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
        if (_.isEqual(packageIdentifier, 'Current Package') || _.isEqual(packageIdentifier, '')) {
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
        const structs = this.getCurrentPackage().getStructDefinitions().map(struct => struct.getName());
        return _.union(this._types, structs);
    }

    /**
     * Merge package1 into package2 and returns merged package2
     * @param {Package} package1 package 1
     * @param {Package} package2 package 2
     * @returns {Package} merged package
     */
    mergePackages(package1, package2) {
        // merge function definitions
        const pkg1FunctionDefinitions = package1.getFunctionDefinitions();
        const pkg2FunctionDefinitions = package2.getFunctionDefinitions();
        package2.setFunctionDefinitions(this.mergePackageItems(pkg1FunctionDefinitions, pkg2FunctionDefinitions));

        // merge struct definitions
        const pkg1StructDefinitions = package1.getStructDefinitions();
        const pkg2StructDefinitions = package2.getStructDefinitions();
        package2.setStructDefinitions(this.mergePackageItems(pkg1StructDefinitions, pkg2StructDefinitions));

        // merge connector definitions
        const pkg1Connectors = package1.getConnectors();
        const pkg2Connectors = package2.getConnectors();
        package2.setConnectors(this.mergePackageItems(pkg1Connectors, pkg2Connectors));

        // merge type definitions
        const pkg1TypeDefinitions = package1.getTypeDefinitions();
        const pkg2TypeDefinitions = package2.getTypeDefinitions();
        package2.setTypeDefinitions(this.mergePackageItems(pkg1TypeDefinitions, pkg2TypeDefinitions));

        // merge constant definitions
        const pkg1ConstantDefinitions = package1.getConstantDefinitions();
        const pkg2ConstantDefinitions = package2.getConstantDefinitions();
        package2.setConstantDefinitions(this.mergePackageItems(pkg1ConstantDefinitions, pkg2ConstantDefinitions));

        // merge annotation definitions
        const pkg1AnnotationDefinitions = package1.getAnnotationDefinitions();
        const pkg2AnnotationDefinitions = package2.getAnnotationDefinitions();
        package2.setAnnotationDefinitions(this.mergePackageItems(pkg1AnnotationDefinitions, pkg2AnnotationDefinitions));

        const pkg1ObjectDefinitions = package1.getObjects();
        const pkg2ObjectDefinitions = package2.getObjects();
        package2.setObjects(this.mergePackageItems(pkg1ObjectDefinitions, pkg2ObjectDefinitions));

        const pkg1EndpointDefintions = package1.getEndpoints();
        const pkg2EndpointDefintions = package2.getEndpoints();
        package2.setEndpoints(this.mergePackageItems(pkg1EndpointDefintions, pkg2EndpointDefintions));

        return package2;
    }

    /**
     * merge given item arrays. If there are items with same name, items in itemArray2 gets the priority
     * @param {Array} itemArray1
     * @param {Array} itemArray2
     */
    mergePackageItems(itemArray1, itemArray2) {
        _.remove(itemArray1, (item1) => {
            const duplicate = itemArray2.filter(item2 => item2.getName() == item1.getName());
            return duplicate.length > 0;
        });
        return itemArray1.concat(itemArray2);
    }

    /**
     * Get annotation attachment types.
     * @return {[string]} annotationAttachmentTypes
     * */
    getAnnotationAttachmentTypes() {
        return this._annotationAttachmentTypes;
    }

    /**
     * Get type lattice.
     * @returns {TypeLattice} type lattice
     * @memberof PackageScopedEnvironment
     */
    getTypeLattice() {
        return this._typeLattice;
    }
}

export default PackageScopedEnvironment;

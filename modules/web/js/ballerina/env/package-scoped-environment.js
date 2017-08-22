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
import TypeLattice from './../../type-lattice/type-lattice';
import SymbolTableGenVisitor from './../visitors/symbol-table/ballerina-ast-root-visitor';

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
        this._currentPackage = new Package({ name: 'Current Package' });
        this._packages.push(this._currentPackage);
        this._annotationAttachmentTypes = Environment.getAnnotationAttachmentTypes();
        this._typeLattice = Environment.getTypeLattice();
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
        const structs = this.getCurrentPackage().getStructDefinitions().map(struct => struct.getName());
        return _.union(this._types, structs);
    }

    /**
     * Create current package from the the given AST
     * @param {BallerinaASTRoot} astRoot 
     */
    createCurrentPackageFromAST(astRoot) {
        // get the latest symbols from this file.
        let currentPackage = new Package();
        currentPackage.setName('Current Package');
        const symbolTableGenVisitor = new SymbolTableGenVisitor(currentPackage, astRoot);
        astRoot.accept(symbolTableGenVisitor);
        currentPackage = symbolTableGenVisitor.getPackage();

        // check if a similar package exists.
        const packages = this.getPackages();
        const currentPackageArray = _.filter(packages, pkg => !_.isEmpty(astRoot.children) && (pkg.getName() ===
            astRoot.children[0].getPackageName()));
        // Check whether the program contains a package name or it is in the dafault package
        if (!_.isEmpty(currentPackageArray)) {
            // Update Current package object after the package resolving
            const currentPackageInEvn = _.clone(currentPackageArray[0]);
            // todo merge the package with this.
            currentPackage = this.mergePackages(currentPackageInEvn, currentPackage);
        }
        // update the package scoped environment with current package
        this.setCurrentPackage(currentPackage);

        return currentPackage;
    }

    /**
     * Merge package1 into package2 and returns merged package2
     * @param {Package} package1 
     * @param {Package} package2 
     * @returns {Package} merged package
     */
    mergePackages(package1, package2){
        // merge function definitions
        let pkg1FunctionDefinitions = package1.getFunctionDefinitions();
        let pkg2FunctionDefinitions = package2.getFunctionDefinitions();
        package2.setFunctionDefinitions(this.mergePackageItems(pkg1FunctionDefinitions, pkg2FunctionDefinitions));

        // merge struct definitions
        let pkg1StructDefinitions = package1.getStructDefinitions();
        let pkg2StructDefinitions = package2.getStructDefinitions();
        package2.setStructDefinitions(this.mergePackageItems(pkg1StructDefinitions, pkg2StructDefinitions));

        // merge connector definitions
        let pkg1Connectors = package1.getConnectors();
        let pkg2Connectors = package2.getConnectors();
        package2.setConnectors(this.mergePackageItems(pkg1Connectors, pkg2Connectors));

        // merge type definitions
        let pkg1TypeDefinitions = package1.getTypeDefinitions();
        let pkg2TypeDefinitions = package2.getTypeDefinitions();
        package2.setTypeDefinitions(this.mergePackageItems(pkg1TypeDefinitions, pkg2TypeDefinitions));

        // merge constant definitions
        let pkg1ConstantDefinitions = package1.getConstantDefinitions();
        let pkg2ConstantDefinitions = package2.getConstantDefinitions();
        package2.setConstantDefinitions(this.mergePackageItems(pkg1ConstantDefinitions, pkg2ConstantDefinitions));

        // merge annotation definitions
        let pkg1AnnotationDefinitions = package1.getAnnotationDefinitions();
        let pkg2AnnotationDefinitions = package2.getAnnotationDefinitions();
        package2.setAnnotationDefinitions(this.mergePackageItems(pkg1AnnotationDefinitions, pkg2AnnotationDefinitions));

        return package2;
    }

    /**
     * merge given item arrays. If there are items with same name, items in itemArray2 gets the priority 
     * @param {Array} itemArray1 
     * @param {Array} itemArray2 
     */
    mergePackageItems(itemArray1, itemArray2){
        _.remove(itemArray1, function(item1){
            let duplicate = itemArray2.filter((item2) => item2.getName() == item1.getName());
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

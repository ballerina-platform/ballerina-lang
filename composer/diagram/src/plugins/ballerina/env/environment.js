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
import log from 'log';
import _ from 'lodash';
import EventChannel from 'event_channel';
import { getTypeLattice, getOperatorLattice, getPackages, getBuiltInTypes } from 'api-client/api-client';
import TypeLattice from 'plugins/ballerina/type-lattice/type-lattice';
import OperatorLattice from 'plugins/ballerina/type-lattice/operator-lattice';
import BallerinaEnvFactory from './ballerina-env-factory';


/**
 * @class BallerinaEnvironment
 * @augments EventChannel
 * @param args {Object} - args.package {[PackageDefinition]} list of packages
 * @constructor
 */
class BallerinaEnvironment extends EventChannel {
    constructor(args) {
        super();
        this.initialized = false;
        this.initPending = false;
        this._packages = _.get(args, 'packages', []);
        this._typeLattice = _.get(args, 'typeLattice', TypeLattice);
        this._operatorLattice = _.get(args, 'operatorLattice', OperatorLattice);
        this._types = _.get(args, 'types', []);
        this._defaultValues = _.get(args, 'defaultValues', []);
        this._annotationAttachmentTypes = _.get(args, 'annotationAttachmentTypes', []);
    }

    initialize() {
        return new Promise((resolve, reject) => {
            // avoid doing init multiple times.
            // if one is pending already, just subscribe to
            // finish event once
            if (this.initPending) {
                this.once('initialized', () => {
                    resolve();
                });
            }
            if (!this.initialized) {
                this.initPending = true;
                this.initializeAnnotationAttachmentPoints();
                Promise.all([
                    this.initializeBuiltinTypes(),
                    this.initializePackages(),
                    this.initializeTypeLattice(),
                    this.initializeOperatorLattice(),
                ]).then(() => {
                    this.initialized = true;
                    this.initPending = false;
                    this.trigger('initialized');
                    resolve();
                }).catch(reject);
            } else {
                resolve();
            }
        });
    }

    /**
     * Given a name, finds relevant package
     * @param packageName {String} name of the package to find
     * @return {Package}
     */
    findPackage(packageName) {
        return _.find(this._packages, packageInstance => _.isEqual(packageInstance.getName(), packageName));
    }

    /**
     * Add a package to env
     * @param packageInstance {Package}
     * @fires BallerinaEnvironment#new-package-added
     */
    addPackage(packageInstance) {
        if (!(BallerinaEnvFactory.isPackage(packageInstance))) {
            const err = `${packageInstance} is not an instance of Package.`;
            log.error(err);
            throw err;
        }
        this._packages.push(packageInstance);
        /**
         * @Event BallerinaEnvironment#new-package-added
         */
        this.trigger('new-package-added', packageInstance);
    }

    /**
     * @return {[Package]}
     */
    getPackages() {
        return this._packages;
    }

    /**
     * get available types for this environment
     * @returns {*}
     */
    getTypes() {
        return this._types;
    }

    /**
     * Get type lattice
     * @returns {TypeLattice} type lattice
     * @memberof BallerinaEnvironment
     */
    getTypeLattice() {
        return this._typeLattice;
    }

    /**
     * Get operator lattice
     * @returns {OperatorLattice} operator lattice
     * @memberof BallerinaEnvironment
     */
    getOperatorLattice() {
        return this._operatorLattice;
    }

    /**
     * Get annotation attachment types.
     * @return {[string]} annotationAttachmentTypes
     * */
    getAnnotationAttachmentTypes() {
        return this._annotationAttachmentTypes;
    }

    /**
     * Initialize packages from BALLERINA_HOME and/or Ballerina Repo
     */
    initializePackages() {
        return getPackages()
            .then((data) => {
                if (data.error && !data.result) {
                    throw new Error(data.error);
                }
                if (_.isArray(data.packages)) {
                    data.packages.forEach((packageNode) => {
                        const pckg = BallerinaEnvFactory.createPackage();
                        pckg.initFromJson(packageNode);
                        this._packages.push(pckg);
                    });
                } else {
                    throw new Error('Error while fetching packages');
                }
            });
    }

    /**
     * Initialize type lattice
     */
    initializeTypeLattice() {
        getTypeLattice().then((typeLatticeJson) => {
            if (typeLatticeJson) {
                this._typeLattice.initFromJson(typeLatticeJson);
            } else {
                log.error('Error while fetching type lattice');
            }
        });
    }

    /**
     * Initialize operator lattice
     */
    initializeOperatorLattice() {
        return getOperatorLattice().then((operatorLatticeJson) => {
            if (operatorLatticeJson) {
                this._operatorLattice.initFromJson(operatorLatticeJson);
            } else {
                log.error('Error while fetching operator lattice');
            }
        });
    }

    /**
     * Initialize builtin types from Ballerina Program
     */
    initializeBuiltinTypes() {
        return getBuiltInTypes()
            .then((data) => {
                if (data.error && !data.result) {
                    throw new Error(data.error);
                }
                if (_.isArray(data.types)) {
                    _.each(data.types, (builtinType) => {
                        if (!_.isNil(builtinType)) {
                            this._types.push(builtinType.name);
                            if (_.isNil(builtinType.defaultValue)) {
                                this._defaultValues[builtinType.name] = 'null';
                            } else if (builtinType.name === 'string') {
                                this._defaultValues[builtinType.name] = '"' + builtinType.defaultValue + '"';
                            } else {
                                this._defaultValues[builtinType.name] = builtinType.defaultValue;
                            }
                        }
                    });
                    this._types = _.sortBy(this._types, [function (type) {
                        return type;
                    }]);
                } else {
                    throw new Error('Error while fetching built in types');
                }
            });
    }

    /**
     * Get default value of given type
     * @param {any} type type
     * @returns default value
     * @memberof BallerinaEnvironment
     */
    getDefaultValue(type) {
        return this._defaultValues[type];
    }

    /**
     * Initialize annotation attachment points for Ballerina Program
     * */
    initializeAnnotationAttachmentPoints() {
        this._annotationAttachmentTypes = _.sortBy(['service', 'resource', 'connector', 'action', 'function',
            'typemapper', 'struct', 'const', 'parameter', 'annotation'], [function (type) {
                return type;
            }]);
    }

    searchPackage(query, excludePackages) {
        return _.filter(this._packages, (pckg) => {
            const existing = _.filter(excludePackages, ex => pckg.getName() === ex);
            return (existing.length === 0) && new RegExp(query.toUpperCase()).test(pckg.getName().toUpperCase());
        });
    }
}

export default new BallerinaEnvironment();

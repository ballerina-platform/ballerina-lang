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
import BallerinaEnvFactory from './ballerina-env-factory';
import { getLangServerClientInstance } from './../../langserver/lang-server-client-controller';
import { getBuiltInPackages, getTypeLattice } from './../../api-client/api-client';
import TypeLattice from './../../type-lattice/type-lattice';


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
        this._types = _.get(args, 'types', []);
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

                getLangServerClientInstance()
                    .then((langServerClient) => {
                        langServerClient.workspaceSymbolRequest('builtinTypes', (data) => {
                            this.initializeBuiltinTypes(data.result);
                            Promise.all([
                                this.initializePackages(),
                                this.initializeTypeLattice(),
                            ]).then(() => {
                                this.initialized = true;
                                this.initPending = false;
                                this.trigger('initialized');
                                resolve();
                            }).catch(reject);
                        });
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
        return new Promise((resolve, reject) => {
            getBuiltInPackages()
                .then((packagesJson) => {
                    if (_.isArray(packagesJson)) {
                        packagesJson.forEach((packageNode) => {
                            const pckg = BallerinaEnvFactory.createPackage();
                            pckg.initFromJson(packageNode);
                            this._packages.push(pckg);
                        });
                        resolve();
                    } else {
                        log.error('Error while fetching packages');
                        resolve();
                    }
                })
                .catch(reject);
        });
    }

    /**
     * Initialize type lattice
     */
    initializeTypeLattice() {
        return new Promise((resolve, reject) => {
            getTypeLattice()
                .then((typeLatticeJson) => {
                    if (typeLatticeJson) {
                        this._typeLattice.initFromJson(typeLatticeJson);
                        resolve();
                    } else {
                        log.error('Error while fetching type lattice');
                        resolve();
                    }
                })
                .catch(reject);
        });
    }

    /**
     * Initialize builtin types from Ballerina Program
     */
    initializeBuiltinTypes(builtinTypes) {
        _.each(builtinTypes, (builtinType) => {
            if (!_.isNil(builtinType)) {
                this._types.push(builtinType.name);
            }
        });
        this._types = _.sortBy(this._types, [function (type) {
            return type;
        }]);
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

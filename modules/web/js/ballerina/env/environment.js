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
import EventChannel from 'event_channel';
import BallerinaEnvFactory from './ballerina-env-factory';
import EnvironmentContent from 'environment_content';

let instance;

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
        this._packages = _.get(args, 'packages', []);
        this._types = _.get(args, 'types', []);
        this._annotationAttachmentTypes = _.get(args, 'annotationAttachmentTypes', []);
    }

    initialize(opts) {
        if (!this.initialized) {
            opts.app.langseverClientController.workspaceSymbolRequest('builtinTypes', (data) => {
                this.initializeBuiltinTypes(data.result);
            });
            this.initializePackages(opts.app);
            this.initializeAnnotationAttachmentPoints(opts.app);
            this.initialized = true;
        }
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
        if (!(packageInstance instanceof Package)) {
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
     * Get annotation attachment types.
     * @return {[string]} annotationAttachmentTypes
     * */
    getAnnotationAttachmentTypes() {
        return this._annotationAttachmentTypes;
    }

    /**
     * Initialize packages from BALLERINA_HOME and/or Ballerina Repo
     */
    initializePackages(app) {
        const self = this;
        const packagesJson = EnvironmentContent.getPackages(app);

        _.each(packagesJson, (packageNode) => {
            const pckg = BallerinaEnvFactory.createPackage();
            pckg.initFromJson(packageNode);
            self._packages.push(pckg);
        });
    }

    /**
     * Initialize builtin types from Ballerina Program
     */
    initializeBuiltinTypes(builtinTypes) {
        const self = this;
        _.each(builtinTypes, (builtinType) => {
            if (!_.isNil(builtinType)) {
                self._types.push(builtinType.name);
            }
        });
        self._types = _.sortBy(self._types, [function (type) {
            return type;
        }]);
    }

    /**
     * Initialize annotation attachment points for Ballerina Program
     * */
    initializeAnnotationAttachmentPoints(app) {
        const self = this;
        self._annotationAttachmentTypes = _.sortBy(['service', 'resource', 'connector', 'action', 'function',
            'typemapper', 'struct', 'const', 'parameter', 'annotation'], [function (type) {
                return type;
            }]);
    }

    searchPackage(query, exclude) {
        const search_text = query;
        const exclude_packages = exclude;
        return _.filter(this._packages, (pckg) => {
            const existing = _.filter(exclude_packages, ex => pckg.getName() === ex);
            return (existing.length === 0) && new RegExp(query.toUpperCase()).test(pckg.getName().toUpperCase());
        });
    }
}

export default new BallerinaEnvironment();

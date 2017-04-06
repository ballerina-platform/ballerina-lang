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

var instance;

/**
 * @class BallerinaEnvironment
 * @augments EventChannel
 * @param args {Object} - args.package {[PackageDefinition]} list of packages
 * @constructor
 */
class BallerinaEnvironment extends EventChannel {
    constructor(args) {
        super();
        this._packages = _.get(args, 'packages', []);
        this._types = _.get(args, 'types', []);
        this.initializeNativeTypes();
        this.initializePackages();
    }

    /**
     * Given a name, finds relevant package
     * @param packageName {String} name of the package to find
     * @return {Package}
     */
    findPackage(packageName) {
        return _.find(this._packages, function (packageInstance) {
            return _.isEqual(packageInstance.getName(), packageName);
        });
    }

    /**
     * Add a package to env
     * @param packageInstance {Package}
     * @fires BallerinaEnvironment#new-package-added
     */
    addPackage(packageInstance) {
        if (!(packageInstance instanceof Package)) {
            var err = packageInstance + " is not an instance of Package.";
            log.error(err);
            throw err;
        }
        this._packages.push(packageInstance);
        /**
         * @Event BallerinaEnvironment#new-package-added
         */
        this.trigger("new-package-added", packageInstance);
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
     * Initialize packages from BALLERINA_HOME and/or Ballerina Repo
     */
    initializePackages() {

        var self = this;
        var packagesJson = EnvironmentContent.getPackages();

        _.each(packagesJson, function (packageNode) {
            var pckg = BallerinaEnvFactory.createPackage();
            pckg.initFromJson(packageNode);
            self._packages.push(pckg);
        });
    }

    /**
     * Initialize native types from Ballerina Program
     */
    initializeNativeTypes() {
        var self = this;
        var nativeTypesJson = EnvironmentContent.getNativeTypes();
        _.each(nativeTypesJson, function (nativeType) {
            if (!_.isNil(nativeType)) {
                self._types.push(nativeType);
            }
        });
    }

    searchPackage(query, exclude) {
        var search_text = query;
        var exclude_packages = exclude;
        var result = _.filter(this._packages, function (pckg) {
            var existing = _.filter(exclude_packages, function (ex) {
                return pckg.getName() == ex;
            });
            return (existing.length == 0) && new RegExp(query.toUpperCase()).test(pckg.getName().toUpperCase());
        });
        return result;
    }
}

export default new BallerinaEnvironment();

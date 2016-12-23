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
define(['lodash', 'event_channel', './package'], function(_, EventChannel, Package){

    /**
     * @class BallerinaEnvironment
     * @augments EventChannel
     * @param args {Object} - args.package {[PackageDefinition]} list of packages
     * @constructor
     */
    var BallerinaEnvironment = function(args) {
        this._packages = _.get(args, 'packages', []);
    };

    BallerinaEnvironment.prototype = Object.create(EventChannel.prototype);
    BallerinaEnvironment.prototype.constructor = BallerinaEnvironment;

    /**
     * Given a name, finds relevant package
     * @param packageName {String} name of the package to find
     * @return {Package}
     */
    BallerinaEnvironment.prototype.findPackage = function (packageName) {
        return _.find(this._packages, function(packageInstance){
            return _.isEqual(packageInstance.getName(), packageName);
        });
    };

    /**
     * Add a package to env
     * @param packageInstance {Package}
     * @fires BallerinaEnvironment#new-package-added
     */
    BallerinaEnvironment.prototype.addPackage = function(packageInstance) {
       if(!(packageInstance instanceof Package)){
            var err = packageInstance + " is not an instance of Package.";
            log.error(err);
            throw err;
       }
       this._packages.push(packageInstance);
        /**
         * @Event BallerinaEnvironment#new-package-added
         */
       this.trigger("new-package-added", packageInstance);
    };

    /**
     * @return {[Package]}
     */
    BallerinaEnvironment.prototype.getPackages = function() {
        return this._packages;
    };


    return BallerinaEnvironment;
});
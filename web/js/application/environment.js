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
define(['lodash', 'log', './packageScope', './ballerinaSDK'], function (_, log, PackageScope, BallerinaSDK) {
    /**
     * Represents Environment
     * @param args
     * @constructor
     */
    var Environment = function (args) {
        if (!_.has(args, 'packageScope')) {
            this._packageScope = new PackageScope();
        } else {
            this._packageScope = _.get(args, 'packageScope');
        }
        if (!_.has(args, 'ballerinaSDK')) {
            this._ballerinaSDK = new BallerinaSDK();
        } else {
            this._ballerinaSDK = _.get(args, 'ballerinaSDK');
        }
    };

    Environment.prototype.init = function () {
        this._structs = _.union(this.getBallerinaSDK().getStructs(), this.getPackageScope().getStructs());
    };

    Environment.prototype.setStructs = function (structs) {
        this._structs = structs;
    };

    Environment.prototype.getStructs = function () {
        return this._structs;
    };

    Environment.prototype.setBallerinaSDK = function (ballerinaSDK) {
        this._ballerinaSDK = ballerinaSDK;
    };

    Environment.prototype.getBallerinaSDK = function () {
        return this._ballerinaSDK;
    };

    Environment.prototype.setPackageScope = function (packageScope) {
        this._packageScope = packageScope;
    };

    Environment.prototype.getPackageScope = function () {
        return this._packageScope;
    };

    return Environment;

});
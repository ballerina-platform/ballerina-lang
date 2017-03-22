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
import log from 'log';
import PackageScope from './packageScope';
import BallerinaSDK from './ballerinaSDK';

/**
 * Represents Environment
 * @param args
 * @constructor
 */
class Environment {
    constructor(args) {
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
    }

    init() {
        this._structs = _.union(this.getBallerinaSDK().getStructs(), this.getPackageScope().getStructs());
    }

    setStructs(structs) {
        this._structs = structs;
    }

    getStructs() {
        return this._structs;
    }

    setBallerinaSDK(ballerinaSDK) {
        this._ballerinaSDK = ballerinaSDK;
    }

    getBallerinaSDK() {
        return this._ballerinaSDK;
    }

    setPackageScope(packageScope) {
        this._packageScope = packageScope;
    }

    getPackageScope() {
        return this._packageScope;
    }
}

export default Environment;


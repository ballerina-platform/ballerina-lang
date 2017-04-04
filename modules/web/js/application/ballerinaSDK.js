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

/**
 * Represents the constructs elements from BallerinaSDK
 * @param {Object} args - Construct elements from BallerinaSDK
 * @param {Object} args.functions - functions
 * @param {Object} args.connectorDefinitions - connectorDefinitions;
 * @param {Object} args.structs - structs;
 * @param {Object} args.nativetype - nativetype;
 * @param {Object} args.type - type;
 * @constructor
 */
class BallerinaSDK {
    constructor(args) {
        this._functions = _.get(args, 'functions', []);
        this._connectorDefinitions = _.get(args, 'connectorDefinitions', {});
        this._structs = _.get(args, 'structs', []);
        this._nativetype = _.get(args, 'nativeTypes', []);
        this._type = _.get(args, 'types', []);
    }

    init() {
        this._type = _.union(this.getNativeTypes(), this.getStructs());
    }

    setStructs(structs) {
        this._structs = structs;
    }

    getStructs() {
        return this._structs;
    }

    setTypes(type) {
        this._type = type;
    }

    getTypes() {
        return this._type;
    }

    setNativeTypes(nativeType) {
        this._nativeType = nativeType;
    }

    getNativeTypes() {
        return this._nativeType;
    }
}

export default BallerinaSDK;


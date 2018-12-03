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
import EventChannel from 'event_channel';

/**
 * Represents a single field inside an annotation definition({@link AnnotationDefinition}) for ballerina-env.
 *
 * @class AnnotationAttributeDefinition
 * @extends {EventChannel}
 */
class AnnotationAttributeDefinition extends EventChannel {
    /**
     * Creates an instance of AnnotationAttributeDefinition.
     * @param {Object} args Object to initialize.
     * @param {string} args.bType The ballerina type of the field definition. Example: "string" of "string foo".
     * @param {boolean} args.arrayType Whether the attribute is an arrayed typed.
     * @param {string} args.packagePath The package to which the field belongs to.
     * @param {string} args.identifier The identifier of the field. Example: "foo" of "string foo".
     *
     * @memberof AnnotationAttributeDefinition
     */
    constructor(args) {
        super(args);
        this._bType = _.get(args, 'bType');
        this._arrayType = _.get(args, 'arrayTyped');
        this._packagePath = _.get(args, 'packagePath');
        this._identifier = _.get(args, 'identifier');
    }

    setBType(bType) {
        this._bType = bType;
    }

    getBType() {
        return this._bType;
    }

    setArrayType(arrayType) {
        this._arrayType = arrayType;
    }

    isArrayType() {
        return this._bType.includes('[]');
    }

    setPackagePath(packagePath) {
        this._packagePath = packagePath;
    }

    getPackagePath() {
        return this._packagePath;
    }

    setIdentifier(identifier) {
        this._identifier = identifier;
    }

    getIdentifier() {
        return this._identifier;
    }

    /**
     * Sets values from a json object
     *
     * @param {Object} jsonNode json object with values.
     * @param {string} jsonNode.bType The ballerina type of the attribute/field.
     * @param {boolean} jsonNode.arrayType Whether the attribute is an arrayed type.
     * @param {string} jsonNode.identifier The identifier of the attribute/field.
     * @param {string} jsonNode.packagePath the package path of the attribute/field.
     *
     * @memberof AnnotationAttributeDefinition
     */
    initFromJson(jsonNode) {
        this.setBType(jsonNode.bType);
        this.setArrayType(jsonNode.arrayType);
        this.setIdentifier(jsonNode.identifier);
        this.setPackagePath(jsonNode.packagePath);
    }
}

export default AnnotationAttributeDefinition;

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
 * Represents an annotation attachment i.e. @doc of an annotation definition({@link Annotation Attachment}) for ballerina-env.
 *
 * @class AnnotationAttachment
 * @extends {EventChannel}
 */
class AnnotationAttachment extends EventChannel {
    /**
     * Creates an instance of AnnotationAttachment.
     * @param {Object} args Object to initialize.
     * @param {string} args.attributeNameValPairs.key The identifier of the attribute definition.
     * @param {string} args.attributeNameValPairs.value The description of the field.
     *
     * @memberof AnnotationAttributeDefinition
     */
    constructor(args) {
        super(args);
        this._key = _.get(args, 'attributeNameValPairs.key');
        this._value = _.get(args, 'attributeNameValPairs.value');
    }

    setKey(key) {
        this._key = key;
    }

    getKey() {
        return this._key;
    }

    setValue(value) {
        this._value = value;
    }

    getValue() {
        return this._value;
    }

    /**
     * Sets values from a json object
     *
     * @param {Object} jsonNode json object with values.
     * @param {string} jsonNode.attributeNameValPairs.key The identifier of the attribute definition.
     * @param {string} jsonNode.attributeNameValPairs.value The description of the field.
     * @memberof AnnotationAttachment
     */
    initFromJson(jsonNode) {
        this.setKey(jsonNode.attributeNameValPairs.key);
        this.setValue(jsonNode.attributeNameValPairs.value);
    }
}

export default AnnotationAttachment;

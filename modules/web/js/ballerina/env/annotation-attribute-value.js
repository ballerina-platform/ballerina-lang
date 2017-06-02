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
 * A class representing the value of an annotation's attribute name value pair in ballerina-env.
 * 
 * @class AnnotationAttributeValue
 * @extends {EventChannel}
 */
class AnnotationAttributeValue extends EventChannel {
    /**
     * Creates an instance of AnnotationAttributeValue.
     * Only one of the properties can have a value of the bValue, annotationValue, valueArray. 
     * 
     * @param {Object} args Object for creating an annotation attribute value
     * @param {string} args.bValue The ballerina value of the attribute value.
     * @param {AnnotationAttachment} args.annotationValue The annotation value of the attribute value.
     * @param {AnnotationAttributeValue[]} args.valueArray The array of the attribute value.
     * 
     * @memberof AnnotationAttributeValue
     */
    constructor(args) {
        super(args);
        this._bValue = _.get(args, 'bValue');
        this._annotationValue = _.get(args, 'annotationValue');
        this._valueArray = _.get(args, 'valueArray');
    }

    setBValue(bValue) {
        this._bValue = bValue;
        this._annotationValue = undefined;
        this._valueArray = undefined;
    }

    getBValue() {
        return this._bValue;
    }

    isBValue() {
        return this._bValue === undefined;
    }

    /**
     * Sets a value for annotationValue
     * 
     * @param {AnnotationAttachment} annotation 
     * 
     * @memberof AnnotationAttributeValue
     */
    setAnnotationValue(annotation) {
        this._bValue = undefined;
        this._annotationValue = annotation;
        this._valueArray = undefined;
    }

    getAnnotationValue() {
        return this._annotationValue;
    }

    isAnnotationValue() {
        return this._annotationValue === undefined;
    }

    /**
     * Sets the value for value array
     * 
     * @param {AnnotationAttributeValue[]} valueArray
     * 
     * @memberof AnnotationAttributeValue
     */
    setValueArray(valueArray) {
        this._bValue = undefined;
        this._annotationValue = undefined;
        this._valueArray = valueArray;
    }

    getValueArray() {
        return this._valueArray;
    }

    isValueArray() {
        return this._valueArray === undefined;
    }

    /**
    * sets values from a json object
    * @param {Object} jsonNode
    */
    initFromJson(jsonNode) {
        var self = this;
        this.setName(jsonNode.name);

        _.each(jsonNode.attachmentPoints, function (attachmentPoint) {
            self.addAttachmentPoints(attachmentPoint);
        });

    }

}

export default AnnotationAttributeValue;
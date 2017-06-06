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
 * A class that represents an annotation attachment in ballerina-env.
 * 
 * @class AnnotationAttachment
 * @extends {EventChannel}
 */
class AnnotationAttachment extends EventChannel {
    /**
     * Creates an instance of Annotation.
     * @param {Object} args Object for creating an annotation in ballerina-env.
     * @param {string} args.packageName The name of the package of the annotation. This is the suffix. Example: http
     * @param {string} args.packagePath The full package name of the annotation. Example: ballerina.net.http
     * @param {string} args.name The name or the identifier of the annotation. Example: GET
     * @param {string} args.attachmentPoint The place which the annotation can be attached to. Example: "service", "resource".
     * 
     * @memberof AnnotationAttachment
     */
    constructor(args) {
        super(args);
        this._packageName = _.get(args, 'packageName');
        this._packagePath = _.get(args, 'packagePath');
        this._name = _.get(args, 'name');
        this._attachmentPoint = _.get(args, 'attachmentPoint');

        /**
         * Stores the attribute pair values of the annotation. new Map<string, AnnotationAttributeValue>().
         * @type {Map}
         */
        this._attributeNameValPairs = new Map();
    }

    setPackageName(packageName) {
        this._packageName = packageName;
    }

    getPackageName() {
        return this._packageName;
    }

    setPackagePath(packagePath) {
        this._packagePath = packagePath; 
    }

    getPackagePath() {
        return this._packagePath;
    }

    /**
    * sets the name
    * @param {string} name
    */
    setName(name) {
        var oldName = this._name;
        this._name = name;
        this.trigger('name-modified', name, oldName);
    }

    /**
     * returns the name
     * @returns {string}
     */
    getName() {
        return this._name;
    }

    setAttachmentPoint(attachmentPoint) {
        this._attachmentPoint = attachmentPoint;
    }
    
    getAttachmentPoint() {
        return this._attachmentPoint;
    }

    setAttributeNameValPairs(attributeNameValPairs) {
        this._attributeNameValPairs = attributeNameValPairs;
    }

    getAttributeNameValPairs() {
        return this._attributeNameValPairs;
    }

    addAttributeNameValPair(key, annotationAttributeValue) {
        this._attributeNameValPairs.set(key, annotationAttributeValue);
    }

    getAnnotationAttributeValue(key) {
        return this._attributeNameValPairs.get(key);
    }

    removeAttributeNameValPair(key) {
        this._attributeNameValPairs.delete(key);
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

export default AnnotationAttachment;
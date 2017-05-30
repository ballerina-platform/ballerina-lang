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
import ASTNode from './node';
import log from 'log';
import CommonUtils from '../utils/common-utils';

/**
 * Annotation Definition for defining an annotation
 * */
class AnnotationDefinition extends ASTNode {
    constructor(args) {
        super('AnnotationDefinition');
        this._annotationName = _.get(args, 'annotationName');
        this._attachmentPoints = _.get(args, 'attachmentPoints', []);
    }

    setAnnotationName(annotationName, options) {
        if (!_.isNil(annotationName) && ASTNode.isValidIdentifier(annotationName)) {
            this.setAttribute('_annotationName', annotationName, options);
        } else {
            let error = 'Invalid name for the annotation name: ' + annotationName;
            log.error(error);
            throw  error;
        }
    }

    addAttachDefinition(definition) {
        this._attachedDefinitions.push(definition);
    }

    getAnnotationName() {
        return this._annotationName;
    }

    getAttachmentPoints() {
        return this._attachmentPoints;
    }

    setAttachmentPoints(attachmentPoints, options) {
        this.setAttribute('_attachmentPoints', attachmentPoints, options);
    }

    getAnnotationAttributeDefinitions() {
        return this.filterChildren(this.getFactory().isAnnotationAttributeDefinition);
    }

    addAnnotationAttributeDefinition(type, identifier, defaultValue) {
        // if identifier is empty
        if (_.isEmpty(identifier)) {
            let errorString = 'Identifier cannot be empty';
            log.error(errorString);
            throw errorString;
        }

        // Check if already variable definition exists with same identifier.
        let identifierAlreadyExists = _.findIndex(this.getAnnotationAttributeDefinitions(), function (attDef) {
            return attDef.getAttributeName() === identifier;
        }) !== -1;

        // If annotation attribute definition with the same identifier exists, then throw an error,
        // else create the new annotation attribute definition.
        if (identifierAlreadyExists) {
            let errorString = 'An attribute with identifier \'' + identifier + '\' already exists.';
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new annotation attribute definition.
            let newAnnotationAttributeDefinition = this.getFactory().createAnnotationAttributeDefinition();
            newAnnotationAttributeDefinition.setAttributeName(identifier);
            newAnnotationAttributeDefinition.setAttributeType(type);
            newAnnotationAttributeDefinition.setAttributeValue(defaultValue);

            // Get the index of the last definition.
            let index = this.findLastIndexOfChild(this.getFactory().isVariableDefinitionStatement);

            this.addChild(newAnnotationAttributeDefinition, index + 1);
        }
    }

    /**
     * Add Annotation attachments to the model.
     * @param {string} identifier - attachment name.
     * */
    addAnnotationAttachmentPoint(identifier) {
        if (_.isEmpty(identifier)) {
            let errorString = 'Identifier cannot be empty';
            log.error(errorString);
            throw errorString;
        }

        let identifierAlreadyExists = _.findIndex(this.getAttachmentPoints(), function (attachmentPoint) {
            return _.isEqual(identifier, attachmentPoint);
        }) !== -1;

        if (identifierAlreadyExists) {
            let errorString = 'An attribute with identifier "' + identifier + '" already exists.';
            log.error(errorString);
            throw errorString;
        } else {
            this._attachmentPoints.push(identifier);
            // Trigger model change.
            this.setAttachmentPoints(this._attachmentPoints);
        }
    }

    /**
     * Remove annotation attachment points.
     * @param {string} identifier - identifier for the attachment point.
     * */
    removeAnnotationAttachmentPoints(identifier) {
        _.pull(this._attachmentPoints, identifier);
        // Trigger model change.
        this.setAttachmentPoints(this._attachmentPoints);
    }

    /**
     * Removes annotation attribute definition.
     * @param {string} modelID - The model ID of the variable.
     */
    removeAnnotationAttributeDefinition(modelID) {
        this.removeChildById(modelID);
    }

    /**
     * @inheritDoc
     * @override
     * */
    initFromJson(jsonNode) {
        let self = this;
        this.setAnnotationName(jsonNode.annotation_name, {doSilently: true});
        if (!_.isNil(jsonNode.annotation_attachment_points)) {
            this.setAttachmentPoints(_.split(jsonNode.annotation_attachment_points, ','), {doSilently: true});
        }
        _.each(jsonNode.children, function (childNode) {
            let child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * @inheritDoc
     * @override
     * */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'Annotation',
                setter: this.setAnnotationName,
                getter: this.getAnnotationName,
                parents: [{
                    node: this.parent,
                    getChildrenFunc: this.parent.getAnnotationDefinitions,
                    getter: this.getAnnotationName
                }]
            }]
        });
    }
}

export default AnnotationDefinition;
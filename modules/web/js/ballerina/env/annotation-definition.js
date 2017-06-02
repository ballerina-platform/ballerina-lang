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
import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * Represents an annotation definition in the ballerina-env.
 * 
 * @class AnnotationDefinition
 * @extends {EventChannel}
 */
class AnnotationDefinition extends EventChannel {
    /**
     * Creates an instance of AnnotationDefinition.
     * @param {Object} args Object to initialize
     * @param {string} args.packagePath The package name of the annotation definition.
     * @param {string} args.name The name of the annotation definition.
     * @param {string[]} args.attachmentPoints The points which the annotation definition can be attached as an 
     * annotation attachment.
     * @param {AnnotationAttributeDefinition[]} The fields of the annotation definition.
     * 
     * @memberof AnnotationDefinition
     */
    constructor(args) {
        super(args);
        this._packagePath = _.get(args, 'packagePath');
        this._name = _.get(args, 'name');
        this._attachmentPoints = _.get(args, 'attachmentPoints', []);
        this._annotationAttributeDefinitions = _.get(args, 'annotationAttributeDefinitions', []);
    }

    setPackagePath(packagePath) {
        this._packagePath = packagePath;
    }

    getPackagePath() {
        return this._packagePath;
    }

    setName(name) {
        this._name = name;
    }
    
    getName() {
        return this._name;
    }

    setAttachmentPoints(attachmentPoints) {
        this._attachmentPoints = attachmentPoints;
    }

    getAttachmentPoints() {
        return this._attachmentPoints;
    }

    addAttachmentPoint(attachmentPoint) {
        this._attachmentPoints.push(attachmentPoint);
    }

    removeAttachmentPoint(attachmentPoint) {
        _.pull(this._attachmentPoints, attachmentPoint);
    }

    setAnnotationAttributeDefinitions(annotationAttribtueDefinitions) {
        this._annotationAttributeDefinitions = annotationAttribtueDefinitions;
    }

    getAnnotationAttributeDefinitions() {
        return this._annotationAttributeDefinitions;
    }

    addAnnotationAttributeDefinition(annotationAttributeDefinition) {
        this._annotationAttributeDefinitions.push(annotationAttributeDefinition);
    }

    removeAnnotationAttributeDefinition(annotationAttributeDefinition) {
        let annotationAttributeDefinitionToBeRemoved = annotationAttributeDefinition;
        this._annotationAttributeDefinitions = 
            _.remove(this._annotationAttributeDefinitions, (annotationAttributeDefinition) => {
                return annotationAttributeDefinition.getIdentifier() === 
                                                            annotationAttributeDefinitionToBeRemoved.getIdentifier();
            });
    }

    /**
     * Sets values from a json object
     * 
     * @param {Object} jsonNode json object with values.
     * @param {string} jsonNode.packagePath The package of the annotation definition.
     * @param {string} jsonNode.name The name of the annotation definition.
     * @param {string[]} jsonNode.attachmentPoints The attachment points of the annotation definition.
     * @param {Object[]} jsonNode.annotationAttributeDefs The field attributes of the annotation definition.
     * 
     * @memberof AnnotationDefinition
     */
    initFromJson(jsonNode) {
        this.setPackagePath(jsonNode.packagePath);
        this.setName(jsonNode.name);
        this.setAttachmentPoints(jsonNode.attachmentPoints);

        // Setting the fields of the annotation definition.
        for (let annotationattributeDef of jsonNode.annotationAttributeDefs) {
            let annotationAttributeDefinition = BallerinaEnvFactory.createAnnotationAttributeDefinition();
            annotationAttributeDefinition.initFromJson(annotationattributeDef);
            this.addAnnotationAttributeDefinition(annotationAttributeDefinition);
        }
    }
}

export default AnnotationDefinition;
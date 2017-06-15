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
import Argument from './argument';

/**
 * AST node for a resource parameter.
 * @param {Object} args - Arguments for creating the resource parameter.
 * @param {string} args.type - Ballerina type of the parameter. Example: string, int.
 * @param {string} args.identifier - Identifier of the parameter.
 * @param {string} args.annotationType - The annotation of the parameter. Example: @PathParam.
 * @param {string} args.annotationText - The text within the annotation.
 * @constructor
 * @augments Argument
 */
class ResourceParameter extends Argument {
    constructor(args) {
        super(args);
        this.annotationType = _.get(args, 'annotationType');
        this.annotationText = _.get(args, 'annotationText');
        this.type = 'ResourceParameter';
    }

    setAnnotationType(annotationType, options) {
        this.setAttribute('annotationType', annotationType, options);
    }

    getAnnotationType() {
        return this.annotationType;
    }

    setAnnotationText(annotationText, options) {
        this.setAttribute('annotationText', annotationText, options);
    }

    getAnnotationText() {
        return this.annotationText;
    }

    /**
     * Gets a string representation of the current parameter.
     * @return {string} - String representation.
     */
    getParameterAsString() {
        let paramAsString = !_.isUndefined(this.getAnnotationType()) ? this.getAnnotationType() : '';
        paramAsString += !_.isUndefined(this.getAnnotationText()) && !_.isEmpty(this.getAnnotationText()) ?
        '{value:"' + this.getAnnotationText() + '"} ' : '';
        paramAsString += '' + this.getBType() + ' ';
        paramAsString += this.getIdentifier();

        return paramAsString.trim();
    }

    /**
     * Gets the supported annotations for a path param.
     * @return {string[]} - The supported annotations for a resource param.
     * @static
     */
    static getSupportedAnnotations() {
        return ['@http:PathParam', '@http:QueryParam'/* , "@HeaderParam", "@FormParam", "@Body"*/];
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        this.setBType(jsonNode.parameter_type, { doSilently: true });
        this.setIdentifier(jsonNode.parameter_name, { doSilently: true });

        // As of now we only support one annotation.
        if (_.isEqual(_.size(jsonNode.children), 1) && _.isEqual(jsonNode.children[0].type, 'annotation_attachment')) {
            const annotationJson = jsonNode.children[0];
            this.setAnnotationType('@' + annotationJson.annotation_package_name + ':' + annotationJson.annotation_name, { doSilently: true });
            this.setAnnotationText(annotationJson.children[0].value, { doSilently: true });
        }
    }
}

export default ResourceParameter;

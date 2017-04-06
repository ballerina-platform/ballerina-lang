/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
        super('Annotation');
        this._annotationName = _.get(args, 'annotationName');
        this._attachmentPoints = _.get(args, 'attachmentPoints', []);
    }

    setAnnotationName(annotationName, options) {
        if (!_.isNil(annotationName) && ASTNode.isValidIdentifier(annotationName)) {
            this.setAttribute('_annotationName', annotationName, options);
        } else {
            var error = "Invalid name for the annotation name: " + annotationName;
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

    getAnnotationDefinitions(){
    }

    /**
     * @inheritDoc
     * @override
     * */
    initFromJson(jsonNode) {
        var self = this;
        this.setAnnotationName(jsonNode.annotation_name, {doSilently: true});
        this.setAttachmentPoints(_.split(jsonNode.annotation_attachment_points, ','), {doSilently: true});
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
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
                parent: [{
                    node: this.parent,
                    getChildrenFunc: this.parent.getAnnotationDefinitions(),
                    getter: this.getAnnotationName
                }]
            }]
        });
    }
}

export default AnnotationDefinition;
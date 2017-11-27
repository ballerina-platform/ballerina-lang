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

import AnnotationHelper from 'plugins/ballerina/env/helpers/annotation-helper';
import BallerinaEnvironment from 'plugins/ballerina/env/environment';
import NodeFactory from 'plugins/ballerina/model/node-factory';
import TreeUtil from 'plugins/ballerina/model/tree-util';

/**
 * Deletes a given node from the tree.
 *
 * @param {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue} node The node.
 */
function deleteNode(node) {
    if (TreeUtil.isAnnotationAttachment(node) && !TreeUtil.isAnnotationAttachmentAttributeValue(node.parent)) {
        node.parent.removeAnnotationAttachments(node);
    } else if (TreeUtil.isAnnotationAttachmentAttribute(node) && TreeUtil.isAnnotationAttachment(node.parent)) {
        node.parent.removeAttributes(node);
    } else if (TreeUtil.isAnnotationAttachmentAttributeValue(node)
                && TreeUtil.isAnnotationAttachmentAttributeValue(node.parent)) {
        node.parent.removeValueArray(node);
    } else if (TreeUtil.isAnnotationAttachment(node)
                && TreeUtil.isAnnotationAttachmentAttributeValue(node.parent)
                && TreeUtil.isAnnotationAttachmentAttributeValue(node.parent.parent)) {
        node.parent.parent.removeValueArray(node.parent);
    }
}

/**
 * Adds an attribute to a given annotation-attachment.
 *
 * @param {AnnotationAttchment} annotationAttachment The annotation attachment.
 * @returns The newly added attribute.
 */
function addAttribute(annotationAttachment) {
    const literal = NodeFactory.createLiteral({
        value: '""',
    });
    const annotationAttributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
    const annotationAttribute = NodeFactory.createAnnotationAttachmentAttribute();

    annotationAttributeValue.setValue(literal);
    annotationAttribute.setName(NodeFactory.createLiteral({
        value: '',
    }));
    annotationAttribute.setValue(annotationAttributeValue);
    if (annotationAttachment) {
        annotationAttachment.addAttributes(annotationAttribute);
    }
    return annotationAttribute;
}

/**
 * Generates a suitable annotation attribute value for a given key.
 *
 * @param {string} annotationAttributeKey The key of the value
 * @param {AnnotationDefintion} annotationDefinitionModel The annotation definition model of which the key belongs to.
 * @returns {AnnotationAttributeValue} The generated node.
 */
function getArrayValue(environment, annotationAttributeKey, annotationDefinitionModel) {
    const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(environment, annotationAttributeKey,
        annotationDefinitionModel.getPackagePath(), annotationDefinitionModel.getName());
    annotationAttributeDef.setPackagePath(annotationDefinitionModel.getPackagePath());
    const arrayAnnotationAttributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
    if (BallerinaEnvironment.getTypes().includes(annotationAttributeDef.getBType().replace('[]', ''))) {
        const bValueInArray = NodeFactory.createLiteral();
        // bValueInArray.setBType(annotationAttributeDef.getBType());
        arrayAnnotationAttributeValue.setValue(bValueInArray);
    } else {
        const annotationAttachmentInArray = NodeFactory.createAnnotationAttachment({
            packageAlias: NodeFactory.createLiteral({
                value: annotationAttributeDef.getPackagePath() ?
                                                        annotationAttributeDef.getPackagePath().split('.').pop() : '',
            }),
            annotationName: NodeFactory.createLiteral({
                value: annotationAttributeDef.getBType().split(':').pop().replace('[]', ''),
            }),
        });
        arrayAnnotationAttributeValue.setValue(annotationAttachmentInArray);
    }
    return arrayAnnotationAttributeValue;
}

export {
    deleteNode,
    addAttribute,
    getArrayValue,
};

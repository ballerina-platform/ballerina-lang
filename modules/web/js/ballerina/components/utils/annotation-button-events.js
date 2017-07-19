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

import AnnotationHelper from './../../env/helpers/annotation-helper';
import ASTFactory from './../../ast/ballerina-ast-factory';
import BallerinaEnvironment from '../../env/environment';

/**
 * Deletes a given node from the tree.
 *
 * @param {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue} node The node.
 */
function deleteNode(node) {
    const factory = node.getFactory();
    if (factory.isAnnotationAttachment(node)) {
        node.getParent().removeChild(node);
    } else if (factory.isAnnotationAttribute(node) && factory.isAnnotationAttachment(node.getParent())) {
        node.getParent().removeChild(node);
    } else if (factory.isAnnotationAttributeValue(node) && factory.isAnnotationAttributeValue(node.getParent())) {
        node.getParent().removeChild(node);
    }
}

/**
 * Generates a child node.
 *
 * @param {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue} node The node.
 * @returns {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue|BValue} New child.
 */
function generateChildNode(node) {
    const factory = node.getFactory();
    if (factory.isAnnotationAttachment(node)) {
        const bValue = factory.createBValue();
        const annotationAttributeValue = factory.createAnnotationAttributeValue();
        const annotationAttribute = factory.createAnnotationAttribute();

        annotationAttributeValue.addChild(bValue);
        annotationAttribute.addChild(annotationAttributeValue);
        return annotationAttribute;
    } else if (factory.isAnnotationAttribute(node)) {
        const bValue = factory.createBValue();
        const annotationAttributeValue = factory.createAnnotationAttributeValue();
        annotationAttributeValue.addChild(bValue);
        return annotationAttributeValue;
    } else if (factory.isAnnotationAttributeValue(node)) {
        if (node.isBValue()) {
            return factory.createBValue();
        } else if (node.isAnnotation()) {
            return factory.createAnnotationAttachment();
        } else if (node.isArray()) {
            return factory.createAnnotationAttributeValue();
        }
        return undefined;
    }

    return undefined;
}

/**
 * Adds a child node to a given node
 *
 * @param {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue} node The node.
 * @param {number} index Index at which the child should be added.
 * @returns {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue|BValue} Newly added child.
 */
function addChildNode(node, index) {
    const factory = node.getFactory();
    if (factory.isAnnotationAttachment(node)) {
        const annotationAttribute = generateChildNode(node);
        node.addChild(annotationAttribute, index);
        return annotationAttribute;
    } else if (factory.isAnnotationAttribute(node)) {
        const annotationAttributeValue = generateChildNode(node);
        node.addChild(annotationAttributeValue, index);
        return annotationAttributeValue;
    } else if (factory.isAnnotationAttributeValue(node)) {
        const child = generateChildNode(node);
        node.addChild(child, index);
        return child;
    }

    const annotationAttachment = factory.createAnnotationAttachment();
    node.addChild(annotationAttachment, index);
    return annotationAttachment;
}

/**
 * Adds a node above the given node.
 *
 * @param {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue} node The node.
 * @returns {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue|BValue} Newly added child.
 */
function addChildNodeAbove(node) {
    const currentNodeIndex = node.getParent().getIndexOfChild(node);
    // As it replaces its index.
    const newChildIndex = currentNodeIndex;
    return addChildNode(node.getParent(), newChildIndex);
}

/**
 * Adds a node below the given node.
 *
 * @param {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue} node The node.
 * @returns {AnnotationAttachment|AnnotationAttribute|AnnotationAttribtueValue|BValue} Newly added child.
 */
function addChildNodeBelow(node) {
    const currentNodeIndex = node.getParent().getIndexOfChild(node);
    const newChildIndex = currentNodeIndex + 1;
    return addChildNode(node.getParent(), newChildIndex);
}

/**
 * Adds an attribute to a given annotation-attachment.
 *
 * @param {AnnotationAttchment} annotationAttachment The annotation attachment.
 * @returns The newly added attribute.
 */
function addAttribute(annotationAttachment) {
    const factory = annotationAttachment.getFactory();
    const bValue = factory.createBValue();
    const annotationAttributeValue = factory.createAnnotationAttributeValue();
    const annotationAttribute = factory.createAnnotationAttribute();

    annotationAttributeValue.addChild(bValue);
    annotationAttribute.addChild(annotationAttributeValue);
    annotationAttachment.addChild(annotationAttribute);
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
    const arrayAnnotationAttributeValue = ASTFactory.createAnnotationAttributeValue();
    if (BallerinaEnvironment.getTypes().includes(annotationAttributeDef.getBType())) {
        const bValueInArray = ASTFactory.createBValue();
        bValueInArray.setBType(annotationAttributeDef.getBType());
        arrayAnnotationAttributeValue.addChild(bValueInArray);
    } else {
        const annotationAttachmentInArray = ASTFactory.createAnnotationAttachment();
        annotationAttachmentInArray.setFullPackageName(annotationAttributeDef.getPackagePath());
        if (annotationAttributeDef.getPackagePath() !== undefined &&
                                                        annotationAttributeDef.getPackagePath() !== 'Current Package') {
            annotationAttachmentInArray.setPackageName(annotationAttributeDef.getPackagePath().split('.').pop());
        }
        annotationAttachmentInArray.setName(annotationAttributeDef.getBType());
        arrayAnnotationAttributeValue.addChild(annotationAttachmentInArray);
    }
    return arrayAnnotationAttributeValue;
}

export {
    deleteNode,
    addChildNode,
    addChildNodeAbove,
    addChildNodeBelow,
    addAttribute,
    generateChildNode,
    getArrayValue,
};

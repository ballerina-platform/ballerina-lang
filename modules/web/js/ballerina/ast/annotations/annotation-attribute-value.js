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
import ASTNode from '../node';

/**
 * AST node for an annotation attribute value.
 *
 * @class AnnotationAttributeValue
 * @extends {ASTNode}
 */
class AnnotationAttributeValue extends ASTNode {

    /**
     * Creates an instance of AnnotationAttributeValue.
     * @memberof AnnotationAttributeValue
     */
    constructor() {
        super('AnnotationAttributeValue');
        this.whiteSpace.defaultDescriptor.regions = {
            3: ' ',     // space before value
            4: '\n',    // space after value
        };
    }

    /**
     * If the current value a b-value.
     *
     * @returns {boolean} true if b-value, else false.
     * @memberof AnnotationAttributeValue
     */
    isBValue() {
        return this.getChildren().length > 0 && this.getFactory().isBValue(this.getChildren()[0]);
    }

    /**
     * If the current value an annotation-attachment.
     *
     * @returns {boolean} true if annotation-attachment, else false.
     * @memberof AnnotationAttributeValue
     */
    isAnnotation() {
        return this.getChildren().length > 0 && this.getFactory().isAnnotationAttachment(this.getChildren()[0]);
    }

    /**
     * If the current value an array. In which case the children will be {@link AnnotationAttributeValue}s
     *
     * @returns {boolean} true if n array, else false.
     * @memberof AnnotationAttributeValue
     */
    isArray() {
        return this.getChildren().length === 0 || this.getFactory().isAnnotationAttributeValue(this.getChildren()[0]);
    }

    /**
     * Setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        jsonNode.children.forEach((childNode) => {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default AnnotationAttributeValue;

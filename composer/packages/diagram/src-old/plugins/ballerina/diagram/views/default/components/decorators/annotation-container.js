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
let keyIndex = 0;

/**
 * Class of annotation container.
 *
 * @class AnnotationContainer
 * */
class AnnotationContainer {

    /**
     * Constructor for AnnotationContainer
     *
     * @param {object} bBox - binding box for the Annotation Container
     * @param {object} annotations - annotations collection
     * @param {ASTNode} parentNode - node of the parent of annotation container.
     * */
    constructor(bBox, annotations, parentNode) {
        this.bBox = bBox;
        this.annotations = annotations;
        this.parentNode = parentNode;
    }

    /**
     * Get ID of the annotation container.
     *
     * @return {string} annotation container Id.
     * */
    getID() {
        keyIndex += 1;
        return `annotation-container-${keyIndex}`;
    }

    /**
     * Gets the annotations of the container.
     *
     * @returns {AnnotationAttachment[]} Annotation attachments.
     * @memberof AnnotationContainer
     */
    getAnnotations() {
        return this.annotations;
    }
}

export default AnnotationContainer;

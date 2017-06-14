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
import ASTVisitor from '../ast-visitor';

/**
 * Constructor for the Abstract JSON Generation Visitor
 * @extends ASTVisitor
 */
class AbstractSwaggerJsonGenVisitor extends ASTVisitor {
    constructor(parent) {
        super();
        this._swaggerJson = { swagger: '2.0' };
        this.parent = parent;
    }

    getSwaggerJson() {
        return this._swaggerJson;
    }

    setSwaggerJson(swaggerJson) {
        this._swaggerJson = swaggerJson;
    }

    getParent() {
        return this.parent;
    }

    astToJson(annotationEntry) {
        if (annotationEntry.getFactory().isAnnotation(annotationEntry.getRightValue())) {
            // When the right value is an annotation entry array
            const arrayValues = [];
            _.forEach(annotationEntry.getRightValue().getChildren(), (annotationEntry) => {
                arrayValues.push(this.astToJson(annotationEntry));
            });

            return { key: annotationEntry.getLeftValue(), value: arrayValues };
        } else if (annotationEntry.getFactory().isAnnotationEntryArray(annotationEntry.getRightValue())) {
            // When the right value is an annotation entry array
            const arrayValues = [];
            _.forEach(annotationEntry.getRightValue().getChildren(), (annotationEntry) => {
                arrayValues.push(this.astToJson(annotationEntry));
            });

            return { key: annotationEntry.getLeftValue(), value: arrayValues };
        }
            // When the right value is an BValue.
        return { key: annotationEntry.getLeftValue(), value: annotationEntry.getRightValue().replace(/"/g, '') };
    }

    /**
     * Removes the double quotes of a string value.
     * @param {string} val A string value.
     */
    removeDoubleQuotes(val) {
        return val.replace(/"/g, '');
    }
}

export default AbstractSwaggerJsonGenVisitor;

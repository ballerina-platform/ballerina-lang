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
import ASTNode from '../node';

/**
 * Contains children of annotation-entry type.
 * @extends ASTNode
 */
class AnnotationEntryArray extends ASTNode {
    constructor() {
        super('Annotation-Entry-Array');
    }

    /**
     * An annotation array stringified along with its children.
     * @return {string}
     */
    toString() {
        let stringVal = '[';
        let annotationEntries = [];
        _.forEach(this.getChildren(), function(annotationEntry){
            annotationEntries.push(annotationEntry.toString());
        });
        stringVal += _.join(annotationEntries, ', ');
        stringVal += ']';
        return stringVal;
    }
    /**
     * Setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        _.each(jsonNode.children, childNode => {
            let child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default AnnotationEntryArray;

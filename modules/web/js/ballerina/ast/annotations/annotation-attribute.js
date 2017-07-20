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
 * AST node for an attribute residing in an annotation-attachment. There should only be one child.
 * The one child epresents the value.
 *
 * @class AnnotationAttribute
 * @extends {ASTNode}
 */
class AnnotationAttribute extends ASTNode {
    /**
     * Creates an instance of AnnotationAttribute.
     * @param {object} args Arguments for creating an annotation attribute.
     * @param {string} args.key The key value.
     * @memberof AnnotationAttribute
     */
    constructor(args) {
        super('AnnotationAttribute');

        /**
         * The key part of an annotation attribute.
         * @type {string}
         */
        this._key = _.get(args, 'key');

        this.whiteSpace.defaultDescriptor.regions = {
            0: '',      // space before key
            1: '',      // space between key and ':'
            2: ' ',     // ignore
        };
    }

    // eslint-disable-next-line require-jsdoc
    setKey(key, options) {
        this.setAttribute('_key', key, options);
    }

    // eslint-disable-next-line require-jsdoc
    getKey() {
        return this._key;
    }

    // eslint-disable-next-line require-jsdoc
    setValue(value, ignoreTreeModified) {
        this.removeChild(this.getChildren()[0], true);
        this.addChild(value, undefined, ignoreTreeModified);
    }

    // eslint-disable-next-line require-jsdoc
    getValue() {
        return this.getChildren()[0];
    }

    /**
     * Creates an annotation attribute using json model.
     * @param {Object} jsonNode The json.
     * @param {string} jsonNode.annotation_attribute_pair_key The key.
     * @memberof AnnotationAttribute
     */
    initFromJson(jsonNode) {
        if (jsonNode.annotation_attribute_pair_key === 'undefined') {
            this.setKey(undefined, { doSilently: true });
        } else {
            this.setKey(jsonNode.annotation_attribute_pair_key, { doSilently: true });
        }
        jsonNode.children.forEach((childNode) => {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default AnnotationAttribute;

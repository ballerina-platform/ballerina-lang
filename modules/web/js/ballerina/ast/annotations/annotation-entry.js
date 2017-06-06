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
import log from 'log';
import ASTNode from '../node';

/**
 * This represents a map entry for an {@link Annotation} and a single entry for a {@link AnnotationEntryArray}. This ast
 * node should NOT have children.
 * @extends ASTNode
 */
class AnnotationEntry extends ASTNode {
    /**
     * Creates an annotation entry AST node.
     * @param {object} args Arguments to create the annotation entry node.
     * @param {string} args.leftValue The value of the key.
     * @param {string|Annotation|AnnotationEntryArray} args.rightValue The value of the right hand element.
     */
    constructor(args) {
        super('Annotation-Entry');
        /**
         * Assigned key of an entry. Nullable.
         * @type {string}
         */
        this._leftValue = _.get(args, 'leftValue', 'value');

        /**
         * The value of the entry
         * @type {string|Annotation|AnnotationEntryArray}
         */
        this._rightValue = _.get(args, 'rightValue');

        if (!_.isUndefined(this._rightValue) && !_.isString(this._rightValue)) {
            this._rightValue.on('tree-modified', (event) => {
                this.trigger('tree-modified', event);
            });
        }
    }

    setLeftValue(leftValue, options) {
        this.setAttribute('_leftValue', leftValue, options);
    }

    getLeftValue() {
        return this._leftValue;
    }

    setRightValue(rightValue, options) {
        this.setAttribute('_rightValue', rightValue, options);
        if (!_.isString(rightValue)) {
            rightValue.on('tree-modified', (event) => {
                this.trigger('tree-modified', event);
            });
        }
    }

    getRightValue() {
        return this._rightValue;
    }

    /**
     * Generates ballerina code for the entry.
     * @return {string}
     */
    toString() {
        let annotationEntryAsString;
        if (_.isString(this._rightValue)) {
            annotationEntryAsString = this._rightValue;
        } else {
            annotationEntryAsString = this._rightValue.toString();
        }

        if (!_.isUndefined(this._leftValue) && !_.isEmpty(this._leftValue)) {
            return this._leftValue + ': ' + annotationEntryAsString;
        } else {
            return annotationEntryAsString;
        }
    }

    /**
     * Setting parameters from json
     * @param {object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.setLeftValue(jsonNode.annotation_entry_key, {doSilently: true});
        if (!_.isString(jsonNode.annotation_entry_value)) {
            let child = this.getFactory().createFromJson(jsonNode.annotation_entry_value);
            if (this.getFactory().isAnnotation(child) || this.getFactory().isAnnotationEntryArray(child)) {
                child.initFromJson(jsonNode.annotation_entry_value);
                this.setRightValue(child, {doSilently: true});
            } else {
                log.error('Unknown object found when parsing an annotation');
            }
        } else {
            this.setRightValue(jsonNode.annotation_entry_value, {doSilently: true});
        }
    }
}

export default AnnotationEntry;

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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

/**
 * Source gen visitor for a b-value.
 *
 * @class BValueVisitor
 * @extends {AbstractSourceGenVisitor}
 */
class BValueVisitor extends AbstractSourceGenVisitor {
    /**
     * Checks if the b-value and be visited.
     *
     * @returns {boolean} true if can be visited, else false.
     * @memberof BValueVisitor
     */
    canVisitBValue() {
        return true;
    }

    /**
     * Begins to visit a b-value.
     *
     * @param {BValue} bValue The node being visited.
     * @memberof BValueVisitor
     */
    beginVisitBValue(bValue) {
        if (bValue.getBType() === 'string') {
            this.appendSource(`"${_.trim(bValue.getStringValue(), '"')}"`);
        } else {
            if (bValue.getBType() === 'int') {
                if (bValue.getStringValue() === undefined) {
                    this.appendSource(0);
                } else {
                    this.appendSource(bValue.getStringValue());
                }
            } else if (bValue.getBType() === 'boolean') {
                if (bValue.getStringValue() === undefined) {
                    this.appendSource(false);
                } else {
                    this.appendSource(bValue.getStringValue());
                }
            } else {
                this.appendSource(bValue.getStringValue());
            }
        }
    }

    /**
     * Visiting the body of the annotation.
     * @memberof BValueVisitor
     */
    visitBValue() {
    }

    /**
     * Ends visiting the b-value
     * @memberof BValueVisitor
     */
    endVisitBValue() {
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default BValueVisitor;

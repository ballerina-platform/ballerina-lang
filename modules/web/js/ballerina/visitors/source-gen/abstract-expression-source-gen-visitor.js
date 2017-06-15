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
import ExpressionVisitor from '../expression-visitor';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

/**
 * Constructor for the Abstract Source Generation Visitor for the expressions
 * @param parent
 * @constructor
 */
class AbstractExpressionSourceGenVisitor extends ExpressionVisitor {
    constructor(parent) {
        super();
        this._generatedSource = '';
        this.parent = parent;
    }

    getGeneratedSource() {
        return this._generatedSource;
    }

    setGeneratedSource(generatedSource) {
        this._generatedSource = generatedSource;
    }

    appendSource(source) {
        this._generatedSource += source;
    }

    getParent() {
        return this.parent;
    }
}

AbstractExpressionSourceGenVisitor.prototype.constructor = AbstractSourceGenVisitor;

export default AbstractExpressionSourceGenVisitor;

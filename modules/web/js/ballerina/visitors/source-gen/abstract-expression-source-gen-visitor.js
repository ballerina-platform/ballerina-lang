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
 * Source generation for the Abstract Source Generation Visitor for the expressions
 */
class AbstractExpressionSourceGenVisitor extends ExpressionVisitor {

    /**
     * Constructor for abstract expression source generation visitor
     * @param {ASTVisitor} parent - parent visitor
     * @constructor
     */
    constructor(parent) {
        super();
        this._generatedSource = '';
        this.parent = parent;
        this._sourceGenMeta = (parent) ? parent.getSourceGenMeta() : { sourceTotalLineCount: 0 };
    }

    /**
     * Get the current Generated code
     * @return {string} - current generated code
     */
    getGeneratedSource() {
        return this._generatedSource;
    }

    /**
     * Set the generated code
     * @param {string} generatedSource - Generated code
     */
    setGeneratedSource(generatedSource) {
        this._generatedSource = generatedSource;
    }

    /**
     * Append source segment to the generated source
     * @param {string} source - code segment
     */
    appendSource(source) {
        this._generatedSource += source;
    }

    /**
     * Get the parent source generation visitor
     * @return {object} - parent
     */
    getParent() {
        return this.parent;
    }

    /**
     * Get the source generation related meta info object
     * @return {{sourceTotalLineCount: number}} source generation meta information
     */
    getSourceGenMeta() {
        return this._sourceGenMeta;
    }

    /**
     * Increase the total line count of the source by certain number of lines
     * @param {number} lines - number of lines from which the total is increased
     */
    increaseTotalSourceLineCountBy(lines) {
        this.getSourceGenMeta().sourceTotalLineCount += lines;
    }

    /**
     * Get the current total line numbers of the source
     * @return {number} - total number of lines in the source
     */
    getTotalNumberOfLinesInSource() {
        return this.getSourceGenMeta().sourceTotalLineCount;
    }

    /**
     * Get the number of endlines/carriage returns in the given segment
     * @param {string} sourceSegment - source segment
     * @return {number} - number of endLines
     */
    getEndLinesInSegment(sourceSegment) {
        return (sourceSegment.match(/\n/g) || []).length;
    }
}

AbstractExpressionSourceGenVisitor.prototype.constructor = AbstractSourceGenVisitor;

export default AbstractExpressionSourceGenVisitor;

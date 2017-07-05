/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import StatementVisitor from '../statement-visitor';
import SourceGenUtil from './source-gen-util';

/**
 * Class for statement source generation visitor
 * @extends ASTVisitor
 */
class AbstractStatementSourceGenVisitor extends StatementVisitor {
    /**
     * Constructor for abstract statement source generation visitor
     * @param {ASTVisitor} parent - parent visitor
     * @constructor
     */
    constructor(parent) {
        super();
        this._generatedSource = '';
        this.parent = parent;
        this.indentCount = (parent) ? parent.indentCount : 0;
        this._sourceGenMeta = (parent) ? parent.getSourceGenMeta() : { sourceTotalLineCount: 0 };
    }

    /**
     * Increase the indent Count
     */
    indent() {
        this.indentCount++;
    }

    /**
     * Decrease the indent count
     */
    outdent() {
        this.indentCount--;
    }

    /**
     * Get the current indentation
     * @return {string} Indentation
     */
    getIndentation() {
        let indentation = '';
        for (let i = 0; i < this.indentCount; i++) {
            indentation += '    ';
        }
        return indentation;
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
     * Get the current preceding indentation
     * @return {string} - preceding indentation
     */
    getCurrentPrecedingIndentation() {
        return SourceGenUtil.getTailingIndentation(this.getParent().getGeneratedSource());
    }

    /**
     * Replace the current preceding indentation
     * @param {string} newIndentation - new indentation
     */
    replaceCurrentPrecedingIndentation(newIndentation) {
        const increasedNumberOfEndLines = this.getEndLinesInSegment(newIndentation);
        const newContent = SourceGenUtil
            .replaceTailingIndentation(this.getParent().getGeneratedSource(), newIndentation);
        this.getParent().setGeneratedSource(newContent);
        this.increaseTotalSourceLineCountBy(increasedNumberOfEndLines);
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

export default AbstractStatementSourceGenVisitor;

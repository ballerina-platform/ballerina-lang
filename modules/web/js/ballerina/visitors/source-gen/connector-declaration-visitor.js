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

import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

/**
 * Source generation visitor for connector declaration
 */
class ConnectorDeclarationVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit for ConnectorDeclaration
     * @return {boolean} - true
     */
    canVisitConnectorDeclaration() {
        return true;
    }

    /**
     * Begin visit for ConnectorDeclaration
     * @param {ConnectorDeclaration} connectorDeclaration - connector declaration node
     * @returns {void}
     */
    beginVisitConnectorDeclaration(connectorDeclaration) {
        if (connectorDeclaration.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        connectorDeclaration.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = connectorDeclaration.getStatementString();

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit ConnectorDeclaration
     */
    visitConnectorDeclaration() {
    }

    /**
     * End visit call for ConnectorDeclarationVisitor
     * @param {ConnectorDeclaration} connectorDeclaration - corresponding connector declaration
     * @returns {void}
     */
    endVisitConnectorDeclaration(connectorDeclaration) {
        const constructedSourceSegment = connectorDeclaration.getWSRegion(3)
            + ';' + connectorDeclaration.getWSRegion(4)
            + ((connectorDeclaration.whiteSpace.useDefault)
                ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ConnectorDeclarationVisitor;

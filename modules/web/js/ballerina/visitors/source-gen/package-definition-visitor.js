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
import _ from 'lodash';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

/**
 * Source generation for package definition
 */
class PackageDefinitionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the Package definition
     * @return {boolean} true|false
     */
    canVisitPackageDefinition() {
        return true;
    }

    /**
     * Begin visit package definition source generation
     * @param {PackageDefinition} packageDefinition - Package definition ASTNode
     */
    beginVisitPackageDefinition(packageDefinition) {
        /**
         * set the configuration start for the package definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        if (!_.isNil(packageDefinition.getPackageName()) && packageDefinition.getPackageName() !== '') {
            // Calculate the line number
            const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
            packageDefinition.setLineNumber(lineNumber, { doSilently: true });

            const middleWS = packageDefinition.getParent().getWSRegion(1);
            const constructedSourceSegment = 'package'
                + ((packageDefinition.whiteSpace.useDefault) ? ' ' : middleWS)
                + packageDefinition.getPackageName();

            const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
            // Increase the total number of lines
            this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

            this.appendSource(constructedSourceSegment);
        }
    }

    /**
     * Visit package definition
     */
    visitPackageDefinition() {
    }

    /**
     * End visit package definition source generation
     * @param {PackageDefinition} packageDefinition - Package definition ASTNode
     */
    endVisitPackageDefinition(packageDefinition) {
        if (!_.isNil(packageDefinition.getPackageName()) && packageDefinition.getPackageName() !== '') {
            let tailingWS = packageDefinition.getParent().getWSRegion(3);
            tailingWS = (packageDefinition.whiteSpace.useDefault) ? '\n\n' : tailingWS;

            const constructedSourceSegment = packageDefinition.getParent().getWSRegion(2) + ';' + tailingWS;

            // Add the increased number of lines
            const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
            this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

            this.appendSource(constructedSourceSegment);
            this.getParent().appendSource(this.getIndentation() + this.getGeneratedSource());
        }
    }
}

export default PackageDefinitionVisitor;

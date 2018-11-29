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
import ConnectorDeclarationVisitor from '../visitors/connector-declaration-visitor';
import ASTFactory from '../ast/ast-factory';

class CommonUtils {
    /**
     * Generates a unique id for an attribute by checking against a list of parents.
     *
     * @param {Object} genArgs - Arguments for creating an object.
     * @param {ASTNode} genArgs.node - The AST node of which the attribute belongs to.
     * @param {Object[]} genArgs.attributes[] - The list of attributes to which the unique ID to be generated.
     * @param {string} genArgs.attributes[].defaultValue - The default value for the attribute.
     * @param {function} genArgs.attributes[].setter - The setter function of the attribute.
     * @param {function} genArgs.attributes[].getter - The getter function of the attribute.
     * @param {Object[]} genArgs.attributes[].parents[] - The list of parents to traverse through to generate a unique
     * ID.
     * @param {ASTNode} genArgs.attributes[].parents[].node - The AST node of the parent.
     * @param {function} genArgs.attributes[].parents[].getChildrenFunc - The getter function to get the children of a
     * parent node.
     * @param {function} genArgs.attributes[].parents[].getter - The getter function which returns the identifier of the
     * children returned from getChildrenFunc.
     */
    static generateUniqueIdentifier(genArgs) {
        _.forEach(genArgs.attributes, (attribute) => {
            if (_.isNil(attribute.getter.call(genArgs.node)) || attribute.checkEvenIfDefined) {
                // To store all the identifiers of the parents.
                const existingIdentifiers = [];

                _.forEach(attribute.parents, (parent) => {
                    log.debug(`Children: ${parent.getChildrenFunc.call(parent.node)}`);
                    // Get the children of the parent.
                    _.forEach(parent.getChildrenFunc.call(parent.node), (child) => {
                        // Skipping the current node.
                        if (!_.isEqual(genArgs.node.getID(), child.getID())) {
                            const childVal = parent.getter.call(child);
                            existingIdentifiers.push(childVal);
                        }
                    });
                });

                log.debug(`Existing identifiers: ${existingIdentifiers}`);

                // Generating the ID.
                let counter = 1;
                const currentAttributeValue = attribute.defaultValue;
                while (true) {
                    const tempNewValue = currentAttributeValue + counter;
                    // Generate unique
                    if (ASTFactory.isEndpointTypeVariableDef(genArgs.node)) {
                        this.connectorDeclarationVisitor = new ConnectorDeclarationVisitor();
                        let parent = genArgs.node.getParent();
                        while (!ASTFactory.isBallerinaAstRoot(parent)) {
                            parent = parent.getParent();
                        }
                        parent.accept(this.connectorDeclarationVisitor);
                        counter = this.connectorDeclarationVisitor.connectorCount;
                        break;
                    }
                    if (!_.includes(existingIdentifiers, tempNewValue)) {
                        break;
                    }
                    counter++;
                }
                const opts = { doSilently: true };
                if (_.isEqual(counter, 0)) {
                    attribute.setter.call(genArgs.node, currentAttributeValue, opts);
                } else {
                    attribute.setter.call(genArgs.node, currentAttributeValue + counter, opts);
                }
            }
        });
    }
}

export default CommonUtils;

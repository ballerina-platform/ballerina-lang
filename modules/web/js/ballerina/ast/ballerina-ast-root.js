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

/**
 * Module for BallerinaASTRoot
 */
import _ from 'lodash';
import log from 'log';
import ASTNode from './node';

/**
 * Constructs BallerinaASTRoot
 * @class BallerinaASTRoot
 * @augments ASTNode
 * @param args
 * @constructor
 */
class BallerinaASTRoot extends ASTNode {
    constructor(args) {
        super('BallerinaASTRoot');
        this.on('tree-modified', (e) => {
            if (e.type === 'child-added') {
                // to add the imports based on the function/action drag and drop to editor
                addImportsForTopLevel(e.data.child, { doSilently: true });
            }
        });
        this.packageDefinition = _.get(args, 'packageDefinition');
        const self = this;
        const factory = self.getFactory();
        // Add new imports on new child added to the canvas.
        // Ignore if already added or if it is a current package
        const addImport = function (fullPackageName, options) {
            if (!self.isExistingPackage(fullPackageName)
                && !_.isEqual(fullPackageName, 'Current Package')) {
                const importDeclaration = factory.createImportDeclaration();
                importDeclaration.setPackageName(fullPackageName, options);
                importDeclaration.setParent(self, options);
                self.addImport(importDeclaration, options);
            }
        };

        const addImportsForTopLevel = function (child, options) {
            if (factory.isAssignmentStatement(child)) {
                // TODO : use the same check for variable def stmt when right expression
                // is refactored to directly contain the expression.
                if (child.getRightExpression() !== undefined) {
                    const expression = child.getRightExpression().getChildren()[0];
                    if (expression
                         && (factory.isFunctionInvocationExpression(expression)
                              || factory.isActionInvocationExpression(expression))
                         && expression.getFullPackageName()) {
                        addImport(expression.getFullPackageName(), options);
                    }
                }
            } else if (factory.isVariableDefinitionStatement(child)) {
                if (child.getRightExpression() !== undefined) {
                    const expression = child.getRightExpression();
                    if (expression
                         && (factory.isFunctionInvocationExpression(expression)
                              || factory.isActionInvocationExpression(expression))
                         && expression.getFullPackageName()) {
                        addImport(expression.getFullPackageName(), options);
                    }
                }
            } else if (factory.isConnectorDeclaration(child)) {
                if (child.getFullPackageName()) {
                    addImport(child.getFullPackageName(), options);
                }
            } else if (factory.isServiceDefinition(child)) {
                const annotations = child.getChildrenOfType(factory.isAnnotation);
                const resources = child.getChildrenOfType(factory.isResourceDefinition);
                _.forEach(annotations, (annotation) => {
                    addImport(annotation.getFullPackageName(), options);
                });
                _.forEach(resources, (resource) => {
                    addImportsForTopLevel(resource, options);
                });
            } else if (factory.isResourceDefinition(child)) {
                const annotations = child.getChildrenOfType(factory.isAnnotation);
                _.forEach(annotations, (annotation) => {
                    addImport(annotation.getFullPackageName(), options);
                });
            } else if (factory.isFunctionInvocationStatement(child)) {
                const functionInvocationExpression = child.findChild(factory.isFunctionInvocationExpression);
                if (functionInvocationExpression &&
                    functionInvocationExpression.getFullPackageName()) {
                    addImport(functionInvocationExpression.getFullPackageName(), options);
                }
            }
        };

        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '',
            3: '\n',
        };
    }

    /**
     * Getter function for PackageDefinition
     * @return {*}
     */
    getPackageDefinition() {
        return this.getChildrenOfType(this.getFactory().isPackageDefinition);
    }

    /**
     * Getter function for import declarations.
     * @return {*}
     */
    getImportDeclarations() {
        const importDeclarations = [];
        const ballerinaASTFactory = this.getFactory();
        _.forEach(this.getChildren(), (child) => {
            if (ballerinaASTFactory.isImportDeclaration(child)) {
                importDeclarations.push(child);
            }
        });
        return importDeclarations;
    }

    /**
     * This is the root element of ballerina ast - so cannot be a child of anyone.
     * @param node
     * @return {boolean}
     */
    canBeAChildOf(node) {
        return false;
    }

    /**
     * Deletes an import with given package name.
     */
    deleteImport(packageName, options) {
        const ballerinaASTFactory = this.getFactory();
        const importDeclaration = _.find(this.getChildren(), (child) => {
            return ballerinaASTFactory.isImportDeclaration(child) && child.getPackageName() == packageName;
        });

        const modifiedEvent = {
            origin: this,
            type: 'child-removed',
            title: 'remove import',
            data: {
                child: importDeclaration,
                index: this.getIndexOfChild(importDeclaration),
            },
        };

        _.remove(this.getChildren(), (child) => {
            return ballerinaASTFactory.isImportDeclaration(child) && child.getPackageName() == packageName;
        });

        /**
         * @event ASTNode#tree-modified
         */
        if (options === undefined || !options.doSilently) {
            this.trigger('tree-modified', modifiedEvent);
        }
    }

    /**
     * Adds new import declaration.
     * @param {ImportDeclaration} importDeclaration - New import declaration.
     */
    addImport(importDeclaration, options) {
        if (this.isExistingPackage(importDeclaration.getPackageName())) {
            const errorString = 'Package "' + importDeclaration.getPackageName() + '" is already imported.';
            log.debug(errorString);
            return;
        }

        const ballerinaASTFactory = this.getFactory();
        let index = _.findLastIndex(this.getChildren(), (child) => {
            return ballerinaASTFactory.isImportDeclaration(child);
        });

        // If there are no imports index is -1. Then we need to add the first import after the package
        // definition which is the first child of the ast root
        if (index === -1) {
            index = 0;
        }
        this.getChildren().splice(index + 1, 0, importDeclaration);

        const modifiedEvent = {
            origin: this,
            type: 'child-added',
            title: 'add import',
            data: {
                child: importDeclaration,
                index: index + 1,
            },
        };

        /**
         * @event ASTNode#tree-modified
         */
        this.trigger('import-new-package', importDeclaration.getPackageName());
        if (options === undefined || !options.doSilently) {
            this.trigger('tree-modified', modifiedEvent);
        }
    }

    // // Start of constant definition functions

    /**
     * Adds a new constance definition.
     * @param {string} bType - The ballerina type.
     * @param {string} identifier - The identifier.
     * @param {string} value - The value of the constant.
     */
    addConstantDefinition(bType, identifier, value) {
        // Check if already constant declaration exists with same identifier.
        const identifierAlreadyExists = _.findIndex(this.getConstantDefinitions(), (constantDefinition) => {
            return constantDefinition.getIdentifier() === identifier;
        }) !== -1;

        // If constant declaration with the same identifier exists, then throw an error. Else create the new constant
        // declaration.
        if (identifierAlreadyExists) {
            const errorString = "A constant with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new constant definition.
            const newConstantDefinition = this.getFactory().createConstantDefinition();
            newConstantDefinition.setBType(bType);
            newConstantDefinition.setIdentifier(identifier);
            newConstantDefinition.setValue(value);

            const self = this;

            // Get the index of the last constant declaration.
            let index = _.findLastIndex(this.getChildren(), (child) => {
                return self.getFactory().isConstantDefinition(child);
            });

            // If index is still -1, then get the index of the last import.
            if (index == -1) {
                index = _.findLastIndex(this.getChildren(), (child) => {
                    return self.getFactory().isImportDeclaration(child);
                });
            }

            // If index is still -1, then consider the package definition.
            if (_.isEqual(index, -1) && !_.isNil(this.getPackageDefinition())) {
                index = 0;
            }

            this.addChild(newConstantDefinition, index + 1);
        }
    }

    /**
     * Adds a global (a variable or a constant) from a json object
     * @param {Object} jsonNode json object representing the global
     * @returns {void}
     */
    addGlobal(jsonNode) {
        const globalNode = this.getFactory().createFromJson(jsonNode);

        if(!this.getFactory().isConstantDefinition(globalNode) &&
            !this.getFactory().isGlobalVariableDefinition(globalNode)) {
            // only constants and global variables can be added at global level
            return;
        }

        globalNode.initFromJson(jsonNode);
        // Get the index of the last constant declaration.
        let index = _.findLastIndex(this.getChildren(), (child) => {
            return this.getFactory().isConstantDefinition(child) ||
                   this.getFactory().isGlobalVariableDefinition(child);
        });

        // If index is still -1, then get the index of the last import.
        if (index == -1) {
            index = _.findLastIndex(this.getChildren(), (child) => {
                return this.getFactory().isImportDeclaration(child);
            });
        }

        // If index is still -1, then consider the package definition.
        if (_.isEqual(index, -1) && !_.isNil(this.getPackageDefinition())) {
            index = 0;
        }

        this.addChild(globalNode, index + 1);
    }

    /**
     * Removes a constant definition.
     * @param {string} modelID - The ID of the constant definition.
     */
    removeConstantDefinition(modelID) {
        const self = this;
        // Deleting the variable from the children.
        const resourceParameter = _.find(this.getChildren(), (child) => {
            return self.getFactory().isConstantDefinition(child) && child.id === modelID;
        });

        this.removeChild(resourceParameter);
    }

    /**
     * Gets all the constant definitions.
     * @return {ConstantDefinition[]} - The constant definitions.
     */
    getConstantDefinitions() {
        const constantDeclarations = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isConstantDefinition(child)) {
                constantDeclarations.push(child);
            }
        });
        return constantDeclarations;
    }

    // // End of constant definition functions

    // // Start of annotation definitions functions

    getAnnotationDefinitions() {
        const annotationDefinition = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isAnnotationDefinition(child)) {
                annotationDefinition.push(child);
            }
        });
        return annotationDefinition;
    }

    // // End of annotation definitions functions

    // // Start of service definitions functions

    getServiceDefinitions() {
        const serviceDefinition = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isServiceDefinition(child)) {
                serviceDefinition.push(child);
            }
        });
        return serviceDefinition;
    }

    // // End of service definitions functions

    // // Start of connector definitions functions

    getConnectorDefinitions() {
        const connectorDefinitions = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isConnectorDefinition(child)) {
                connectorDefinitions.push(child);
            }
        });
        return connectorDefinitions;
    }

    // // End of connector definitions functions

    // // Start of function definitions functions

    getFunctionDefinitions() {
        const functionDefinitions = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isFunctionDefinition(child)) {
                functionDefinitions.push(child);
            }
        });
        return functionDefinitions;
    }

    // // End of function definitions functions

    // // Start of struct definitions functions

    getStructDefinitions() {
        const structDefinitions = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isStructDefinition(child)) {
                structDefinitions.push(child);
            }
        });
        return structDefinitions;
    }

    // // End of struct definitions functions

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        const BallerinaASTFactory = this.getFactory();

        const existingMainFunction = _.find(this.getFunctionDefinitions(), (functionDef) => {
            return functionDef.isMainFunction();
        });

        if (!_.isNil(existingMainFunction) && BallerinaASTFactory.isFunctionDefinition(node) && node.isMainFunction()) {
            return false;
        }

        return BallerinaASTFactory.isServiceDefinition(node)
            || BallerinaASTFactory.isFunctionDefinition(node)
            || BallerinaASTFactory.isConnectorDefinition(node)
            || BallerinaASTFactory.isTypeDefinition(node)
            || BallerinaASTFactory.isConnectorDefinition(node)
            || BallerinaASTFactory.isStructDefinition(node)
            || BallerinaASTFactory.isAnnotationDefinition(node);
    }

    /**
     * Check whether package name is existing one or not.
     * @param {String} packageName
     * @return {Boolean} if exist returns true if doesn't return false
     * */
    isExistingPackage(packageName) {
        return !!_.find(this.getImportDeclarations(), (child) => {
            return _.isEqual(child.getPackageName(), packageName);
        });
    }
}

export default BallerinaASTRoot;

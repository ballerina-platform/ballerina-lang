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
import ImportDeclaration from './import-declaration';

/**
 * Constructs BallerinaASTRoot
 * @class BallerinaASTRoot
 * @augments ASTNode
 * @param args
 * @constructor
 */
class BallerinaASTRoot extends ASTNode {
    constructor(args) {
        super("BallerinaASTRoot");
        this.packageDefinition = _.get(args, 'packageDefinition');
        const self = this;
        const factory = self.getFactory();
        // Add new imports on new child added to the canvas.
        // Ignore if already added or if it is a current package
        const addImport = function (fullPackageName) {
            if (!self.isExistingPackage(fullPackageName)
                && !_.isEqual(fullPackageName, "Current Package")) {
                let importDeclaration = factory.createImportDeclaration();
                importDeclaration.setPackageName(fullPackageName);
                importDeclaration.setParent(self);
                self.addImport(importDeclaration);
            }
        };

        const addImportsForTopLevel = function (child, options) {
            if (factory.isAssignmentStatement(child)) {
                if (child._fullPackageName) {
                    addImport(child.getFullPackageName(), options);
                }
            } else if (factory.isConnectorDeclaration(child)) {
                if (child._fullPackageName) {
                    addImport(child.getFullPackageName(), options);
                }
            } else if (factory.isServiceDefinition(child)) {
                const annotations = child.getChildrenOfType(factory.isAnnotation);
                const resources = child.getChildrenOfType(factory.isResourceDefinition);
                _.forEach(annotations, annotation => {
                    addImport(annotation.getFullPackageName(), options);
                });
                _.forEach(resources, resource => {
                    addImportsForTopLevel(resource, options);
                });
            } else if (factory.isResourceDefinition(child)) {
                let annotations = child.getChildrenOfType(factory.isAnnotation);
                _.forEach(annotations, annotation => {
                    addImport(annotation.getFullPackageName(), options);
                });
            } else if (factory.isActionInvocationExpression(child)) {
                if (child._fullPackageName) {
                    addImport(child.getFullPackageName(), options);
                }
            } else if (factory.isFunctionInvocationStatement(child)) {
                const functionInvocationExpression = _.find(child.children, function (child) {
                    return factory.isFunctionInvocationExpression(child);
                });
                if (functionInvocationExpression &&
                    functionInvocationExpression._fullPackageName) {
                    addImport(functionInvocationExpression.getFullPackageName(), options);
                }
            }
        };
        this.on('tree-modified', function (e) {
            if (e.type === "child-added") {
                //to add the imports based on the function/action drag and drop to editor
                addImportsForTopLevel(e.data.child, {doSilently: true});
            }
        });
        this.setWhiteSpaceDescriptor({
            regions: {
                0: '',
                1: ' ',
                2: '',
                3: '\n'
            }
        });
    }

    /**
     * Setter function for PackageDefinition
     * @param packageDefinition
     */
    setPackageDefinition(packageDefinition, options) {
        if(!_.isNil(packageDefinition)){
            packageDefinition.setParent(this, options);
            this.setAttribute('packageDefinition', packageDefinition, options);
        }
    }

    /**
     * Getter function for PackageDefinition
     * @return {*}
     */
    getPackageDefinition() {
        return this.packageDefinition;
    }

    /**
     * Getter function for import declarations.
     * @return {*}
     */
    getImportDeclarations() {
        var importDeclarations = [];
        var ballerinaASTFactory = this.getFactory();
        _.forEach(this.getChildren(), function(child){
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
        var ballerinaASTFactory = this.getFactory();
        var importDeclaration = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isImportDeclaration(child) && child.getPackageName() == packageName;
        });

        var modifiedEvent = {
            origin: this,
            type: 'child-removed',
            title: 'remove import',
            data: {
                child: importDeclaration,
                index: this.getIndexOfChild(importDeclaration)
            }
        };

        _.remove(this.getChildren(), function (child) {
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
            var errorString = "Package \"" + importDeclaration.getPackageName() + "\" is already imported.";
            log.error(errorString);
            throw errorString;
        }

        var ballerinaASTFactory = this.getFactory();
        var index = _.findLastIndex(this.getChildren(), function(child){
            return ballerinaASTFactory.isImportDeclaration(child);
        });

        // If there are no imports index is -1. Then we need to add the first import after the package
        // definition which is the first child of the ast root
        if (index === -1) {
            index = 0;
        }
        this.getChildren().splice(index + 1, 0, importDeclaration);

        var modifiedEvent = {
            origin: this,
            type: 'child-added',
            title: 'add import',
            data: {
                child: importDeclaration,
                index: index + 1
            }
        };

        /**
         * @event ASTNode#tree-modified
         */
        this.trigger('import-new-package', importDeclaration.getPackageName());
        if (options === undefined || !options.doSilently) {
            this.trigger('tree-modified', modifiedEvent);
        }
    }

    //// Start of constant definition functions

    /**
     * Adds a new constance definition.
     * @param {string} bType - The ballerina type.
     * @param {string} identifier - The identifier.
     * @param {string} value - The value of the constant.
     */
    addConstantDefinition(bType, identifier, value) {

        // Check if already constant declaration exists with same identifier.
        var identifierAlreadyExists = _.findIndex(this.getConstantDefinitions(), function (constantDefinition) {
            return constantDefinition.getIdentifier() === identifier;
        }) !== -1;

        // If constant declaration with the same identifier exists, then throw an error. Else create the new constant
        // declaration.
        if (identifierAlreadyExists) {
            var errorString = "A constant with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new constant definition.
            var newConstantDefinition = this.getFactory().createConstantDefinition();
            newConstantDefinition.setBType(bType);
            newConstantDefinition.setIdentifier(identifier);
            newConstantDefinition.setValue(value);

            var self = this;

            // Get the index of the last constant declaration.
            var index = _.findLastIndex(this.getChildren(), function (child) {
                return self.getFactory().isConstantDefinition(child);
            });

            // If index is still -1, then get the index of the last import.
            if (index == -1) {
                index = _.findLastIndex(this.getChildren(), function (child) {
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
     * Removes a constant definition.
     * @param {string} modelID - The ID of the constant definition.
     */
    removeConstantDefinition(modelID) {
        var self = this;
        // Deleting the variable from the children.
        var resourceParameter = _.find(this.getChildren(), function (child) {
            return self.getFactory().isConstantDefinition(child) && child.id === modelID;
        });

        this.removeChild(resourceParameter);
    }

    /**
     * Gets all the constant definitions.
     * @return {ConstantDefinition[]} - The constant definitions.
     */
    getConstantDefinitions() {
        var constantDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConstantDefinition(child)) {
                constantDeclarations.push(child);
            }
        });
        return constantDeclarations;
    }

    //// End of constant definition functions

    //// Start of annotation definitions functions

    getAnnotationDefinitions() {
        var annotationDefinition = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isAnnotationDefinition(child)) {
                annotationDefinition.push(child);
            }
        });
        return annotationDefinition;
    }

    //// End of annotation definitions functions

    //// Start of service definitions functions

    getServiceDefinitions() {
        var serviceDefinition = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isServiceDefinition(child)) {
                serviceDefinition.push(child);
            }
        });
        return serviceDefinition;
    }

    //// End of service definitions functions

    //// Start of connector definitions functions

    getConnectorDefinitions() {
        var connectorDefinitions = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConnectorDefinition(child)) {
                connectorDefinitions.push(child);
            }
        });
        return connectorDefinitions;
    }

    //// End of connector definitions functions

    //// Start of function definitions functions

    getFunctionDefinitions() {
        var functionDefinitions = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isFunctionDefinition(child)) {
                functionDefinitions.push(child);
            }
        });
        return functionDefinitions;
    }

    //// End of function definitions functions

    //// Start of struct definitions functions

    getStructDefinitions() {
        var structDefinitions = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isStructDefinition(child)) {
                structDefinitions.push(child);
            }
        });
        return structDefinitions;
    }

    //// End of struct definitions functions

    //// Start of type mapper definitions functions

    getTypeMapperDefinitions() {
        var typeMapperDefinition = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isTypeMapperDefinition(child)) {
                typeMapperDefinition.push(child);
            }
        });
        return typeMapperDefinition;
    }

    //// End of type mapper definitions functions

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        var BallerinaASTFactory = this.getFactory();

        var existingMainFunction = _.find(this.getFunctionDefinitions(), function(functionDef){
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
            || BallerinaASTFactory.isTypeMapperDefinition(node)
            || BallerinaASTFactory.isAnnotationDefinition(node);
    }

    /**
     * Check whether package name is existing one or not.
     * @param {String} packageName
     * @return {Boolean} if exist returns true if doesn't return false
     * */
    isExistingPackage(packageName) {
        return _.find(this.getImportDeclarations(), function (child) {
            return _.isEqual(child.getPackageName(), packageName);
        }) ? true : false;
    }
}

export default BallerinaASTRoot;

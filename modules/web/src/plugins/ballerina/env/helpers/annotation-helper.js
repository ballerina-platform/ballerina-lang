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
import TreeUtils from 'plugins/ballerina/model/tree-util';

/**
 * A helper class relations ballerina environment annotations.
 *
 * @class AnnotationHelper
 */
class AnnotationHelper {

    /**
     * Gets the attributes of a given package name and annotation definition name.
     *
     * @static
     * @param {PackageScopedEnvironment} environment The ballerina environment.
     * @param {string} fullPackageName The full package name.
     * @param {string} annotationDefinitionName The annotation definition name.
     * @returns {AnnotationAttributeDefinition[]} List of annotation attribute definitions.
     * @memberof AnnotationHelper
     */
    static getAttributes(environment, fullPackageName, annotationDefinitionName) {
        const annotationAttributes = [];
        for (const packageDefintion of environment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationDefinitionName) {
                        for (const annotationAttribute of annotationDefinition.getAnnotationAttributeDefinitions()) {
                            annotationAttributes.push(annotationAttribute);
                        }
                    }
                }
            }
        }

        if (!fullPackageName) {
            if (environment.getBuiltInPackage()) {
                for (const annotationDefinition of environment.getBuiltInPackage().getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationDefinitionName) {
                        for (const annotationAttribute of annotationDefinition.getAnnotationAttributeDefinitions()) {
                            annotationAttributes.push(annotationAttribute);
                        }
                    }
                }
            }
        }

        if (environment.getCurrentPackage()) {
            for (const annotationDefinition of environment.getCurrentPackage().getAnnotationDefinitions()) {
                if (annotationDefinition.getName() === annotationDefinitionName) {
                    for (const annotationAttribute of annotationDefinition.getAnnotationAttributeDefinitions()) {
                        annotationAttributes.push(annotationAttribute);
                    }
                }
            }
        }

        return annotationAttributes;
    }

    /**
     * Gets the annotation definition of a given package name and definition name.
     *
     * @static
     * @param {PackageScopedEnvironment} environment The ballerina environment.
     * @param {string} fullPackageName The full package name.
     * @param {string} annotationDefinitionName The annotation definition name.
     * @returns {AnnotationDefinition|undefined} The annotation definition.
     * @memberof AnnotationHelper
     */
    static getAnnotationDefinition(environment, fullPackageName, annotationDefinitionName) {
        let matchingAnnotationDefintion;
        for (const packageDefintion of environment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationDefinitionName) {
                        matchingAnnotationDefintion = annotationDefinition;
                    }
                }
            }
        }

        if (!fullPackageName) {
            if (environment.getBuiltInPackage()) {
                for (const annotationDefinition of environment.getBuiltInPackage().getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationDefinitionName) {
                        matchingAnnotationDefintion = annotationDefinition;
                    }
                }
            }
        }

        if (environment.getCurrentPackage()) {
            for (const annotationDefinition of environment.getCurrentPackage().getAnnotationDefinitions()) {
                if (annotationDefinition.getName() === annotationDefinitionName) {
                    matchingAnnotationDefintion = annotationDefinition;
                }
            }
        }

        return matchingAnnotationDefintion;
    }

    /**
     * Gets the annotation names as suggestions.
     *
     * @param {PackageScopedEnvironment} environment The ballerina environment.
     * @param {ASTNode} astNode The parent AST node of the annotation-attachment.
     * @param {string} fullPackageName The full package name
     * @param {boolean} allowAnnotationWithNoAttachmentType Include package names with no attachments.
     * @returns {string[]} An array of identifiers
     * @memberof AnnotationHelper
     */
    static getNames(environment, astNode, fullPackageName, allowAnnotationWithNoAttachmentType = true) {
        const annotationIdentifiers = new Set();
        let attachmentType = '';
        if (TreeUtils.isService(astNode)) {
            attachmentType = 'service';
        } else if (TreeUtils.isResource(astNode)) {
            attachmentType = 'resource';
        } else if (TreeUtils.isFunction(astNode)) {
            attachmentType = 'function';
        } else if (TreeUtils.isConnector(astNode)) {
            attachmentType = 'connector';
        } else if (TreeUtils.isAction(astNode)) {
            attachmentType = 'action';
        } else if (TreeUtils.isAnnotation(astNode)) {
            attachmentType = 'annotation';
        } else if (TreeUtils.isStruct(astNode)) {
            attachmentType = 'struct';
        }

        if (fullPackageName !== 'Current Package') {
            for (const packageDefintion of environment.getPackages()) {
                if (packageDefintion.getName() === fullPackageName) {
                    for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                        if (annotationDefinition.getAttachmentPoints().includes(attachmentType)) {
                            annotationIdentifiers.add(annotationDefinition.getName());
                        }

                        if (allowAnnotationWithNoAttachmentType &&
                                                            annotationDefinition.getAttachmentPoints().length === 0) {
                            annotationIdentifiers.add(annotationDefinition.getName());
                        }
                    }
                }
            }
        } else if (environment.getCurrentPackage()) {
            for (const annotationDefinition of environment.getCurrentPackage().getAnnotationDefinitions()) {
                if (annotationDefinition.getAttachmentPoints().includes(attachmentType)) {
                    annotationIdentifiers.add(annotationDefinition.getName());
                }

                if (allowAnnotationWithNoAttachmentType &&
                    annotationDefinition.getAttachmentPoints().length === 0) {
                    annotationIdentifiers.add(annotationDefinition.getName());
                }
            }
        }

        return Array.from(annotationIdentifiers);
    }

    /**
     * Get the supported package names as suggestions
     *
     * @param {PackageScopedEnvironment} environment The ballerina environment.
     * @param {ASTNode} astNode The parent AST node of the annotation-attachment.
     * @param {boolean} allowAnnotationWithNoAttachmentType Include package names with no attachments.
     * @returns {string[]} An array of package names.
     * @memberof AnnotationHelper
     */
    static getPackageNames(environment, astNode, allowAnnotationWithNoAttachmentType = true) {
        const packageNames = new Set();
        let attachmentType = '';
        if (TreeUtils.isService(astNode)) {
            attachmentType = 'service';
        } else if (TreeUtils.isResource(astNode)) {
            attachmentType = 'resource';
        } else if (TreeUtils.isFunction(astNode)) {
            attachmentType = 'function';
        } else if (TreeUtils.isConnector(astNode)) {
            attachmentType = 'connector';
        } else if (TreeUtils.isAction(astNode)) {
            attachmentType = 'action';
        } else if (TreeUtils.isAnnotation(astNode)) {
            attachmentType = 'annotation';
        } else if (TreeUtils.isStruct(astNode)) {
            attachmentType = 'struct';
        }

        for (const packageDefintion of environment.getPackages()) {
            for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                if (annotationDefinition.getAttachmentPoints().includes(attachmentType)) {
                    packageNames.add(packageDefintion.getName());
                }

                if (allowAnnotationWithNoAttachmentType && annotationDefinition.getAttachmentPoints().length === 0) {
                    packageNames.add(packageDefintion.getName());
                }
            }
        }

        if (environment.getCurrentPackage()) {
            for (const annotationDefinition of environment.getCurrentPackage().getAnnotationDefinitions()) {
                if (annotationDefinition.getAttachmentPoints().includes(attachmentType)) {
                    packageNames.add(environment.getCurrentPackage().getName());
                }

                if (allowAnnotationWithNoAttachmentType && annotationDefinition.getAttachmentPoints().length === 0) {
                    packageNames.add(environment.getCurrentPackage().getName());
                }
            }
        }

        return Array.from(packageNames);
    }

    /**
     * Gets attribute definition.
     *
     * @static
     * @param {PackageScopedEnvironment} environment The ballerina environment.
     * @param {string} attributeName The name of the attribute.
     * @param {string} annotationDefinitionFullPackageName The full package name of the annotation definition which the
     * attribute resides.
     * @param {string} annotationDefinitionName The name of the annotation definition which the attribute resides.
     * @returns {AnnotationAttributeDefinition} The matching attribute definition.
     * @memberof AnnotationHelper
     */
    static getAttributeDefinition(environment, attributeName, annotationDefinitionFullPackageName,
                                                                                            annotationDefinitionName) {
        const attributesDefinitions = AnnotationHelper.getAttributes(
                                            environment, annotationDefinitionFullPackageName, annotationDefinitionName);
        return attributesDefinitions.filter((attributeDefinition) => {
            return attributeDefinition.getIdentifier() === attributeName;
        })[0];
    }

    /**
     * Get the annotation attachments - doc annotations for the attributes
     * @param {PackageScopedEnvironment} environment The ballerina environment.
     * @param fullPackageName
     * @param annotationDefinitionName - configuration
     * @returns {Array}
     */
    static getAnnotationAttachments(environment, fullPackageName, annotationDefinitionName) {
        const annotationAttachments = [];
        for (const packageDefintion of environment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationDefinitionName) {
                        for (const annotationAttribute of annotationDefinition.getAnnotationAttachments()) {
                            annotationAttachments.push(annotationAttribute);
                        }
                    }
                }
            }
        }
        return annotationAttachments;
    }

    static resolveFullPackageName(astRoot, alias) {
        let fullPkgName;
        let packageName = [];
        astRoot.getImports().forEach((importNode) => {
            if (importNode.getAlias().getValue() === alias) {
                packageName = importNode.getPackageName().map((packageNameLiteral) => {
                    return packageNameLiteral.getValue();
                });
            }
        });
        if (alias !== 'builtin') {
            fullPkgName = packageName.join('.');
        }
        return fullPkgName;
    }

    static generateDefaultValue(type) {
        if (type === 'string') {
            return '""';
        } else if (type === 'int') {
            return '0';
        } else if (type === 'boolean') {
            return 'false';
        }

        return '';
    }

}

export default AnnotationHelper;

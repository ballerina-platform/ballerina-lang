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

import BallerinaEnvironment from '../environment';

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
     * @param {string} fullPackageName The full package name.
     * @param {string} annotationDefinitionName The annotation definition name.
     * @returns {AnnotationAttributeDefinition[]} List of annotation attribute definitions.
     * @memberof AnnotationHelper
     */
    static getAttributes(fullPackageName, annotationDefinitionName) {
        const annotationAttributes = [];
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
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

        return annotationAttributes;
    }

    /**
     * Gets the annotation definition of a given package name and definition name.
     *
     * @static
     * @param {string} fullPackageName The full package name.
     * @param {string} annotationDefinitionName The annotation definition name.
     * @returns {AnnotationDefinition|undefined} The annotation definition.
     * @memberof AnnotationHelper
     */
    static getAnnotationDefinition(fullPackageName, annotationDefinitionName) {
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationDefinitionName) {
                        return annotationDefinition;
                    }
                }
            }
        }
        return undefined;
    }

    /**
     * Gets the annotation names as suggestions.
     *
     * @param {string} fullPackageName The full package name
     * @returns {string[]} An array of identifiers
     * @memberof AnnotationHelper
     */
    static getNames(fullPackageName) {
        const annotationIdentifiers = [];
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    annotationIdentifiers.push(annotationDefinition.getName());
                }
            }
        }

        return annotationIdentifiers;
    }

    /**
     * Get the supported package names as suggestions
     *
     * @param {string} attachmentType The type of attachment. Example: 'service', 'resource'.
     * @param {boolean} allowAnnotationWithNoAttachmentType Include package names with no attachments.
     * @returns {string[]} An array of package names.
     * @memberof AnnotationHelper
     */
    static getPackageNames(attachmentType, allowAnnotationWithNoAttachmentType = true) {
        const packageNames = new Set();
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                if (annotationDefinition.getAttachmentPoints().includes(attachmentType)) {
                    packageNames.add(packageDefintion.getName());
                }

                if (allowAnnotationWithNoAttachmentType) {
                    packageNames.add(packageDefintion.getName());
                }
            }
        }

        return Array.from(packageNames);
    }

    /**
     * Gets attribute definition.
     *
     * @static
     * @param {string} attributeName The name of the attribute.
     * @param {string} annotationDefinitionFullPackageName The full package name of the annotation definition which the
     * attribute resides.
     * @param {string} annotationDefinitionName The name of the annotation definition which the attribute resides.
     * @returns {AnnotationAttributeDefinition}
     * @memberof AnnotationHelper
     */
    static getAttributeDefinition(attributeName, annotationDefinitionFullPackageName, annotationDefinitionName) {
        const attributesDefinitions = AnnotationHelper.getAttributes(
                                                    annotationDefinitionFullPackageName, annotationDefinitionName);
        return attributesDefinitions.filter((attributeDefinition) => {
            return attributeDefinition.getIdentifier() === attributeName;
        })[0];
    }
}

export default AnnotationHelper;

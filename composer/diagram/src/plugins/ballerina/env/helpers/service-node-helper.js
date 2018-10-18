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
/**
 * A helper class relations ballerina environment annotations.
 *
 * @class AnnotationHelper
 */
class ServiceNodeHelper {

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
    static getAttributes(environment, pkgIdentifier, annotationDefinitionName) {
        const annotationAttributes = [];
        for (const packageDefintion of environment.getPackages()) {
            if (environment.getPackageByIdentifier(pkgIdentifier)) {
                const fullPackageName = environment.getPackageByIdentifier(pkgIdentifier).getName();
                if (packageDefintion.getName() === fullPackageName) {
                    for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                        if (annotationDefinition.getName() === annotationDefinitionName) {
                            for (const annotationAttribute of
                                annotationDefinition.getAnnotationAttributeDefinitions()) {
                                annotationAttributes.push(annotationAttribute);
                            }
                        }
                    }
                }
            }
        }

        return annotationAttributes;
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
    static getAttributeDefinition(environment, attributeName, pkgIdentifier, annotationDefinitionName) {
        const attributesDefinitions = ServiceNodeHelper.getAttributes(
            environment, pkgIdentifier, annotationDefinitionName);
        return attributesDefinitions.filter((attributeDefinition) => {
            return attributeDefinition.getIdentifier() === attributeName;
        })[0];
    }

}

export default ServiceNodeHelper;

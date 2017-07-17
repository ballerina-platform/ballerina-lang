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

import JS_YAML from 'js-yaml';
import _ from 'lodash';
import ASTFactory from '../ballerina/ast/ballerina-ast-factory';
import DefaultBallerinaASTFactory from '../ballerina/ast/default-ballerina-ast-factory';

const HTTP_FULL_PACKAGE = 'ballerina.net.http';
const HTTP_PACKAGE = 'http';
const SWAGGER_FULL_PACKAGE = 'ballerina.net.http.swagger';
const SWAGGER_PACKAGE = 'swagger';

/**
 * This parser class provides means of merging a swagger JSON to a {@link ServiceDefinition} or a
 * {@link ResourceDefinition}.
 */
class SwaggerParser {

    /**
     * @constructs
     * @param {string|object} swaggerDefintiion - The swagger definition as a string. This can be YAML or JSON.
     * @param {boolean} [isYaml=false] is the swagger definition a YAML content or not.
     */
    constructor(swaggerDefintiion, isYaml = false) {
        if (isYaml) {
            this._swaggerJson = JS_YAML.safeLoad(swaggerDefintiion);
        } else {
            this._swaggerJson = swaggerDefintiion;
        }
    }

    /**
     * Merge the {@link _swaggerJson} to service definition.
     * @param {ServiceDefinition} serviceDefinition The service definition.
     * @memberof SwaggerParser
     */
    mergeToService(serviceDefinition) {
        try {
            // Set service name if missing
            if (_.isNil(serviceDefinition.getServiceName())) {
                serviceDefinition.setServiceName(this._swaggerJson.info.title.replace(/[^0-9a-z]/gi, ''));
            }

            // Creating ServiceInfo annotation.
            this.createServiceInfoAnnotation(serviceDefinition);
            // Creating Swagger annotation.
            this.createSwaggerAnnotation(serviceDefinition);
            // Creating ServiceConfig annotation.
            this.createServiceConfigAnnotation(serviceDefinition);
            // Creating @http:config#basePath attribute.
            this.createHttpConfigBasePathAttribute(serviceDefinition);
            // Creating @http:Consumes annotation.
            this.createConsumesAnnotation(serviceDefinition, this._swaggerJson.consumes);
            // Creating @http:Produces annotation.
            this.createProducesAnnotation(serviceDefinition, this._swaggerJson.produces);

            // Updating/Creating resources using path annotation
            _.forEach(this._swaggerJson.paths, (httpMethodObjects, pathString) => {
                _.forEach(httpMethodObjects, (operation, httpMethodAsString) => {
                    let existingResource = serviceDefinition.getResourceDefinitions().find((resourceDefinition) => {
                        const resourceName = resourceDefinition.getResourceName();
                        const operationId = operation.operationId;
                        if (resourceName === operationId) {
                            return true;
                        }
                        return false;
                    });

                    // if the operation id does not match we will check if a resource exist with matching path and
                    // methods.
                    if (_.isNil(existingResource)) {
                        existingResource = serviceDefinition.getResourceDefinitions().find((resourceDefinition) => {
                            const httpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();
                            const pathAnnotation = resourceDefinition.getPathAnnotation(true);
                            return !_.isUndefined(httpMethodAnnotation) && !_.isUndefined(pathAnnotation) &&
                                _.isEqual(pathString, pathAnnotation.getChildren()[0].getRightValue().replace(/"/g, ''))
                                 &&
                                _.isEqual(httpMethodAsString, httpMethodAnnotation.getIdentifier().toLowerCase());
                        });
                        // if operationId exists set it as resource name.
                        if (existingResource && operation.operationId) {
                            existingResource.setResourceName(operation.operationId);
                        }
                    }

                    if (!_.isNil(existingResource)) {
                        this.mergeToResource(existingResource, pathString, httpMethodAsString, operation);
                    } else {
                        this._createNewResource(serviceDefinition, pathString, httpMethodAsString, operation);
                    }
                });
            });
        } catch (err) {
            throw new Error('Unable to parse swagger definition.');
        }
    }

    createHttpConfigBasePathAttribute(serviceDefinition) {
        if (!_.isNil(this._swaggerJson.basePath)) {
            const configAnnotation = SwaggerParser.getAnnotationAttachment(serviceDefinition, HTTP_FULL_PACKAGE,
                HTTP_PACKAGE, 'config');
            const basePathBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.basePath });
            SwaggerParser.setAnnotationAttribute(configAnnotation, 'basePath', basePathBValue);
        }
    }

    createConsumesAnnotation(serviceDefinition, swaggerConsumesDefinition) {
        if (!_.isNil(swaggerConsumesDefinition)) {
            const consumesAnnotation = SwaggerParser.getAnnotationAttachment(serviceDefinition, HTTP_FULL_PACKAGE,
                HTTP_PACKAGE, 'Consumes');
            const consumeBValues = [];
            swaggerConsumesDefinition.forEach((consumeEntry) => {
                consumeBValues.push(ASTFactory.createBValue({ stringValue: consumeEntry }));
            });
            SwaggerParser.addNodesAsArrayedAttribute(consumesAnnotation, 'value', consumeBValues);
        }
    }

    createProducesAnnotation(serviceDefinition, swaggerProducesDefinition) {
        if (!_.isNil(swaggerProducesDefinition)) {
            const producesAnnotation = SwaggerParser.getAnnotationAttachment(serviceDefinition, HTTP_FULL_PACKAGE,
                HTTP_PACKAGE, 'Produces');
            const producesBValues = [];
            swaggerProducesDefinition.forEach((producesEntry) => {
                producesBValues.push(ASTFactory.createBValue({ stringValue: producesEntry }));
            });
            SwaggerParser.addNodesAsArrayedAttribute(producesAnnotation, 'value', producesBValues);
        }
    }

    /**
     * Creates the @ServiceInfo annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     */
    createServiceInfoAnnotation(serviceDefinition) {
        const serviceInfoAnnotation = ASTFactory.createAnnotationAttachment({
            fullPackageName: SWAGGER_FULL_PACKAGE,
            packageName: SWAGGER_PACKAGE,
            name: 'ServiceInfo',
        });

        if (!_.isNil(this._swaggerJson.info.title)) {
            const titleBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.info.title });
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'title', titleBValue);
        }

        if (!_.isNil(this._swaggerJson.info.version)) {
            const versionBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.info.version });
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'version', versionBValue);
        }

        if (!_.isNil(this._swaggerJson.info.description)) {
            const descriptionBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.info.description });
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'description', descriptionBValue);
        }

        if (!_.isNil(this._swaggerJson.info.termsOfService)) {
            const termOfServiceBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.info.termsOfService });
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'termsOfService', termOfServiceBValue);
        }

        if (!_.isNil(this._swaggerJson.info.contact)) {
            const contactAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: SWAGGER_FULL_PACKAGE,
                packageName: SWAGGER_PACKAGE,
                name: 'Contact',
            });

            if (!_.isNil(this._swaggerJson.info.contact.name)) {
                const nameBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.contact.name });
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'name', nameBValue);
            }

            if (!_.isNil(this._swaggerJson.info.contact.url)) {
                const urlBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.contact.url });
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'url', urlBValue);
            }

            if (!_.isNil(this._swaggerJson.info.contact.email)) {
                const emailBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.contact.email });
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'email', emailBValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'contact', contactAnnotation);
        }

        if (!_.isNil(this._swaggerJson.info.license)) {
            const licenseAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: SWAGGER_FULL_PACKAGE,
                packageName: SWAGGER_PACKAGE,
                name: 'License',
            });

            if (!_.isNil(this._swaggerJson.info.license.name)) {
                const nameBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.info.license.name });
                SwaggerParser.setAnnotationAttribute(licenseAnnotation, 'name', nameBValue);
            }

            if (!_.isNil(this._swaggerJson.info.license.url)) {
                const urlBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.info.license.url });
                SwaggerParser.setAnnotationAttribute(licenseAnnotation, 'url', urlBValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'license', licenseAnnotation);
        }

        // TODO : Create externalDocs, tag, organization, developers.

        const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
        const serviceInfoAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
            SWAGGER_PACKAGE, 'ServiceInfo');
        serviceDefinition.addChild(serviceInfoAnnotation, serviceInfoAnnotationIndex);
    }

    /**
     * Creates the @Swagger annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     */
    createSwaggerAnnotation(serviceDefinition) {
        const swaggerAnnotation = ASTFactory.createAnnotationAttachment({
            fullPackageName: SWAGGER_FULL_PACKAGE,
            packageName: SWAGGER_PACKAGE,
            name: 'Swagger',
        });

        if (!_.isNil(this._swaggerJson.swagger)) {
            const versionBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.swagger });
            SwaggerParser.setAnnotationAttribute(swaggerAnnotation, 'version', versionBValue);
        }

        const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
        const swaggerAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
            SWAGGER_PACKAGE, 'Swagger');
        serviceDefinition.addChild(swaggerAnnotation, swaggerAnnotationIndex);
    }

    /**
     * Creates the @ServiceConfig annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     */
    createServiceConfigAnnotation(serviceDefinition) {
        const serviceConfigAnnotation = ASTFactory.createAnnotationAttachment({
            fullPackageName: SWAGGER_FULL_PACKAGE,
            packageName: SWAGGER_PACKAGE,
            name: 'ServiceConfig',
        });

        if (!_.isNil(this._swaggerJson.host)) {
            const hostBValue = ASTFactory.createBValue({ stringValue: this._swaggerJson.host });
            SwaggerParser.setAnnotationAttribute(serviceConfigAnnotation, 'host', hostBValue);
        }

        if (!_.isNil(this._swaggerJson.schemes) && this._swaggerJson.schemes.length > 0) {
            const schemeBValues = [];
            this._swaggerJson.schemes.forEach((schemeEntry) => {
                schemeBValues.push(ASTFactory.createBValue({ stringValue: schemeEntry }));
            });
            SwaggerParser.addNodesAsArrayedAttribute(serviceConfigAnnotation, 'schemes', schemeBValues);
        }

        // TODO: Authorization attribute.

        const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
        const serviceConfigAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
            SWAGGER_PACKAGE, 'ServiceConfig');
        serviceDefinition.addChild(serviceConfigAnnotation, serviceConfigAnnotationIndex);
    }

    /**
     * Merge a swagger json object of the http method to a {@link ResourceDefinition}.
     * @param {ResourceDefinition} resourceDefinition The resource definition to be merged with.
     * @param {string} pathString The @http:Path{} value
     * @param {string} httpMethodAsString The http method of the resource. Example: @http:GET{}
     * @param {object} httpMethodJsonObject The http method object of the swagger json.
     * @private
     */
    mergeToResource(resourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        // Creating @Path annotation.
        this.createPathAnnotation(resourceDefinition, pathString);
        // Creating the http method annotation.
        this.createHttpMethodAnnotation(resourceDefinition, httpMethodAsString);
        // Creating @Consumes annotation.
        this.createConsumesAnnotation(resourceDefinition, httpMethodJsonObject.consumes);
        // Creating @Produces annotation.
        this.createProducesAnnotation(resourceDefinition, httpMethodJsonObject.produces);
        // Creating parameter definitions for the resource definition.
        this.createParameterDefs(resourceDefinition, httpMethodJsonObject.parameters);
        // Creating @ParametersInfo annotation.
        this.createParametersInfoAnnotation(resourceDefinition, httpMethodJsonObject);
        // Creating @ResourceConfig annotation.
        this.createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject);
        // Creating @Responses annotation.
        this.createResponsesAnnotation(resourceDefinition, httpMethodJsonObject);
    }

    createPathAnnotation(resourceDefinition, pathString) {
        const pathAnnotation = ASTFactory.createAnnotationAttachment({
            fullPackageName: HTTP_FULL_PACKAGE,
            packageName: HTTP_PACKAGE,
            name: 'Path',
        });

        if (!_.isNil(pathString)) {
            const pathBValue = ASTFactory.createBValue({ stringValue: pathString });
            SwaggerParser.setAnnotationAttribute(pathAnnotation, 'value', pathBValue);
        }

        const resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
        const pathAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
            HTTP_PACKAGE, 'Path');
        resourceDefinition.addChild(pathAnnotation, pathAnnotationIndex);
    }

    createHttpMethodAnnotation(resourceDefinition, httpMethodAsString) {
        const methodAnnotation = resourceDefinition.getHttpMethodAnnotation();
        if (!_.isNil(methodAnnotation)) {
            methodAnnotation.setName(httpMethodAsString.toUpperCase());
        } else {
            const httpMethodAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: HTTP_FULL_PACKAGE,
                packageName: HTTP_PACKAGE,
                name: httpMethodAsString,
            });
            resourceDefinition.addChild(httpMethodAnnotation);
        }
    }

    createParameterDefs(resourceDefinition, swaggerParameters) {
        // Removing existing parameter definitions except for the first one(message m).
        const parameterDefinitions = resourceDefinition.getArgumentParameterDefinitionHolder().getChildren();
        parameterDefinitions.forEach((parameterDefinition, index) => {
            if (index !== 0) {
                resourceDefinition.getArgumentParameterDefinitionHolder().removeChild(parameterDefinition);
            }
        });

        swaggerParameters.forEach((swaggerParameter) => {
            const newParameterDefinition = ASTFactory.createParameterDefinition();
            if (swaggerParameter.type === 'number' || swaggerParameter.type === 'integer') {
                newParameterDefinition.setTypeName('int');
            } else {
                newParameterDefinition.setTypeName(swaggerParameter.type);
            }
            newParameterDefinition.setName(swaggerParameter.name);

            // Creating parameter annotation.
            const paramAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: HTTP_FULL_PACKAGE,
                packageName: HTTP_PACKAGE,
            });

            const nameBValue = ASTFactory.createBValue({ stringValue: swaggerParameter.name });
            SwaggerParser.setAnnotationAttribute(paramAnnotation, 'value', nameBValue);

            if (swaggerParameter.in === 'query') {
                paramAnnotation.setName('QueryParam');
            } else if (swaggerParameter.in === 'path') {
                paramAnnotation.setName('PathParam');
            }

            newParameterDefinition.addChild(paramAnnotation);
            resourceDefinition.getArgumentParameterDefinitionHolder().addChild(newParameterDefinition);
        });
    }

    /**
     * Creates the @ParametersInfo annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     *
     * @param {ResourceDefinition} resourceDefinition The resource definition to be updated.
     * @param {Object} httpMethodJsonObject The http method json object of the swagger json.
     *
     * @memberof SwaggerParser
     */
    createParametersInfoAnnotation(resourceDefinition, httpMethodJsonObject) {
        if (!_.isNil(httpMethodJsonObject.parameters) && httpMethodJsonObject.parameters.length > 0) {
            const parametersInfoAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: SWAGGER_FULL_PACKAGE,
                packageName: SWAGGER_PACKAGE,
                name: 'ParametersInfo',
            });

            const parameterInfoAnnotations = [];
            httpMethodJsonObject.parameters.forEach((parameter) => {
                const responseAnnotation = ASTFactory.createAnnotationAttachment({
                    fullPackageName: SWAGGER_FULL_PACKAGE,
                    packageName: SWAGGER_PACKAGE,
                    name: 'ParameterInfo',
                });

                if (!_.isNil(parameter.in)) {
                    const inBValue = ASTFactory.createBValue({ stringValue: parameter.in });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'in', inBValue);
                }

                if (!_.isNil(parameter.name)) {
                    const nameBValue = ASTFactory.createBValue({ stringValue: parameter.name });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'name', nameBValue);
                }

                if (!_.isNil(parameter.description)) {
                    const descriptionBValue = ASTFactory.createBValue({ stringValue: parameter.description });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'description', descriptionBValue);
                }

                if (!_.isNil(parameter.required)) {
                    const requiredBValue = ASTFactory.createBValue({ type: 'boolean',
                        stringValue: parameter.required });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'required', requiredBValue);
                }

                if (!_.isNil(parameter.allowEmptyValue)) {
                    const allowEmptyValueBValue = ASTFactory.createBValue({ type: 'boolean',
                        stringValue: parameter.allowEmptyValue });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'allowEmptyValue', allowEmptyValueBValue);
                }

                if (!_.isNil(parameter.type)) {
                    const typeBValue = ASTFactory.createBValue({ stringValue: parameter.type });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'type', typeBValue);
                }

                if (!_.isNil(parameter.format)) {
                    const formatBValue = ASTFactory.createBValue({ stringValue: parameter.format });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'format', formatBValue);
                }

                if (!_.isNil(parameter.collectionFormat)) {
                    const collectionFormatBValue = ASTFactory.createBValue({
                        stringValue: parameter.collectionFormat,
                    });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'collectionFormat',
                        collectionFormatBValue);
                }

                parameterInfoAnnotations.push(responseAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(parametersInfoAnnotation, 'value', parameterInfoAnnotations);

            const resourceDefinitionAnnotations =
                resourceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
            const parametersInfoAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                SWAGGER_PACKAGE, 'ParametersInfo');
            resourceDefinition.addChild(parametersInfoAnnotation, parametersInfoAnnotationIndex);
        }
    }

    /**
     * Creates the @ResourceConfig annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     */
    createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject) {
        const resourceConfigAnnotation = resourceDefinition.getFactory().createAnnotationAttachment({
            fullPackageName: SWAGGER_FULL_PACKAGE,
            packageName: SWAGGER_PACKAGE,
            name: 'ResourceConfig',
        });

        if (!_.isNil(httpMethodJsonObject.schemes) && httpMethodJsonObject.schemes.length > 0) {
            const schemeBValues = [];
            this._swaggerJson.schemes.forEach((schemeEntry) => {
                schemeBValues.push(ASTFactory.createBValue({ stringValue: schemeEntry }));
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'schemes', schemeBValues);
        }

        // TODO : Implement authorization.

        const resourceDefinitionAnnotations =
                                        resourceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
        const resourceConfigAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                    SWAGGER_PACKAGE, 'ResourceConfig');
        resourceDefinition.addChild(resourceConfigAnnotation, resourceConfigAnnotationIndex);
    }

    /**
     * Creates the @Responses annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     */
    createResponsesAnnotation(resourceDefinition, httpMethodJsonObject) {
        if (!_.isNil(httpMethodJsonObject.responses)) {
            const responsesAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: SWAGGER_FULL_PACKAGE,
                packageName: SWAGGER_PACKAGE,
                name: 'Responses',
            });

            const responseAnnotations = [];
            _.forEach(httpMethodJsonObject.responses, (codeObj, code) => {
                const responseAnnotation = ASTFactory.createAnnotationAttachment({
                    fullPackageName: SWAGGER_FULL_PACKAGE,
                    packageName: SWAGGER_PACKAGE,
                    name: 'Response',
                });

                if (!_.isNil(code)) {
                    const codeBValue = ASTFactory.createBValue({ stringValue: code });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'code', codeBValue);
                }

                if (!_.isNil(code)) {
                    const descriptionBValue = ASTFactory.createBValue({ stringValue: codeObj.description });
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'description', descriptionBValue);
                }
                responseAnnotations.push(responseAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(responsesAnnotation, 'value', responseAnnotations);

            const resourceDefinitionAnnotations =
                resourceDefinition.getChildrenOfType(ASTFactory.isAnnotationAttachment);
            const resourceConfigAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                        SWAGGER_PACKAGE, 'Responses');
            resourceDefinition.addChild(responsesAnnotation, resourceConfigAnnotationIndex);
        }
    }

    /**
     * Creates a new {@link ResourceDefinition} with a given @http:Path value and an http method annotation.
     * @param {ServiceDefinition} serviceDefinition The service definition to which the resource definition should be
     * added to.
     * @param {string} pathString The @http:Path value.
     * @param {string} httpMethodAsString The http method value.
     * @param {object} httpMethodJsonObject http method object of the swagger JSON.
     * @private
     */
    _createNewResource(serviceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        const newResourceDefinition = DefaultBallerinaASTFactory.createResourceDefinition();

        // if an operation id is defined set it as resource name.
        if (httpMethodJsonObject.operationId) {
            newResourceDefinition.setResourceName(httpMethodJsonObject.operationId);
        } else {
            newResourceDefinition.setResourceName(pathString.replace(/\W/g, '') + httpMethodAsString.toUpperCase());
        }

        this.mergeToResource(newResourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject);
        serviceDefinition.addChild(newResourceDefinition);
        newResourceDefinition.generateUniqueIdentifiers();
    }

    /**
     * Removed an existing annotation from a given list of annotations.
     * @param {Annotation[]} existingAnnotations The list of annotation to be checked through.
     * @param {string} annotationPackage The package of the annotation to be removed.
     * @param {string} annotationIdentifier The identifier of the annotation to be removed.
     * @return {number} The removed annotation index.
     * @private
     */
    static removeExistingAnnotation(existingAnnotations, annotationPackage, annotationIdentifier) {
        let removedChildIndex = _.size(existingAnnotations);
        _.forEach(existingAnnotations, (existingAnnotation) => {
            if (_.isEqual(existingAnnotation.getPackageName(), annotationPackage) &&
                _.isEqual(existingAnnotation.getName(), annotationIdentifier)) {
                removedChildIndex = existingAnnotation.getParent().getIndexOfChild(existingAnnotation);
                existingAnnotation.getParent().removeChild(existingAnnotation);
                return false;
            }

            return true;
        });

        return removedChildIndex;
    }

    static getAnnotationAttachment(astNode, fullPackageName, packageName, name) {
        const matchingAttachments = astNode.getChildrenOfType(ASTFactory.isAnnotationAttachment)
        .filter((annotationAttachment) => {
            return annotationAttachment.getFullPackageName() === fullPackageName &&
                    annotationAttachment.getName() === name;
        });

        if (matchingAttachments.length > 0) {
            return matchingAttachments[0];
        } else {
            const newAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName,
                packageName,
                name,
            });
            astNode.addChild(newAnnotation);
            return newAnnotation;
        }
    }

    static setAnnotationAttribute(annotationAttachment, key, valueAST) {
        const existingAttributes = annotationAttachment.getChildrenOfType(ASTFactory.isAnnotationAttribute);
        const matchingAttributes = existingAttributes.filter((annotationAttribute) => {
            return annotationAttribute.getKey() === key;
        });

        if (matchingAttributes.length > 0) {
            const attributeValue = matchingAttributes[0].getValue();
            attributeValue.removeAllChildren();
            attributeValue.addChild(valueAST);
        } else {
            const value = ASTFactory.createAnnotationAttributeValue();
            value.addChild(valueAST);
            const attribute = ASTFactory.createAnnotationAttribute({ key });
            attribute.addChild(value);
            annotationAttachment.addChild(attribute);
        }
    }

    static addNodesAsArrayedAttribute(annotationAttachment, key, astNodes) {
        const existingAttributes = annotationAttachment.getChildrenOfType(ASTFactory.isAnnotationAttribute);
        const matchingAttributes = existingAttributes.filter((annotationAttribute) => {
            return annotationAttribute.getKey() === key;
        });

        if (matchingAttributes.length > 0) {
            const attributeValue = matchingAttributes[0].getValue();
            // Removing existing array elements.
            attributeValue.removeAllChildren();

            astNodes.forEach((ast) => {
                const arrayItem = ASTFactory.annotationAttributeValue();
                arrayItem.addChild(ast);
                attributeValue.addChild(arrayItem);
            });
        } else {
            const value = ASTFactory.createAnnotationAttributeValue();
            astNodes.forEach((ast) => {
                const arrayItem = ASTFactory.annotationAttributeValue();
                arrayItem.addChild(ast);
                value.addChild(arrayItem);
            });

            const attribute = ASTFactory.createAnnotationAttribute({ key });
            attribute.addChild(value);
            annotationAttachment.addChild(attribute);
        }
    }
}

export default SwaggerParser;

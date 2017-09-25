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
import log from 'log';
import NodeFactory from 'ballerina/model/node-factory';
import DefaultNodeFactory from 'ballerina/model/default-node-factory';

let httpAlias = 'http';
let swaggerAlias = 'swagger';
const DISABLE_EVENT_FIRING = true;

/**
 * This parser class provides means of merging a swagger JSON to a {@link ServiceDefinition} or a
 * {@link ResourceDefinition}.
 */
class SwaggerParser {

    /**
     * @constructs
     * @param {string|object} swaggerDefintiion - The swagger definition as a string. This can be YAML or JSON.
     * @param {string} httpPackageAlias The alias used for the ballerina.net.http package.
     * @param {string} swaggerPackageAlias The alias used for the ballerina.net.http.swagger package.
     * @param {boolean} [isYaml=false] is the swagger definition a YAML content or not.
     */
    constructor(swaggerDefintiion, httpPackageAlias = 'http', swaggerPackageAlias = 'swagger', isYaml = false) {
        if (isYaml) {
            this._swaggerJson = JS_YAML.safeLoad(swaggerDefintiion);
        } else {
            this._swaggerJson = swaggerDefintiion;
        }

        httpAlias = httpPackageAlias;
        swaggerAlias = swaggerPackageAlias;
    }

    /**
     * Merge the {@link _swaggerJson} to service definition.
     * @param {ServiceNode} serviceDefinition The service definition.
     * @memberof SwaggerParser
     */
    mergeToService(serviceDefinition) {
        try {
            // Set service name if missing
            if (_.isNil(serviceDefinition.getServiceName())) {
                serviceDefinition.setName(this._swaggerJson.info.title.replace(/[^0-9a-z]/gi, ''), true);
            }

            // Creating ServiceInfo annotation.
            this.createServiceInfoAnnotation(serviceDefinition);
            // Creating Swagger annotation.
            this.createSwaggerAnnotation(serviceDefinition);
            // Creating ServiceConfig annotation.
            this.createServiceConfigAnnotation(serviceDefinition);
            // Creating @http:config annotation.
            this.createHttpConfigAnnotation(serviceDefinition);
            // Creating @http:Consumes annotation.
            // this.createConsumesAnnotation(serviceDefinition, this._swaggerJson.consumes);
            // Creating @http:Produces annotation.
            // this.createProducesAnnotation(serviceDefinition, this._swaggerJson.produces);

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
                        if (httpMethodAsString === 'x-MULTI') {
                            const xMultiObj = operation;
                            existingResource = serviceDefinition.getResourceDefinitions().find((resourceDefinition) => {
                                const httpMethods = resourceDefinition.getHttpMethodValues();
                                let hasSameHttpMethods = false;
                                if (httpMethods.length === xMultiObj['x-METHODS'].length) {
                                    hasSameHttpMethods =
                                    _.intersectionBy(httpMethods, xMultiObj['x-METHODS'], String.toLowerCase).length ===
                                     httpMethods.length;
                                }

                                const pathValue = resourceDefinition.getPathAnnotationValue();
                                return httpMethods.length > 0 && !_.isNil(pathValue) &&
                                    _.isEqual(pathString, pathValue.replace(/"/g, ''))
                                    && hasSameHttpMethods;
                            });
                            // if operationId exists set it as resource name.
                            if (existingResource && xMultiObj.operationId && xMultiObj.operationId.trim() !== '') {
                                existingResource.setResourceName(xMultiObj.operationId.replace(/\s/g, ''));
                            }
                        } else {
                            existingResource = serviceDefinition.getResourceDefinitions().find((resourceDefinition) => {
                                const httpMethods = resourceDefinition.getHttpMethodValues();
                                const pathValue = resourceDefinition.getPathAnnotationValue();
                                return httpMethods.length > 0 && !_.isNil(pathValue) &&
                                    _.isEqual(pathString, pathValue.replace(/"/g, ''))
                                    &&
                                    _.isEqual(httpMethodAsString, httpMethods[0].toLowerCase());
                            });
                            // if operationId exists set it as resource name.
                            if (existingResource && operation.operationId && operation.operationId.trim() !== '') {
                                existingResource.setResourceName(operation.operationId.replace(/\s/g, ''));
                            }
                        }
                    }

                    if (!_.isNil(existingResource)) {
                        this.mergeToResource(existingResource, pathString, httpMethodAsString, operation);
                    } else {
                        this.createNewResource(serviceDefinition, pathString, httpMethodAsString, operation);
                    }
                });
            });
        } catch (err) {
            log.error(err);
            throw new Error('Unable to parse swagger definition.');
        }
    }

    /**
     * Creates/Updates the @http:config annotation.
     *
     * @param {ServiceNode} serviceDefinition The service definition which has the annotation attachment.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createHttpConfigAnnotation(serviceDefinition, silent = DISABLE_EVENT_FIRING) {
        const configAnnotation = SwaggerParser.createAnnotationAttachment(httpAlias, 'configuration');
        if (!_.isNil(this._swaggerJson.basePath)) {
            const basePathValue = NodeFactory.createLiteral();
            basePathValue.setValueAsString(this._swaggerJson.basePath);
            SwaggerParser.setAnnotationAttribute(configAnnotation, 'basePath', basePathValue);
        }

        if (!_.isNil(this._swaggerJson.host)) {
            const hostAndPort = this._swaggerJson.host.split(':');

            const hostBValue = NodeFactory.createLiteral();
            hostBValue.setValueAsString(hostAndPort[0]);
            SwaggerParser.setAnnotationAttribute(configAnnotation, 'host', hostBValue);

            const portBValue = NodeFactory.createLiteral();
            portBValue.setValue(hostAndPort[1]);
            SwaggerParser.setAnnotationAttribute(configAnnotation, 'port', portBValue);
        }

        const existingConfigurationAnnotation = serviceDefinition.filterAnnotationAttachments(
            (annotationAttachment) => {
                return annotationAttachment.getAnnotationName() === `${httpAlias}:configuration`;
            });
        if (existingConfigurationAnnotation.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingConfigurationAnnotation[0], configAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(configAnnotation, silent);
        }
    }

    /**
     * Creates @http:Consumes annotation for a given ast.
     *
     * @param {ServiceNode|ResourceNode} astNode The service or resource defintion.
     * @param {string[]} swaggerConsumesDefinitions The consumes values.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createConsumesAnnotation(astNode, swaggerConsumesDefinitions, silent = DISABLE_EVENT_FIRING) {
        if (!_.isNil(swaggerConsumesDefinitions) && swaggerConsumesDefinitions.length > 0) {
            const consumesAnnotation = SwaggerParser.createAnnotationAttachment(httpAlias, 'Consumes');
            const consumeValues = [];
            swaggerConsumesDefinitions.forEach((consumeEntry) => {
                const consumesValue = NodeFactory.createLiteral();
                consumesValue.setValueAsString(consumeEntry);
                consumeValues.push(consumesValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(consumesAnnotation, 'value', consumeValues);

            const existingConsumesAnnotation = astNode.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getAnnotationName() === `${httpAlias}:Consumes`;
            });
            if (existingConsumesAnnotation.length > 0) {
                astNode.replaceAnnotationAttachments(existingConsumesAnnotation[0], consumesAnnotation);
            } else {
                astNode.addAnnotationAttachments(consumesAnnotation, silent);
            }
        }
    }

    /**
     * Creates @http:Produces annotation for a given ast.
     *
     * @param {ServiceNode|ResourceNode} astNode The service or resource defintion.
     * @param {string[]} swaggerProducesDefinitions The produces values.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createProducesAnnotation(astNode, swaggerProducesDefinitions, silent = DISABLE_EVENT_FIRING) {
        if (!_.isNil(swaggerProducesDefinitions) && swaggerProducesDefinitions.length > 0) {
            const producesAnnotation = SwaggerParser.createAnnotationAttachment(httpAlias, 'Produces');
            const producesValues = [];
            swaggerProducesDefinitions.forEach((producesEntry) => {
                const producesValue = NodeFactory.createLiteral();
                producesValue.setValueAsString(producesEntry);
                producesValues.push(producesValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(producesAnnotation, 'value', producesValues);

            const existingProducesAnnotation = astNode.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getAnnotationName() === `${httpAlias}:Produces`;
            });
            if (existingProducesAnnotation.length > 0) {
                astNode.replaceAnnotationAttachments(existingProducesAnnotation[0], producesAnnotation);
            } else {
                astNode.addAnnotationAttachments(producesAnnotation, silent);
            }
        }
    }

    /**
     * Creates the @ServiceInfo annotation for a given {@link ServiceNode} using the {@link _swaggerJson}.
     * @param {ServiceNode} serviceDefinition The service definition
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     */
    createServiceInfoAnnotation(serviceDefinition, silent = DISABLE_EVENT_FIRING) {
        const serviceInfoAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ServiceInfo');

        if (!_.isNil(this._swaggerJson.info.title)) {
            const titleValue = NodeFactory.createLiteral();
            titleValue.setValueAsString(this._swaggerJson.info.title);
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'title', titleValue);
        }

        if (!_.isNil(this._swaggerJson.info.version)) {
            const versionValue = NodeFactory.createLiteral();
            versionValue.setValueAsString(this._swaggerJson.info.version);
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'version', versionValue);
        }

        if (!_.isNil(this._swaggerJson.info.description)) {
            const descriptionValue = NodeFactory.createLiteral();
            descriptionValue.setValueAsString(this._swaggerJson.info.description);
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'description', descriptionValue);
        }

        if (!_.isNil(this._swaggerJson.info.termsOfService)) {
            const termOfServiceValue = NodeFactory.createLiteral();
            termOfServiceValue.setValueAsString(this._swaggerJson.info.termsOfService);
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'termsOfService', termOfServiceValue);
        }

        if (!_.isNil(this._swaggerJson.info.contact)) {
            const contactAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Contact');
            if (!_.isNil(this._swaggerJson.info.contact.name)) {
                const nameValue = NodeFactory.createLiteral();
                nameValue.setValueAsString(this._swaggerJson.info.contact.name);
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'name', nameValue);
            }

            if (!_.isNil(this._swaggerJson.info.contact.url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValueAsString(this._swaggerJson.info.contact.url);
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'url', urlValue);
            }

            if (!_.isNil(this._swaggerJson.info.contact.email)) {
                const emailValue = NodeFactory.createLiteral();
                emailValue.setValueAsString(this._swaggerJson.info.contact.email);
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'email', emailValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'contact', contactAnnotation);
        }

        if (!_.isNil(this._swaggerJson.info.license)) {
            const licenseAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'License');

            if (!_.isNil(this._swaggerJson.info.license.name)) {
                const nameValue = NodeFactory.createLiteral();
                nameValue.setValueAsString(this._swaggerJson.info.license.name);
                SwaggerParser.setAnnotationAttribute(licenseAnnotation, 'name', nameValue);
            }

            if (!_.isNil(this._swaggerJson.info.license.url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValueAsString(this._swaggerJson.info.license.url);
                SwaggerParser.setAnnotationAttribute(licenseAnnotation, 'url', urlValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'license', licenseAnnotation);
        }

        if (!_.isNil(this._swaggerJson.externalDocs)) {
            const externalDocsAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ExternalDoc');

            if (!_.isNil(this._swaggerJson.externalDocs.description)) {
                const descriptionValue = NodeFactory.createLiteral();
                descriptionValue.setValueAsString(this._swaggerJson.externalDocs.description);
                SwaggerParser.setAnnotationAttribute(externalDocsAnnotation, 'description', descriptionValue);
            }

            if (!_.isNil(this._swaggerJson.externalDocs.url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValueAsString(this._swaggerJson.externalDocs.url);
                SwaggerParser.setAnnotationAttribute(externalDocsAnnotation, 'url', urlValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'externalDocs', externalDocsAnnotation);
        }

        if (!_.isNil(this._swaggerJson.info['x-organization'])) {
            const organizationAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Organization');

            if (!_.isNil(this._swaggerJson.info['x-organization'].name)) {
                const nameValue = NodeFactory.createLiteral();
                nameValue.setValueAsString(this._swaggerJson.info['x-organization'].name);
                SwaggerParser.setAnnotationAttribute(organizationAnnotation, 'name', nameValue);
            }

            if (!_.isNil(this._swaggerJson.info['x-organization'].url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValueAsString(this._swaggerJson.info['x-organization'].url);
                SwaggerParser.setAnnotationAttribute(organizationAnnotation, 'url', urlValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'organization', organizationAnnotation);
        }

        if (!_.isNil(this._swaggerJson.info['x-developers']) && this._swaggerJson.info['x-developers'].length > 0) {
            const developerBValues = [];
            this._swaggerJson.info['x-developers'].forEach((developer) => {
                const developerAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Developer');

                if (!_.isNil(developer.name)) {
                    const nameValue = NodeFactory.createLiteral();
                    nameValue.setValueAsString(developer.name);
                    SwaggerParser.setAnnotationAttribute(developerAnnotation, 'name', nameValue);
                }

                if (!_.isNil(developer.email)) {
                    const emailValue = NodeFactory.createLiteral();
                    emailValue.setValueAsString(developer.email);
                    SwaggerParser.setAnnotationAttribute(developerAnnotation, 'email', emailValue);
                }
                developerBValues.push(developerAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(serviceInfoAnnotation, 'developers', developerBValues);
        }

        if (!_.isNil(this._swaggerJson.tags) && this._swaggerJson.tags.length > 0) {
            const tagBValues = [];
            this._swaggerJson.tags.forEach((tag) => {
                const tagAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Tag');

                if (!_.isNil(tag.name)) {
                    const nameValue = NodeFactory.createLiteral();
                    nameValue.setValueAsString(tag.name);
                    SwaggerParser.setAnnotationAttribute(tagAnnotation, 'name', nameValue);
                }

                if (!_.isNil(tag.description)) {
                    const descriptionValue = NodeFactory.createLiteral();
                    descriptionValue.setValueAsString(tag.description);
                    SwaggerParser.setAnnotationAttribute(tagAnnotation, 'description', descriptionValue);
                }
                tagBValues.push(tagAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(serviceInfoAnnotation, 'tags', tagBValues);
        }

        const existingServiceInfoAnnotation = serviceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getAnnotationName() === `${swaggerAlias}:ServiceInfo`;
        });
        if (existingServiceInfoAnnotation.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingServiceInfoAnnotation[0], serviceInfoAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(serviceInfoAnnotation, silent);
        }
    }

    /**
     * Creates the @Swagger annotation for a given {@link ServiceNode} using the {@link _swaggerJson}.
     * @param {ServiceNode} serviceDefinition The service definition
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     */
    createSwaggerAnnotation(serviceDefinition, silent = DISABLE_EVENT_FIRING) {
        const swaggerAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Swagger');

        if (!_.isNil(this._swaggerJson.swagger)) {
            const versionValue = NodeFactory.createLiteral();
            versionValue.setValueAsString(this._swaggerJson.swagger);
            SwaggerParser.setAnnotationAttribute(swaggerAnnotation, 'version', versionValue);
        }

        const existingSwaggerAnnotation = serviceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getAnnotationName() === `${swaggerAlias}:Swagger`;
        });
        if (existingSwaggerAnnotation.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingSwaggerAnnotation[0], swaggerAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(swaggerAnnotation, silent);
        }
    }

    /**
     * Creates the @ServiceConfig annotation for a given {@link ServiceNode} using the {@link _swaggerJson}.
     * @param {ServiceNode} serviceDefinition The service definition
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     */
    createServiceConfigAnnotation(serviceDefinition, silent = DISABLE_EVENT_FIRING) {
        const serviceConfigAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ServiceConfig');

        if (!_.isNil(this._swaggerJson.host)) {
            let hostValue;
            if (!_.isNil(this._swaggerJson.schemes) && this._swaggerJson.schemes.length > 0) {
                hostValue.setValueAsString(`${this._swaggerJson.schemes[0]}://${this._swaggerJson.host}`);
            } else {
                hostValue.setValueAsString(`http://:${this._swaggerJson.host}`);
            }
            SwaggerParser.setAnnotationAttribute(serviceConfigAnnotation, 'host', hostValue);
        }

        if (!_.isNil(this._swaggerJson.schemes) && this._swaggerJson.schemes.length > 0) {
            const schemeBValues = [];
            this._swaggerJson.schemes.forEach((schemeEntry) => {
                const schemeValue = NodeFactory.createLiteral();
                schemeValue.setValueAsString(schemeEntry);
                schemeBValues.push(schemeValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(serviceConfigAnnotation, 'schemes', schemeBValues);
        }

        // TODO: Authorization attribute.
        const existingServiceConfig = serviceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getAnnotationName() === `${swaggerAlias}:ServiceConfig`;
        });
        if (existingServiceConfig.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingServiceConfig[0], serviceConfigAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(serviceConfigAnnotation, silent);
        }
    }

    /**
     * Merge a swagger json object of the http method to a {@link ResourceNode}.
     * @param {ResourceNode} resourceDefinition The resource definition to be merged with.
     * @param {string} pathString The @http:Path{} value
     * @param {string} httpMethodAsString The http method of the resource. Example: @http:GET{}
     * @param {object} httpMethodJsonObject The http method object of the swagger json.
     */
    mergeToResource(resourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        // Creating @http:resourceConfig annotation.
        this.createHttpResourceConfigAnnotation(resourceDefinition, pathString, httpMethodAsString,
            httpMethodJsonObject);
        // Creating @ResourceConfig annotation.
        this.createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject);
        // Creating parameter definitions for the resource definition.
        this.createParameterDefs(resourceDefinition, httpMethodJsonObject.parameters);
        // Creating @ParametersInfo annotation.
        this.createParametersInfoAnnotation(resourceDefinition, httpMethodJsonObject);
        // Creating @ResourceConfig annotation.
        this.createResourceInfoAnnotation(resourceDefinition, httpMethodJsonObject);
        // Creating @Responses annotation.
        this.createResponsesAnnotation(resourceDefinition, httpMethodJsonObject);
    }

    /**
     * Creates the @http:Path annotation.
     *
     * @param {ResourceNode} resourceDefinition The resource definition to be updated.
     * @param {string} pathString The path value.
     * @param {string} httpMethodAsString The http method.
     * @param {Object} httpMethodJsonObject The swagger operation for the http method.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createHttpResourceConfigAnnotation(resourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject,
            silent = DISABLE_EVENT_FIRING) {
        const resourceConfigAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'resourceConfig');

        const httpMethodsBValues = [];
        if (httpMethodAsString !== 'x-MULTI') {
            const singleHttpMethod = NodeFactory.createLiteral();
            singleHttpMethod.setValueAsString(httpMethodAsString.toUpperCase());
            httpMethodsBValues.push(singleHttpMethod);
        } else {
            httpMethodJsonObject['x-METHODS'].forEach((httpMethod) => {
                const httpMethod = NodeFactory.createLiteral();
                httpMethod.setValueAsString(httpMethod.toUpperCase());
                httpMethodsBValues.push(httpMethod);
            });
        }
        SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'methods', httpMethodsBValues);

        if (!_.isNil(pathString)) {
            const pathBValue = NodeFactory.createLiteral();
            pathBValue.setValueAsString(pathString);
            SwaggerParser.setAnnotationAttribute(resourceConfigAnnotation, 'path', pathBValue);
        }

        if (!_.isNil(httpMethodJsonObject.produces)) {
            const produceValues = [];
            httpMethodJsonObject.produces.forEach((produceEntry) => {
                const produceValue = NodeFactory.createLiteral();
                produceValue.setValueAsString(produceEntry);
                produceValues.push(produceValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'produces', produceValues);
        }

        if (!_.isNil(httpMethodJsonObject.consumes)) {
            const consumeValues = [];
            httpMethodJsonObject.consumes.forEach((consumeEntry) => {
                const consumeValue = NodeFactory.createLiteral();
                consumeValue.setValueAsString(consumeEntry);
                consumeValues.push(consumeValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'consumes', consumeValues);
        }

        const existingResourceConfig = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getAnnotationName() === `${httpAlias}:resourceConfig`;
        });
        if (existingResourceConfig.length > 0) {
            resourceDefinition.replaceAnnotationAttachments(existingResourceConfig[0], resourceConfigAnnotation);
        } else {
            resourceDefinition.addAnnotationAttachments(resourceConfigAnnotation, silent);
        }
    }

    /**
     * Creates the @ResourceConfig annotation for a given {@link ResourceNode} using the http method object of the
     * swagger JSON.
     *
     * @param {ResourceNode} resourceDefinition The resource definition to be updated.
     * @param {Object} httpMethodJsonObject The swagger operation object.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject, silent = DISABLE_EVENT_FIRING) {
        const resourceConfigAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ResourceConfig');

        if (!_.isNil(httpMethodJsonObject.schemes) && httpMethodJsonObject.schemes.length > 0) {
            const schemeBValues = [];
            this._swaggerJson.schemes.forEach((schemeEntry) => {
                const schemeValue = NodeFactory.createLiteral();
                schemeValue.setValueAsString(schemeEntry);
                schemeBValues.push(schemeValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'schemes', schemeBValues);
        }

        // TODO : Implement authorization.

        const existingResourceConfig = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getAnnotationName() === `${swaggerAlias}:ResourceConfig`;
        });

        if (existingResourceConfig.length > 0) {
            resourceDefinition.replaceAnnotationAttachments(existingResourceConfig[0], resourceConfigAnnotation);
        } else {
            resourceDefinition.addAnnotationAttachments(resourceConfigAnnotation, silent);
        }
    }

    /**
     * Remove and create parameter definitions of the resource definition.
     *
     * @param {ResourceNode} resourceDefinition The resource definition to be updated.
     * @param {Object[]} swaggerParameters The swagger parameter objects.
     * @memberof SwaggerParser
     */
    createParameterDefs(resourceDefinition, swaggerParameters) {
        if (!_.isNil(swaggerParameters) && swaggerParameters.length > 0) {
            // Removing existing parameter definitions except for the first one(message m).
            resourceDefinition.getParameters().splice(1);

            swaggerParameters.forEach((swaggerParameter) => {
                if ((swaggerParameter.in === 'query' || swaggerParameter.in === 'path') &&
                                                                                !_.isNil(swaggerParameter.type)) {
                    const parameterType = NodeFactory.createValueType();
                    if (swaggerParameter.type === 'number' || swaggerParameter.type === 'integer') {
                        parameterType.setTypeKind('int');
                    } else {
                        parameterType.setTypeKind(swaggerParameter.type);
                    }
                    const parameterName = NodeFactory.createIdentifier();
                    parameterName.setValue(swaggerParameter.name);

                    // Creating parameter annotation.
                    let paramAnnotation;
                    if (swaggerParameter.in === 'query') {
                        paramAnnotation = SwaggerParser.createAnnotationAttachment(httpAlias, 'QueryParam');
                    } else if (swaggerParameter.in === 'path') {
                        paramAnnotation = SwaggerParser.createAnnotationAttachment(httpAlias, 'PathParam');
                    }

                    const nameValue = NodeFactory.createLiteral();
                    nameValue.setValueAsString(swaggerParameter.name);
                    SwaggerParser.setAnnotationAttribute(paramAnnotation, 'value', nameValue);

                    const parameter = NodeFactory.createVariable({
                        typeNode: parameterType,
                        name: parameterName,
                        // TODO : Set Annotation 'paramAnnotation'
                    });

                    resourceDefinition.addParameter(parameter);
                }
            });
        }
    }

    /**
     * Creates the @ParametersInfo annotation for a given {@link ResourceNode} using the http method object of the
     * swagger JSON.
     *
     * @param {ResourceNode} resourceDefinition The resource definition to be updated.
     * @param {Object} httpMethodJsonObject The http method json object of the swagger json.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createParametersInfoAnnotation(resourceDefinition, httpMethodJsonObject, silent = DISABLE_EVENT_FIRING) {
        if (!_.isNil(httpMethodJsonObject.parameters) && httpMethodJsonObject.parameters.length > 0) {
            const parametersInfoAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias,
                                                                                                    'ParametersInfo');

            const parameterInfoAnnotations = [];
            httpMethodJsonObject.parameters.forEach((parameter) => {
                const responseAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ParameterInfo');

                if (!_.isNil(parameter.in)) {
                    const inValue = NodeFactory.createLiteral();
                    inValue.setValueAsString(parameter.in);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'in', inValue);
                }

                if (!_.isNil(parameter.name)) {
                    const nameValue = NodeFactory.createLiteral();
                    nameValue.setValueAsString(parameter.name);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'name', nameValue);
                }

                if (!_.isNil(parameter.description)) {
                    const descriptionValue = NodeFactory.createLiteral();
                    descriptionValue.setValueAsString(parameter.description);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'description', descriptionValue);
                }

                if (!_.isNil(parameter.required)) {
                    const requiredValue = NodeFactory.createLiteral();
                    requiredValue.setValue(parameter.required);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'required', requiredValue);
                }

                if (!_.isNil(parameter.allowEmptyValue)) {
                    const allowEmptyValueValue = NodeFactory.createLiteral();
                    allowEmptyValueValue.setValue(parameter.allowEmptyValue);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'allowEmptyValue', allowEmptyValueValue);
                }

                if (!_.isNil(parameter.type)) {
                    const typeValue = NodeFactory.createLiteral();
                    typeValue.setValueAsString(parameter.type);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'parameterType', typeValue);
                }

                if (!_.isNil(parameter.format)) {
                    const formatValue = NodeFactory.createLiteral();
                    formatValue.setValueAsString(parameter.format);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'format', formatValue);
                }

                if (!_.isNil(parameter.collectionFormat)) {
                    const collectionFormatValue = NodeFactory.createLiteral();
                    collectionFormatValue.setValueAsString(parameter.collectionFormat);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'collectionFormat',
                        collectionFormatValue);
                }

                parameterInfoAnnotations.push(responseAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(parametersInfoAnnotation, 'value', parameterInfoAnnotations);

            const existingParametersInfo = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getAnnotationName() === `${swaggerAlias}:ParametersInfo`;
            });

            if (existingParametersInfo.length > 0) {
                resourceDefinition.replaceAnnotationAttachments(existingParametersInfo[0], parametersInfoAnnotation);
            } else {
                resourceDefinition.addAnnotationAttachments(parametersInfoAnnotation, silent);
            }
        }
    }

    /**
     * Creates the @ResourceInfo annotation for a given {@link ResourceNode} using the http method object of the
     * swagger JSON.
     *
     * @param {ResourceNode} resourceDefinition The resource definition to be updated.
     * @param {Object} httpMethodJsonObject The swagger operation object.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createResourceInfoAnnotation(resourceDefinition, httpMethodJsonObject, silent = DISABLE_EVENT_FIRING) {
        const resourceInfoAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ResourceInfo');

        if (!_.isNil(httpMethodJsonObject.tags) && httpMethodJsonObject.tags.length > 0) {
            const tagValues = [];
            httpMethodJsonObject.tags.forEach((tagEntry) => {
                const tagValue = NodeFactory.createLiteral();
                tagValue.setValueAsString(tagEntry);
                tagValues.push(tagValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceInfoAnnotation, 'tags', tagValues);
        }

        if (!_.isNil(httpMethodJsonObject.summary)) {
            const summaryBValue = NodeFactory.createLiteral();
            summaryBValue.setValueAsString(httpMethodJsonObject.summary);
            SwaggerParser.setAnnotationAttribute(resourceInfoAnnotation, 'summary', summaryBValue);
        }

        if (!_.isNil(httpMethodJsonObject.description)) {
            const descriptionBValue = NodeFactory.createLiteral();
            descriptionBValue.setValueAsString(httpMethodJsonObject.description);
            SwaggerParser.setAnnotationAttribute(resourceInfoAnnotation, 'description', descriptionBValue);
        }

        if (!_.isNil(httpMethodJsonObject.externalDocs)) {
            const externalDocAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ExternalDoc');

            if (!_.isNil(httpMethodJsonObject.externalDocs.description)) {
                const descriptionBValue = NodeFactory.createLiteral();
                descriptionBValue.setValueAsString(httpMethodJsonObject.externalDocs.description);
                SwaggerParser.setAnnotationAttribute(externalDocAnnotation, 'description', descriptionBValue);
            }

            if (!_.isNil(httpMethodJsonObject.externalDocs.url)) {
                const urlBValue = NodeFactory.createLiteral();
                urlBValue.setValueAsString(httpMethodJsonObject.externalDocs.url);
                SwaggerParser.setAnnotationAttribute(externalDocAnnotation, 'url', urlBValue);
            }

            SwaggerParser.setAnnotationAttribute(resourceInfoAnnotation, 'externalDocs', externalDocAnnotation);
        }

        const existingResourceInfo = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getAnnotationName() === `${swaggerAlias}:ResourceInfo`;
        });

        if (existingResourceInfo.length > 0) {
            resourceDefinition.replaceAnnotationAttachments(existingResourceInfo[0], resourceInfoAnnotation);
        } else {
            resourceDefinition.addAnnotationAttachments(resourceInfoAnnotation, silent);
        }
    }

    /**
     * Creates the @Responses annotation for a given {@link ResourceNode} using the http method object of the
     * swagger JSON.
     *
     * @param {ResourceNode} resourceDefinition The resource definition to be updated.
     * @param {Object} httpMethodJsonObject The swagger operation object.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] True to disable firing events. Else false.
     * @memberof SwaggerParser
     */
    createResponsesAnnotation(resourceDefinition, httpMethodJsonObject, silent = DISABLE_EVENT_FIRING) {
        if (!_.isNil(httpMethodJsonObject.responses)) {
            const responsesAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Responses');

            const responseAnnotations = [];
            _.forEach(httpMethodJsonObject.responses, (codeObj, code) => {
                const responseAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Response');

                if (!_.isNil(code)) {
                    const codeBValue = NodeFactory.createLiteral();
                    codeBValue.setValueAsString(code);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'code', codeBValue);
                }

                if (!_.isNil(code)) {
                    const descriptionBValue = NodeFactory.createLiteral();
                    descriptionBValue.setValueAsString(codeObj.description);
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'description', descriptionBValue);
                }
                responseAnnotations.push(responseAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(responsesAnnotation, 'value', responseAnnotations);

            const existingResponses = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getAnnotationName() === `${swaggerAlias}:ResourceInfo`;
            });

            if (existingResponses.length > 0) {
                resourceDefinition.replaceAnnotationAttachments(existingResponses[0], responsesAnnotation);
            } else {
                resourceDefinition.addAnnotationAttachments(responsesAnnotation, silent);
            }
        }
    }

    /**
     * Creates a new {@link ResourceNode} with a given @http:Path value and an http method annotation.
     * @param {ServiceNode} serviceDefinition The service definition to which the resource definition should be
     * added to.
     * @param {string} pathString The @http:Path value.
     * @param {string} httpMethodAsString The http method value.
     * @param {object} httpMethodJsonObject http method object of the swagger JSON.
     */
    createNewResource(serviceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        const newResourceDefinition = DefaultNodeFactory.createHttpResource(httpAlias);

        // if an operation id is defined set it as resource name.
        let resourceName;
        if (httpMethodJsonObject.operationId && httpMethodJsonObject.operationId.trim() !== '') {
            resourceName = NodeFactory.createIdentifier({
                value: httpMethodJsonObject.operationId.replace(/\s/g, ''),
            });
        } else if (httpMethodAsString !== 'x-MULTI') {
            resourceName = NodeFactory.createIdentifier({
                value: pathString.replace(/\W/g, '') + httpMethodAsString.toUpperCase(),
            });
        } else {
            const httpMethods = httpMethodJsonObject['x-METHODS'].join('_');
            resourceName = NodeFactory.createIdentifier({
                value: pathString.replace(/\W/g, '') + httpMethods,
            });
        }

        newResourceDefinition.setResourceName(resourceName);

        this.mergeToResource(newResourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject);

        serviceDefinition.addResources(newResourceDefinition);
        // newResourceDefinition.generateUniqueIdentifiers();
    }

    /**
     * Creates a new annotation attachment.
     * @static
     * @param {string} packageName The alias of the package.
     * @param {string} annotationName The name of the annotation.
     * @returns {AnnotationAttachmentNode} The annotation.
     * @memberof SwaggerParser
     */
    static createAnnotationAttachment(packageName, annotationName) {
        const annotationNameNode = NodeFactory.createIdentifier({
            value: `${packageName}:${annotationName}`,
        });
        return NodeFactory.createAnnotationAttachment({
            annotationName: annotationNameNode,
        });
    }

    /**
     * Creates or updates a value of an attribute of a given annotation attachment.
     * @static
     * @param {AnnotationAttachmentNode} annotationAttachment The annotation attachment which the attribute resides.
     * @param {string} key The Key of the attribute.
     * @param {Node} valueNode The value of the attribute.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] true to stop firing events, else false.
     * @memberof SwaggerParser
     */
    static setAnnotationAttribute(annotationAttachment, key, valueNode, silent = DISABLE_EVENT_FIRING) {
        const matchingAttributes = annotationAttachment.filterAttributes((annotationAttribute) => {
            return annotationAttribute.getName() === key;
        });

        if (matchingAttributes.length > 0) {
            const attributeValue = matchingAttributes[0].getValue();
            attributeValue.setValue(valueNode, silent);
        } else {
            const attributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
            attributeValue.setValue(valueNode, silent);
            const attribute = NodeFactory.createAnnotationAttachmentAttribute({ name: key });
            annotationAttachment.addAttribute(attribute, silent);
        }
    }

    /**
     * Creates an attribute in an annotation attachment which has a array value.
     * @static
     * @param {AnnotationAttachmentNode} annotationAttachment The annotation attachment which the attribute will get
     * created.
     * @param {string} key The value of the key,
     * @param {Node[]} nodes The nodes of the array value.
     * @param {boolean} [silent=DISABLE_EVENT_FIRING] true to stop firing events, else false.
     * @memberof SwaggerParser
     */
    static addNodesAsArrayedAttribute(annotationAttachment, key, nodes, silent = DISABLE_EVENT_FIRING) {
        const matchingAttributes = annotationAttachment.filterAttributes((annotationAttribute) => {
            return annotationAttribute.getName() === key;
        });

        if (matchingAttributes.length > 0) {
            const attributeValue = matchingAttributes[0].getValue();
            const arrayValues = nodes.map((node) => {
                return NodeFactory.createAnnotationAttachmentAttributeValue({
                    value: node,
                });
            });
            attributeValue.setValueArray(arrayValues, silent);
        } else {
            const arrayValues = nodes.map((node) => {
                return NodeFactory.createAnnotationAttachmentAttributeValue({
                    value: node,
                });
            });

            const attribute = NodeFactory.createAnnotationAttribute({ key });
            attribute.setValueArray(arrayValues, silent);
            annotationAttachment.addAttribute(attribute, silent);
        }
    }
}

export default SwaggerParser;

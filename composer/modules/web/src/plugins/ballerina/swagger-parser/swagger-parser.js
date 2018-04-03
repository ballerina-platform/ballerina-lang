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
import NodeFactory from 'plugins/ballerina/model/node-factory';
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';

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
     * @param {boolean} [isYaml=false] is the swagger definition a YAML content or not.
     * @param {string} httpPackageAlias The alias used for the ballerina.net.http package.
     * @param {string} swaggerPackageAlias The alias used for the ballerina.swagger package.
     */
    constructor(swaggerDefintiion, isYaml = false, httpPackageAlias = 'http', swaggerPackageAlias = 'swagger') {
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
            if (!_.isNil(this._swaggerJson.info.title)) {
                const serviceNameLiteral = NodeFactory.createIdentifier({
                    value: this._swaggerJson.info.title.replace(/[^0-9a-z]/gi, ''),
                });
                serviceDefinition.setName(serviceNameLiteral, DISABLE_EVENT_FIRING);
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
                    let existingResource = serviceDefinition.getResources().find((resourceDefinition) => {
                        const resourceName = resourceDefinition.getName().getValue();
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
                            existingResource = serviceDefinition.getResources().find((resourceDefinition) => {
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
                                existingResource.setName(NodeFactory.createIdentifier({
                                    value: xMultiObj.operationId.replace(/\s/g, ''),
                                }));
                            }
                        } else {
                            existingResource = serviceDefinition.getResources().find((resourceDefinition) => {
                                const httpMethods = resourceDefinition.getHttpMethodValues();
                                const pathValue = resourceDefinition.getPathAnnotationValue();
                                return httpMethods.length > 0 && !_.isNil(pathValue) &&
                                    _.isEqual(pathString, pathValue.replace(/"/g, ''))
                                    &&
                                    _.isEqual(httpMethodAsString, httpMethods[0].toLowerCase());
                            });
                            // if operationId exists set it as resource name.
                            if (existingResource && operation.operationId && operation.operationId.trim() !== '') {
                                existingResource.setName(NodeFactory.createIdentifier({
                                    value: operation.operationId.replace(/\s/g, ''),
                                }));
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
            basePathValue.setValue(JSON.stringify(this._swaggerJson.basePath));
            SwaggerParser.setAnnotationAttribute(configAnnotation, 'basePath', basePathValue);
        }

        if (!_.isNil(this._swaggerJson.host)) {
            const hostAndPort = this._swaggerJson.host.split(':');

            const hostBValue = NodeFactory.createLiteral();
            hostBValue.setValue(JSON.stringify(hostAndPort[0]));
            SwaggerParser.setAnnotationAttribute(configAnnotation, 'host', hostBValue);

            if (hostAndPort.length > 1) {
                const portBValue = NodeFactory.createLiteral();
                portBValue.setValue(hostAndPort[1]);
                SwaggerParser.setAnnotationAttribute(configAnnotation, 'port', portBValue);
            }
        }

        const existingConfigurationAnnotation = serviceDefinition.filterAnnotationAttachments(
            (annotationAttachment) => {
                return annotationAttachment.getPackageAlias().getValue() === httpAlias &&
                            annotationAttachment.getAnnotationName().getValue() === 'configuration';
            });
        if (existingConfigurationAnnotation.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingConfigurationAnnotation[0], configAnnotation,
                                                                                                                silent);
        } else {
            serviceDefinition.addAnnotationAttachments(configAnnotation, -1, silent);
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
                consumesValue.setValue(JSON.stringify(consumeEntry));
                consumeValues.push(consumesValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(consumesAnnotation, 'value', consumeValues);

            const existingConsumesAnnotation = astNode.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                            annotationAttachment.getAnnotationName().getValue() === 'Consumes';
            });
            if (existingConsumesAnnotation.length > 0) {
                astNode.replaceAnnotationAttachments(existingConsumesAnnotation[0], consumesAnnotation, silent);
            } else {
                astNode.addAnnotationAttachments(consumesAnnotation, -1, silent);
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
                producesValue.setValue(JSON.stringify(producesEntry));
                producesValues.push(producesValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(producesAnnotation, 'value', producesValues);

            const existingProducesAnnotation = astNode.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                        annotationAttachment.getAnnotationName().getValue() === 'Produces';
            });
            if (existingProducesAnnotation.length > 0) {
                astNode.replaceAnnotationAttachments(existingProducesAnnotation[0], producesAnnotation);
            } else {
                astNode.addAnnotationAttachments(producesAnnotation, -1, silent);
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
            titleValue.setValue(JSON.stringify(this._swaggerJson.info.title));
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'title', titleValue);
        }

        if (!_.isNil(this._swaggerJson.info.version)) {
            const versionValue = NodeFactory.createLiteral();
            versionValue.setValue(JSON.stringify(this._swaggerJson.info.version));
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'serviceVersion', versionValue);
        }

        if (!_.isNil(this._swaggerJson.info.description)) {
            const descriptionValue = NodeFactory.createLiteral();
            descriptionValue.setValue(JSON.stringify(this._swaggerJson.info.description));
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'description', descriptionValue);
        }

        if (!_.isNil(this._swaggerJson.info.termsOfService)) {
            const termOfServiceValue = NodeFactory.createLiteral();
            termOfServiceValue.setValue(JSON.stringify(this._swaggerJson.info.termsOfService));
            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'termsOfService', termOfServiceValue);
        }

        if (!_.isNil(this._swaggerJson.info.contact)) {
            const contactAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Contact');
            if (!_.isNil(this._swaggerJson.info.contact.name)) {
                const nameValue = NodeFactory.createLiteral();
                nameValue.setValue(JSON.stringify(this._swaggerJson.info.contact.name));
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'name', nameValue);
            }

            if (!_.isNil(this._swaggerJson.info.contact.url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValue(JSON.stringify(this._swaggerJson.info.contact.url));
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'url', urlValue);
            }

            if (!_.isNil(this._swaggerJson.info.contact.email)) {
                const emailValue = NodeFactory.createLiteral();
                emailValue.setValue(JSON.stringify(this._swaggerJson.info.contact.email));
                SwaggerParser.setAnnotationAttribute(contactAnnotation, 'email', emailValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'contact', contactAnnotation);
        }

        if (!_.isNil(this._swaggerJson.info.license)) {
            const licenseAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'License');

            if (!_.isNil(this._swaggerJson.info.license.name)) {
                const nameValue = NodeFactory.createLiteral();
                nameValue.setValue(JSON.stringify(this._swaggerJson.info.license.name));
                SwaggerParser.setAnnotationAttribute(licenseAnnotation, 'name', nameValue);
            }

            if (!_.isNil(this._swaggerJson.info.license.url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValue(JSON.stringify(this._swaggerJson.info.license.url));
                SwaggerParser.setAnnotationAttribute(licenseAnnotation, 'url', urlValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'license', licenseAnnotation);
        }

        if (!_.isNil(this._swaggerJson.externalDocs)) {
            const externalDocsAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ExternalDoc');

            if (!_.isNil(this._swaggerJson.externalDocs.description)) {
                const descriptionValue = NodeFactory.createLiteral();
                descriptionValue.setValue(JSON.stringify(this._swaggerJson.externalDocs.description));
                SwaggerParser.setAnnotationAttribute(externalDocsAnnotation, 'description', descriptionValue);
            }

            if (!_.isNil(this._swaggerJson.externalDocs.url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValue(JSON.stringify(this._swaggerJson.externalDocs.url));
                SwaggerParser.setAnnotationAttribute(externalDocsAnnotation, 'url', urlValue);
            }

            SwaggerParser.setAnnotationAttribute(serviceInfoAnnotation, 'externalDocs', externalDocsAnnotation);
        }

        if (!_.isNil(this._swaggerJson.info['x-organization'])) {
            const organizationAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'Organization');

            if (!_.isNil(this._swaggerJson.info['x-organization'].name)) {
                const nameValue = NodeFactory.createLiteral();
                nameValue.setValue(JSON.stringify(this._swaggerJson.info['x-organization'].name));
                SwaggerParser.setAnnotationAttribute(organizationAnnotation, 'name', nameValue);
            }

            if (!_.isNil(this._swaggerJson.info['x-organization'].url)) {
                const urlValue = NodeFactory.createLiteral();
                urlValue.setValue(JSON.stringify(this._swaggerJson.info['x-organization'].url));
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
                    nameValue.setValue(JSON.stringify(developer.name));
                    SwaggerParser.setAnnotationAttribute(developerAnnotation, 'name', nameValue);
                }

                if (!_.isNil(developer.email)) {
                    const emailValue = NodeFactory.createLiteral();
                    emailValue.setValue(JSON.stringify(developer.email));
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
                    nameValue.setValue(JSON.stringify(tag.name));
                    SwaggerParser.setAnnotationAttribute(tagAnnotation, 'name', nameValue);
                }

                if (!_.isNil(tag.description)) {
                    const descriptionValue = NodeFactory.createLiteral();
                    descriptionValue.setValue(JSON.stringify(tag.description));
                    SwaggerParser.setAnnotationAttribute(tagAnnotation, 'description', descriptionValue);
                }
                tagBValues.push(tagAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(serviceInfoAnnotation, 'tags', tagBValues);
        }

        const existingServiceInfoAnnotation = serviceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                    annotationAttachment.getAnnotationName().getValue() === 'ServiceInfo';
        });
        if (existingServiceInfoAnnotation.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingServiceInfoAnnotation[0], serviceInfoAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(serviceInfoAnnotation, -1, silent);
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
            versionValue.setValue(JSON.stringify(this._swaggerJson.swagger));
            SwaggerParser.setAnnotationAttribute(swaggerAnnotation, 'swaggerVersion', versionValue);
        }

        const existingSwaggerAnnotation = serviceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                    annotationAttachment.getAnnotationName().getValue() === 'Swagger';
        });
        if (existingSwaggerAnnotation.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingSwaggerAnnotation[0], swaggerAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(swaggerAnnotation, -1, silent);
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
            const hostValue = NodeFactory.createLiteral();
            hostValue.setValue(JSON.stringify(this._swaggerJson.host));
            SwaggerParser.setAnnotationAttribute(serviceConfigAnnotation, 'host', hostValue);
        }

        if (!_.isNil(this._swaggerJson.schemes) && this._swaggerJson.schemes.length > 0) {
            const schemeBValues = [];
            this._swaggerJson.schemes.forEach((schemeEntry) => {
                const schemeValue = NodeFactory.createLiteral();
                schemeValue.setValue(JSON.stringify(schemeEntry));
                schemeBValues.push(schemeValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(serviceConfigAnnotation, 'schemes', schemeBValues);
        }

        // TODO: Authorization attribute.
        const existingServiceConfig = serviceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
            annotationAttachment.getAnnotationName() === 'ServiceConfig';
        });
        if (existingServiceConfig.length > 0) {
            serviceDefinition.replaceAnnotationAttachments(existingServiceConfig[0], serviceConfigAnnotation);
        } else {
            serviceDefinition.addAnnotationAttachments(serviceConfigAnnotation, -1, silent);
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
        // Creating @ResourceInfo annotation.
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
        const resourceConfigAnnotation = SwaggerParser.createAnnotationAttachment(httpAlias, 'resourceConfig');

        const httpMethodsBValues = [];
        if (httpMethodAsString !== 'x-MULTI') {
            const singleHttpMethod = NodeFactory.createLiteral();
            singleHttpMethod.setValue(JSON.stringify(httpMethodAsString.toUpperCase()));
            httpMethodsBValues.push(singleHttpMethod);
        } else {
            httpMethodJsonObject['x-METHODS'].forEach((httpMethod) => {
                const httpMethodLiteral = NodeFactory.createLiteral();
                httpMethodLiteral.setValue(JSON.stringify(httpMethod.toUpperCase()));
                httpMethodsBValues.push(httpMethodLiteral);
            });
        }
        SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'methods', httpMethodsBValues);

        if (!_.isNil(pathString)) {
            const pathBValue = NodeFactory.createLiteral();
            pathBValue.setValue(JSON.stringify(pathString));
            SwaggerParser.setAnnotationAttribute(resourceConfigAnnotation, 'path', pathBValue);
        }

        if (!_.isNil(httpMethodJsonObject.produces)) {
            const produceValues = [];
            httpMethodJsonObject.produces.forEach((produceEntry) => {
                const produceValue = NodeFactory.createLiteral();
                produceValue.setValue(JSON.stringify(produceEntry));
                produceValues.push(produceValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'produces', produceValues);
        }

        if (!_.isNil(httpMethodJsonObject.consumes)) {
            const consumeValues = [];
            httpMethodJsonObject.consumes.forEach((consumeEntry) => {
                const consumeValue = NodeFactory.createLiteral();
                consumeValue.setValue(JSON.stringify(consumeEntry));
                consumeValues.push(consumeValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'consumes', consumeValues);
        }

        const existingResourceConfig = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getPackageAlias().getValue() === httpAlias &&
                    annotationAttachment.getAnnotationName().getValue() === 'resourceConfig';
        });
        if (existingResourceConfig.length > 0) {
            resourceDefinition.replaceAnnotationAttachments(existingResourceConfig[0], resourceConfigAnnotation);
        } else {
            resourceDefinition.addAnnotationAttachments(resourceConfigAnnotation, -1, silent);
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
                schemeValue.setValue(JSON.stringify(schemeEntry));
                schemeBValues.push(schemeValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceConfigAnnotation, 'schemes', schemeBValues);
        }

        // TODO : Implement authorization.

        const existingResourceConfig = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                        annotationAttachment.getAnnotationName().getValue() === 'ResourceConfig';
        });

        if (existingResourceConfig.length > 0) {
            resourceDefinition.replaceAnnotationAttachments(existingResourceConfig[0], resourceConfigAnnotation);
        } else {
            resourceDefinition.addAnnotationAttachments(resourceConfigAnnotation, -1, silent);
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
            // Removing existing parameter definitions except for the first two(request and response).
            resourceDefinition.getParameters().splice(2);

            swaggerParameters.forEach((swaggerParameter) => {
                if (swaggerParameter.in === 'path' && !_.isNil(swaggerParameter.type)) {
                    const parameterType = NodeFactory.createValueType();
                    if (swaggerParameter.type === 'number' || swaggerParameter.type === 'integer') {
                        parameterType.setTypeKind('int');
                    } else if (swaggerParameter.type === 'array') {
                        parameterType.setTypeKind(`${swaggerParameter.items.type}[]`);
                    } else {
                        parameterType.setTypeKind(swaggerParameter.type);
                    }
                    const parameterName = NodeFactory.createIdentifier();
                    parameterName.setValue(swaggerParameter.name);

                    const nameValue = NodeFactory.createLiteral();
                    nameValue.setValue(JSON.stringify(swaggerParameter.name));

                    const parameter = NodeFactory.createVariable({
                        typeNode: parameterType,
                        name: parameterName,
                        initialExpression: undefined,
                    });

                    resourceDefinition.addParameters(parameter);
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
                    inValue.setValue(JSON.stringify(parameter.in));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'in', inValue);
                }

                if (!_.isNil(parameter.name)) {
                    const nameValue = NodeFactory.createLiteral();
                    nameValue.setValue(JSON.stringify(parameter.name));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'name', nameValue);
                }

                if (!_.isNil(parameter.description)) {
                    const descriptionValue = NodeFactory.createLiteral();
                    descriptionValue.setValue(JSON.stringify(parameter.description));
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
                    typeValue.setValue(JSON.stringify(parameter.type));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'parameterType', typeValue);
                }

                if (!_.isNil(parameter.format)) {
                    const formatValue = NodeFactory.createLiteral();
                    formatValue.setValue(JSON.stringify(parameter.format));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'format', formatValue);
                }

                if (!_.isNil(parameter.collectionFormat)) {
                    const collectionFormatValue = NodeFactory.createLiteral();
                    collectionFormatValue.setValue(JSON.stringify(parameter.collectionFormat));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'collectionFormat',
                        collectionFormatValue);
                }

                parameterInfoAnnotations.push(responseAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(parametersInfoAnnotation, 'value', parameterInfoAnnotations);

            const existingParametersInfo = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                            annotationAttachment.getAnnotationName().getValue() === 'ParametersInfo';
            });

            if (existingParametersInfo.length > 0) {
                resourceDefinition.replaceAnnotationAttachments(existingParametersInfo[0], parametersInfoAnnotation,
                                                                                                                silent);
            } else {
                resourceDefinition.addAnnotationAttachments(parametersInfoAnnotation, -1, silent);
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
                tagValue.setValue(JSON.stringify(tagEntry));
                tagValues.push(tagValue);
            });
            SwaggerParser.addNodesAsArrayedAttribute(resourceInfoAnnotation, 'tags', tagValues);
        }

        if (!_.isNil(httpMethodJsonObject.summary)) {
            const summaryBValue = NodeFactory.createLiteral();
            summaryBValue.setValue(JSON.stringify(httpMethodJsonObject.summary));
            SwaggerParser.setAnnotationAttribute(resourceInfoAnnotation, 'summary', summaryBValue);
        }

        if (!_.isNil(httpMethodJsonObject.description)) {
            const descriptionBValue = NodeFactory.createLiteral();
            descriptionBValue.setValue(JSON.stringify(httpMethodJsonObject.description));
            SwaggerParser.setAnnotationAttribute(resourceInfoAnnotation, 'description', descriptionBValue);
        }

        if (!_.isNil(httpMethodJsonObject.externalDocs)) {
            const externalDocAnnotation = SwaggerParser.createAnnotationAttachment(swaggerAlias, 'ExternalDoc');

            if (!_.isNil(httpMethodJsonObject.externalDocs.description)) {
                const descriptionBValue = NodeFactory.createLiteral();
                descriptionBValue.setValue(JSON.stringify(httpMethodJsonObject.externalDocs.description));
                SwaggerParser.setAnnotationAttribute(externalDocAnnotation, 'description', descriptionBValue);
            }

            if (!_.isNil(httpMethodJsonObject.externalDocs.url)) {
                const urlBValue = NodeFactory.createLiteral();
                urlBValue.setValue(JSON.stringify(httpMethodJsonObject.externalDocs.url));
                SwaggerParser.setAnnotationAttribute(externalDocAnnotation, 'url', urlBValue);
            }

            SwaggerParser.setAnnotationAttribute(resourceInfoAnnotation, 'externalDocs', externalDocAnnotation);
        }

        const existingResourceInfo = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
            return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                    annotationAttachment.getAnnotationName().getValue() === 'ResourceInfo';
        });

        if (existingResourceInfo.length > 0) {
            resourceDefinition.replaceAnnotationAttachments(existingResourceInfo[0], resourceInfoAnnotation, silent);
        } else {
            resourceDefinition.addAnnotationAttachments(resourceInfoAnnotation, -1, silent);
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
                    codeBValue.setValue(JSON.stringify(code));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'code', codeBValue);
                }

                if (!_.isNil(code)) {
                    const descriptionBValue = NodeFactory.createLiteral();
                    descriptionBValue.setValue(JSON.stringify(codeObj.description));
                    SwaggerParser.setAnnotationAttribute(responseAnnotation, 'description', descriptionBValue);
                }
                responseAnnotations.push(responseAnnotation);
            });

            SwaggerParser.addNodesAsArrayedAttribute(responsesAnnotation, 'value', responseAnnotations);

            const existingResponsesInfo = resourceDefinition.filterAnnotationAttachments((annotationAttachment) => {
                return annotationAttachment.getPackageAlias().getValue() === swaggerAlias &&
                    annotationAttachment.getAnnotationName().getValue() === 'Responses';
            });

            if (existingResponsesInfo.length > 0) {
                resourceDefinition.replaceAnnotationAttachments(existingResponsesInfo[0], responsesAnnotation, silent);
            } else {
                resourceDefinition.addAnnotationAttachments(responsesAnnotation, -1, silent);
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
        const newResourceDefinition = DefaultNodeFactory.createHTTPResource();

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

        newResourceDefinition.setName(resourceName);

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
        return NodeFactory.createAnnotationAttachment({
            packageAlias: NodeFactory.createLiteral({
                value: packageName,
                valueWithBar: packageName,
            }),
            annotationName: NodeFactory.createLiteral({
                value: annotationName,
                valueWithBar: annotationName,
            }),
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
            return annotationAttribute.getName().getValue() === key;
        });

        if (matchingAttributes.length > 0) {
            const existingAttributeValue = matchingAttributes[0].getValue();
            existingAttributeValue.setValue(valueNode, silent);
        } else {
            const keyIdentifier = NodeFactory.createIdentifier({
                value: key,
            });
            const attributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
            attributeValue.setValue(valueNode, silent);
            const attribute = NodeFactory.createAnnotationAttachmentAttribute({
                name: keyIdentifier,
                value: attributeValue,
            });
            annotationAttachment.addAttributes(attribute, silent);
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
            return annotationAttribute.getName().getValue() === key;
        });

        if (matchingAttributes.length > 0) {
            const existingAttributeValue = matchingAttributes[0].getValue();
            const arrayValuesForExisting = nodes.map((node) => {
                const innerValue = NodeFactory.createAnnotationAttachmentAttributeValue();
                innerValue.setValue(node);
                return innerValue;
            });
            existingAttributeValue.setValueArray(arrayValuesForExisting, silent);
        } else {
            const arrayValues = nodes.map((node) => {
                const innerValue = NodeFactory.createAnnotationAttachmentAttributeValue();
                innerValue.setValue(node);
                return innerValue;
            });

            const keyIdentifier = NodeFactory.createIdentifier({
                value: key,
            });

            const attribute = NodeFactory.createAnnotationAttachmentAttribute({ name: keyIdentifier });
            const attributeValue = NodeFactory.createAnnotationAttachmentAttributeValue({
                value: undefined,
            });
            attributeValue.setValueArray(arrayValues);
            attribute.setValue(attributeValue);
            annotationAttachment.addAttributes(attribute, silent);
        }
    }
}

export default SwaggerParser;

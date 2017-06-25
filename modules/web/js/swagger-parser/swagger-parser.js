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
import BallerinaASTFactory from '../ballerina/ast/ballerina-ast-factory';
import DefaultBallerinaASTFactory from '../ballerina/ast/default-ballerina-ast-factory';

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
            const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);

            // Set service name if missing
            if (!serviceDefinition.getServiceName()) {
                serviceDefinition.setServiceName(this._swaggerJson.info.title.replace(/[^0-9a-z]/gi, ''));
            }

            // Creating ServiceInfo annotation.
            this._createServiceInfoAnnotation(serviceDefinition);
            this._createSwaggerAnnotation(serviceDefinition);
            this._createServiceConfigAnnotation(serviceDefinition);

            // Creating basePath annotation
            if (!_.isUndefined(this._swaggerJson.basePath)) {
                const basePathAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
                                                                                                    'http', 'BasePath');
                const basePathAnnotation = this._createSimpleAnnotation({
                    annotation: {
                        fullPackageName: 'ballerina.net.http',
                        packageName: 'http',
                        identifier: 'BasePath',
                        supported: true,
                    },
                    swaggerJsonNode: this._swaggerJson.basePath,
                });
                serviceDefinition.addChild(basePathAnnotation, basePathAnnotationIndex, true);
            }

            // Creating consumes annotation
            if (!_.isUndefined(this._swaggerJson.consumes)) {
                const consumesAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
                                                                                                'swagger', 'Consumes');
                const consumesAnnotation = this._createSimpleAnnotation({
                    annotation: {
                        fullPackageName: 'ballerina.net.http.swagger',
                        packageName: 'swagger',
                        identifier: 'Consumes',
                        supported: true,
                    },
                    swaggerJsonNode: this._swaggerJson.consumes,
                });
                serviceDefinition.addChild(consumesAnnotation, consumesAnnotationIndex, true);
            }

            // Creating consumes annotation
            if (!_.isUndefined(this._swaggerJson.produces)) {
                const producesAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
                                                                                                'swagger', 'Produces');
                const producesAnnotation = this._createSimpleAnnotation({
                    annotation: {
                        fullPackageName: 'ballerina.net.http.swagger',
                        packageName: 'swagger',
                        identifier: 'Produces',
                        supported: true,
                    },
                    swaggerJsonNode: this._swaggerJson.produces,
                });
                serviceDefinition.addChild(producesAnnotation, producesAnnotationIndex, true);
            }

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
                    if (_.isUndefined(existingResource)) {
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

                    if (!_.isUndefined(existingResource)) {
                        this._mergeToResource(existingResource, pathString, httpMethodAsString, operation);
                    } else {
                        this._createNewResource(serviceDefinition, pathString, httpMethodAsString, operation);
                    }
                });
            });
        } catch (err) {
            throw new Error('Unable to parse swagger definition.');
        }
    }

    /**
     * Creates the @ServiceInfo annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     * @private
     */
    _createServiceInfoAnnotation(serviceDefinition) {
        const serviceInfoAnnotation = BallerinaASTFactory.createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger',
            packageName: 'swagger',
            identifier: 'ServiceInfo',
        });

        if (!_.isUndefined(this._swaggerJson.info.title)) {
            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'title',
                rightValue: JSON.stringify(this._swaggerJson.info.title),
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.version)) {
            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'version',
                rightValue: JSON.stringify(this._swaggerJson.info.version),
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.description)) {
            if (this._swaggerJson.info.description) {
                serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: 'description',
                    rightValue: JSON.stringify(this._swaggerJson.info.description),
                }));
            }
        }

        if (!_.isUndefined(this._swaggerJson.info.termsOfService)) {
            if (this._swaggerJson.info.termsOfService) {
                serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: 'termsOfService',
                    rightValue: JSON.stringify(this._swaggerJson.info.termsOfService),
                }));
            }
        }

        if (!_.isUndefined(this._swaggerJson.info.contact)) {
            const contactAnnotation = this._createSimpleAnnotation({
                annotation: { packageName: 'swagger', identifier: 'contact', supported: true },
                swaggerJsonNode: this._swaggerJson.info.contact,
            });

            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'contact',
                rightValue: contactAnnotation,
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.license)) {
            const licenseAnnotation = this._createSimpleAnnotation({
                annotation: { packageName: 'swagger', identifier: 'contact', supported: true },
                swaggerJsonNode: this._swaggerJson.info.license,
            });

            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'contact',
                rightValue: licenseAnnotation,
            }));
        }

        // TODO : Create externalDocs, tag, organization, developers.

        const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        const serviceInfoAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations,
                                                                                    'swagger', 'ServiceInfo');
        serviceDefinition.addChild(serviceInfoAnnotation, serviceInfoAnnotationIndex, true);
    }

    /**
     * Creates the @Swagger annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     * @private
     */
    _createSwaggerAnnotation(serviceDefinition) {
        const swaggerAnnotation = BallerinaASTFactory.createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger',
            packageName: 'swagger',
            identifier: 'Swagger',
        });
        swaggerAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
            leftValue: 'version',
            rightValue: JSON.stringify(this._swaggerJson.swagger),
        }));
        const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        const swaggerAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger',
                                                                                'Swagger');
        serviceDefinition.addChild(swaggerAnnotation, swaggerAnnotationIndex, true);
    }

    /**
     * Creates the @ServiceConfig annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     * @private
     */
    _createServiceConfigAnnotation(serviceDefinition) {
        const serviceConfigAnnotation = BallerinaASTFactory.createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger',
            packageName: 'swagger',
            identifier: 'ServiceConfig',
        });

        if (!_.isUndefined(this._swaggerJson.host)) {
            serviceConfigAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'host',
                rightValue: JSON.stringify(this._swaggerJson.host),
            }));
        }

        this._defaultSwaggerToASTConverter(this._swaggerJson.schemes, serviceConfigAnnotation);

        const serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        const swaggerAnnotationIndex = SwaggerParser.removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger',
                                                                                'ServiceConfig');
        serviceDefinition.addChild(serviceConfigAnnotation, swaggerAnnotationIndex, true);
    }

    /**
     * Creates a simple annotation ast object.
     *
     * @param {any} args The arguments of the object. See {@link Annotation}.
     * @returns {Annotation} A new annotation object.
     *
     * @memberof SwaggerParser
     */
    _createSimpleAnnotation(args) {
        let newAnnotationAst;
        if (!_.isUndefined(args.swaggerJsonNode)) {
            newAnnotationAst = BallerinaASTFactory.createAnnotation(args.annotation);
            this._defaultSwaggerToASTConverter(args.swaggerJsonNode, newAnnotationAst);
        }

        return newAnnotationAst;
    }

    /**
     * Merge a swagger json object of the http method to a {@link ResourceDefinition}.
     * @param {ResourceDefinition} resourceDefinition The resource definition to be merged with.
     * @param {string} pathString The @http:Path{} value
     * @param {string} httpMethodAsString The http method of the resource. Example: @http:GET{}
     * @param {object} httpMethodJsonObject The http method object of the swagger json.
     * @private
     */
    _mergeToResource(resourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        // this._createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject);
        const pathAnnotation = resourceDefinition.getPathAnnotation(true);
        pathAnnotation.getChildren()[0].setRightValue(JSON.stringify(pathString), { doSilently: true });
        const methodAnnotation = resourceDefinition.getHttpMethodAnnotation();
        methodAnnotation.setIdentifier(httpMethodAsString.toUpperCase(), { doSilently: true });

        const resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);

        // Creating consumes annotation
        if (!_.isUndefined(httpMethodJsonObject.consumes)) {
            const consumesAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                    'swagger', 'Consumes');
            const consumesAnnotation = this._createSimpleAnnotation({
                annotation: {
                    fullPackageName: 'ballerina.net.http.swagger',
                    packageName: 'swagger',
                    identifier: 'Consumes',
                },
                swaggerJsonNode: httpMethodJsonObject.consumes,
            });
            resourceDefinition.addChild(consumesAnnotation, consumesAnnotationIndex, true);
        }

        // Creating consumes annotation
        if (!_.isUndefined(httpMethodJsonObject.produces)) {
            const producesAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                    'swagger', 'Produces');
            const producesAnnotation = this._createSimpleAnnotation({
                annotation: {
                    fullPackageName: 'ballerina.net.http.swagger',
                    packageName: 'swagger',
                    identifier: 'Produces',
                },
                swaggerJsonNode: httpMethodJsonObject.produces,
            });
            resourceDefinition.addChild(producesAnnotation, producesAnnotationIndex, true);
        }

        SwaggerParser.createResponsesAnnotation(resourceDefinition, httpMethodJsonObject);
        SwaggerParser.createParametersAnnotation(resourceDefinition, httpMethodJsonObject);
    }

    /**
     * Creates the @ResourceConfig annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     * @private
     */
    static createResourceConfigAnnotation(resourceDefinition) {
        const resourceConfigAnnotation = resourceDefinition.getFactory().createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger',
            packageName: 'swagger',
            identifier: 'ResourceConfig',
        });

        const resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        const resourceConfigAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                        'swagger', 'ResourceConfig');
        resourceDefinition.addChild(resourceConfigAnnotation, resourceConfigAnnotationIndex, true);
    }

    /**
     * Creates the @Responses annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     * @private
     */
    static createResponsesAnnotation(resourceDefinition, httpMethodJsonObject) {
        if (!_.isUndefined(httpMethodJsonObject.responses)) {
            const responsesAnnotation = BallerinaASTFactory.createAnnotation({
                packageName: 'swagger',
                identifier: 'Responses',
            });

            // Creating the responses array entry
            const responsesAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
            const responseAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({
                rightValue: responsesAnnotationArray,
            });
            responsesAnnotation.addChild(responseAnnotationEntry);
            _.forEach(httpMethodJsonObject.responses, (codeObj, code) => {
                const responseAnnotation = BallerinaASTFactory.createAnnotation({
                    packageName: 'swagger',
                    identifier: 'Response',
                });

                const codeAnnotationEntry = resourceDefinition.getFactory().createAnnotationEntry({
                    leftValue: 'code',
                    rightValue: JSON.stringify(code),
                });
                responseAnnotation.addChild(codeAnnotationEntry);

                // description
                const descriptionAnnotationEntry = resourceDefinition.getFactory().createAnnotationEntry({
                    leftValue: 'description',
                    rightValue: JSON.stringify(codeObj.description),
                });
                responseAnnotation.addChild(descriptionAnnotationEntry);

                responsesAnnotationArray.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: '',
                    rightValue: responseAnnotation,
                }));
            });

            const resourceDefinitionAnnotations =
                                                resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
            const resourceConfigAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                            'swagger', 'Responses');
            resourceDefinition.addChild(responsesAnnotation, resourceConfigAnnotationIndex, true);
        }
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
    static createParametersAnnotation(resourceDefinition, httpMethodJsonObject) {
        if (!_.isUndefined(httpMethodJsonObject.parameters)) {
            const parametersAnnotation = BallerinaASTFactory.createAnnotation({
                fullPackageName: 'ballerina.net.http.swagger',
                packageName: 'swagger',
                identifier: 'ParametersInfo',
            });

            // Creating the responses array entry
            const parametersAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
            const parameterAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({
                rightValue: parametersAnnotationArray,
            });
            parametersAnnotation.addChild(parameterAnnotationEntry);
            _.forEach(httpMethodJsonObject.parameters, (parameter) => {
                const responseAnnotation = BallerinaASTFactory.createAnnotation({
                    packageName: 'swagger',
                    identifier: 'ParameterInfo',
                });

                if (!_.isUndefined(parameter.in)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'in',
                        rightValue: JSON.stringify(parameter.in),
                    }));
                }

                if (!_.isUndefined(parameter.name)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'name',
                        rightValue: JSON.stringify(parameter.name),
                    }));
                }

                if (!_.isUndefined(parameter.description)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'description',
                        rightValue: JSON.stringify(parameter.description),
                    }));
                }

                if (!_.isUndefined(parameter.required)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'required',
                        rightValue: parameter.required.toString(),
                    }));
                }

                if (!_.isUndefined(parameter.allowEmptyValue)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'allowEmptyValue',
                        rightValue: JSON.stringify(parameter.allowEmptyValue),
                    }));
                }

                if (!_.isUndefined(parameter.type)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'type',
                        rightValue: JSON.stringify(parameter.type),
                    }));
                }

                if (!_.isUndefined(parameter.format)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'format',
                        rightValue: JSON.stringify(parameter.format),
                    }));
                }

                if (!_.isUndefined(parameter.collectionFormat)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'collectionFormat',
                        rightValue: JSON.stringify(parameter.collectionFormat),
                    }));
                }

                parametersAnnotationArray.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: '',
                    rightValue: responseAnnotation,
                }));
            });

            const resourceDefinitionAnnotations =
                                                resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
            const resourceConfigAnnotationIndex = SwaggerParser.removeExistingAnnotation(resourceDefinitionAnnotations,
                                                                                        'swagger', 'ParametersInfo');
            resourceDefinition.addChild(parametersAnnotation, resourceConfigAnnotationIndex, true);
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

        newResourceDefinition.getPathAnnotation(true).getChildren()[0].setRightValue(JSON.stringify(pathString));
        newResourceDefinition.getHttpMethodAnnotation().setIdentifier(httpMethodAsString.toUpperCase());

        this._mergeToResource(newResourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject);
        serviceDefinition.addChild(newResourceDefinition, undefined, true, true);
        newResourceDefinition.generateUniqueIdentifiers();
    }

    _defaultSwaggerToASTConverter(jsonObject, astNode) {
        if (!_.isUndefined(jsonObject)) {
            if (_.isPlainObject(jsonObject)) {
                _.forEach(jsonObject, (value, key) => {
                    if (_.isPlainObject(value)) {
                        const annotationNode = BallerinaASTFactory.createAnnotation({ identifier: key });
                        this._defaultSwaggerToASTConverter(value, annotationNode);
                        astNode.addChild(annotationNode, undefined, true);
                    } else if (_.isArrayLikeObject(value)) {
                        const entryArrayNode = BallerinaASTFactory.createAnnotationEntryArray();
                        _.forEach(value, (arrayValue) => {
                            this._defaultSwaggerToASTConverter(arrayValue, entryArrayNode);
                        });
                        astNode.addChild(BallerinaASTFactory.createAnnotationEntry({
                            leftValue: key,
                            rightValue: entryArrayNode,
                        }), undefined, true);
                    } else {
                        astNode.addChild(BallerinaASTFactory.createAnnotationEntry({
                            leftValue: key,
                            rightValue: `"${value}"`,
                        }), undefined, true);
                    }
                });
            } else if (_.isArrayLikeObject(jsonObject)) {
                const annotationEntryArray = astNode.getFactory().createAnnotationEntryArray();
                _.forEach(jsonObject, (arrayItem) => {
                    annotationEntryArray.addChild(astNode.getFactory().createAnnotationEntry({
                        leftValue: '',
                        rightValue: `"${arrayItem}"`,
                    }), undefined, true);
                });
                astNode.addChild(astNode.getFactory().createAnnotationEntry({ rightValue: annotationEntryArray }),
                                                                                undefined, true);
            } else {
                astNode.addChild(astNode.getFactory().createAnnotationEntry(
                    { rightValue: `"${jsonObject}"` }), undefined, true);
            }
        }
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
                _.isEqual(existingAnnotation.getIdentifier(), annotationIdentifier)) {
                removedChildIndex = existingAnnotation.getParent().getIndexOfChild(existingAnnotation);
                existingAnnotation.getParent().removeChild(existingAnnotation, true);
                return false;
            }

            return true;
        });

        return removedChildIndex;
    }
}

export default SwaggerParser;

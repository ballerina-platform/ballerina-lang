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

import js_yaml from 'js-yaml';
import BallerinaASTFactory from '../ballerina/ast/ballerina-ast-factory';
import DefaultBallerinaASTFactory from '../ballerina/ast/default-ballerina-ast-factory';
import _ from 'lodash';

/**
 * This parser class provides means of merging a swagger JSON to a {@link ServiceDefinition} or a
 * {@link ResourceDefinition}.
 */
class SwaggerParser {

    /**
     * @constructs
     * @param {string|object} swaggerDefintiion - The swagger definition as a string. This can be YAML or JSON.
     * @param {boolean} isYaml is the swagger definition a YAML content or not.
     */
    constructor(swaggerDefintiion, isYaml) {
        if (isYaml) {
            this._swaggerJson = js_yaml.safeLoad(swaggerDefintiion);
        } else {
            this._swaggerJson = swaggerDefintiion;
        }
    }

    /**
     * Merge the {@link _swaggerJson} to service definition.
     * @param {ServiceDefinition} serviceDefinition The service definition.
     */
    mergeToService(serviceDefinition) {
        let serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);

        // Creating ServiceInfo annotation.
        this._createServiceInfoAnnotation(serviceDefinition);
        this._createSwaggerAnnotation(serviceDefinition);
        this._createServiceConfigAnnotation(serviceDefinition);

        // Creating basePath annotation
        if (!_.isUndefined(this._swaggerJson.basePath)) {
            let basePathAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'http', 'BasePath');
            let basePathAnnotation = this._createSimpleAnnotation({
                annotation: { fullPackageName: 'ballerina.net.http', packageName: 'http', identifier: 'BasePath', supported: true },
                swaggerJsonNode: this._swaggerJson.basePath,
            });
            serviceDefinition.addChild(basePathAnnotation, basePathAnnotationIndex, true);
        }

        // Creating consumes annotation
        if (!_.isUndefined(this._swaggerJson.consumes)) {
            let consumesAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger', 'Consumes');
            let consumesAnnotation = this._createSimpleAnnotation({
                annotation: { fullPackageName: 'ballerina.net.http.swagger', packageName: 'swagger', identifier: 'Consumes', supported: true },
                swaggerJsonNode: this._swaggerJson.consumes,
            });
            serviceDefinition.addChild(consumesAnnotation, consumesAnnotationIndex, true);
        }

        // Creating consumes annotation
        if (!_.isUndefined(this._swaggerJson.produces)) {
            let producesAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger', 'Produces');
            let producesAnnotation = this._createSimpleAnnotation({
                annotation: { fullPackageName: 'ballerina.net.http.swagger', identifier: 'Produces', supported: true },
                swaggerJsonNode: this._swaggerJson.produces,
            });
            serviceDefinition.addChild(producesAnnotation, producesAnnotationIndex, true);
        }

        // Updating/Creating resources using path annotation
        _.forEach(this._swaggerJson.paths, (httpMethodObjects, pathString) => {
            _.forEach(httpMethodObjects , (operation, httpMethodAsString) => {
                let existingResource = serviceDefinition.getResourceDefinitions().find((resourceDefinition) => {
                    let resourceName = resourceDefinition.getResourceName();
                    let operationId = operation.operationId;
                    if(resourceName == operationId){
                        return true;
                    }
                    return false;
                });

                //if the operation id does not match we will check if a resource exist with matching path and methos
                if(_.isUndefined(existingResource)){
                    existingResource = serviceDefinition.getResourceDefinitions().find((resourceDefinition) => {
                        let httpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();
                        let pathAnnotation = resourceDefinition.getPathAnnotation(true);
                        return !_.isUndefined(httpMethodAnnotation) && !_.isUndefined(pathAnnotation) &&
                            _.isEqual(pathString, pathAnnotation.getChildren()[0].getRightValue().replace(/"/g, '')) &&
                            _.isEqual(httpMethodAsString, httpMethodAnnotation.getIdentifier().toLowerCase());                       
                    });
                    //if operationId exists set it as resource name.
                    if(operation.operationId){
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

        // if resources with un matching operations we will check if a body exits if not we will remove 

        // otherwise prompt the user to remove
    }

    /**
     * Creates the @ServiceInfo annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     * @private
     */
    _createServiceInfoAnnotation(serviceDefinition) {
        let serviceInfoAnnotation = BallerinaASTFactory.createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger', 
            packageName: 'swagger',
            identifier: 'ServiceInfo'
        });

        if (!_.isUndefined(this._swaggerJson.info.title)) {
            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'title',
                rightValue: JSON.stringify(this._swaggerJson.info.title)
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.version)) {
            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'version',
                rightValue: JSON.stringify(this._swaggerJson.info.version)
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.description)) {
            if (this._swaggerJson.info.description) {
                serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: 'description',
                    rightValue: JSON.stringify(this._swaggerJson.info.description)
                }));
            }
        }

        if (!_.isUndefined(this._swaggerJson.info.termsOfService)) {
            if (this._swaggerJson.info.termsOfService) {
                serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: 'termsOfService',
                    rightValue: JSON.stringify(this._swaggerJson.info.termsOfService)
                }));
            }
        }

        if (!_.isUndefined(this._swaggerJson.info.contact)) {
            let contactAnnotation = this._createSimpleAnnotation({
                annotation: { packageName: 'swagger', identifier: 'contact', supported: true },
                swaggerJsonNode: this._swaggerJson.info.contact,
            });

            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'contact',
                rightValue: contactAnnotation
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.license)) {
            let licenseAnnotation = this._createSimpleAnnotation({
                annotation: { packageName: 'swagger', identifier: 'contact', supported: true },
                swaggerJsonNode: this._swaggerJson.info.license,
            });

            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'contact',
                rightValue: licenseAnnotation
            }));
        }

        // TODO : Create externalDocs, tag, organization, developers.

        let serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let serviceInfoAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger', 'ServiceInfo');
        serviceDefinition.addChild(serviceInfoAnnotation, serviceInfoAnnotationIndex, true);
    }

    /**
     * Creates the @Swagger annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     * @private
     */
    _createSwaggerAnnotation(serviceDefinition) {
        let swaggerAnnotation = BallerinaASTFactory.createAnnotation({ 
            fullPackageName: 'ballerina.net.http.swagger', 
            packageName: 'swagger', 
            identifier: 'Swagger' 
        });
        swaggerAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
            leftValue: 'version',
            rightValue: JSON.stringify(this._swaggerJson.swagger)
        }));
        let serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let swaggerAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger', 'Swagger');
        serviceDefinition.addChild(swaggerAnnotation, swaggerAnnotationIndex, true);
    }

    /**
     * Creates the @ServiceConfig annotation for a given {@link ServiceDefinition} using the {@link _swaggerJson}.
     * @param {ServiceDefinition} serviceDefinition The service definition
     * @private
     */
    _createServiceConfigAnnotation(serviceDefinition) {
        let serviceConfigAnnotation = BallerinaASTFactory.createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger', 
            packageName: 'swagger',
            identifier: 'ServiceConfig'
        });

        if (!_.isUndefined(this._swaggerJson.host)) {
            serviceConfigAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'host',
                rightValue: JSON.stringify(this._swaggerJson.host)
            }));
        }

        this._defaultSwaggerToASTConverter(this._swaggerJson.schemes, serviceConfigAnnotation);

        let serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let swaggerAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger', 'ServiceConfig');
        serviceDefinition.addChild(serviceConfigAnnotation, swaggerAnnotationIndex, true);
    }
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
        let pathAnnotation = resourceDefinition.getPathAnnotation(true);
        pathAnnotation.getChildren()[0].setRightValue(JSON.stringify(pathString), {doSilently: true});
        let methodAnnotation = resourceDefinition.getHttpMethodAnnotation();
        methodAnnotation.setIdentifier(httpMethodAsString.toUpperCase(), {doSilently: true});

        let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);

        // Creating consumes annotation
        if (!_.isUndefined(httpMethodJsonObject.consumes)) {
            let consumesAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'Consumes');
            let consumesAnnotation = this._createSimpleAnnotation({
                annotation: { packageName: 'swagger', identifier: 'Consumes', supported: true },
                swaggerJsonNode: httpMethodJsonObject.consumes,
            });
            resourceDefinition.addChild(consumesAnnotation, consumesAnnotationIndex, true);
        }

        // Creating consumes annotation
        if (!_.isUndefined(httpMethodJsonObject.produces)) {
            let producesAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'Produces');
            let producesAnnotation = this._createSimpleAnnotation({
                annotation: { packageName: 'swagger', identifier: 'Produces', supported: true },
                swaggerJsonNode: httpMethodJsonObject.produces,
            });
            resourceDefinition.addChild(producesAnnotation, producesAnnotationIndex, true);
        }

        this._createResponsesAnnotation(resourceDefinition, httpMethodJsonObject);
        this._createParametersAnnotation(resourceDefinition, httpMethodJsonObject);
    }

    /**
     * Creates the @ResourceConfig annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     * @private
     */
    _createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject) {
        let resourceConfigAnnotation = resourceDefinition.getFactory().createAnnotation({
            fullPackageName: 'ballerina.net.http.swagger', 
            packageName: 'swagger',
            identifier: 'ResourceConfig'
        });

        let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let resourceConfigAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'ResourceConfig');
        resourceDefinition.addChild(resourceConfigAnnotation, resourceConfigAnnotationIndex, true);
    }

    /**
     * Creates the @Responses annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     * @private
     */
    _createResponsesAnnotation(resourceDefinition, httpMethodJsonObject) {
        if (!_.isUndefined(httpMethodJsonObject.responses)) {
            let responsesAnnotation = BallerinaASTFactory.createAnnotation({ packageName: 'swagger', identifier: 'Responses' });

            // Creating the responses array entry
            let responsesAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
            let responseAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({ rightValue: responsesAnnotationArray });
            responsesAnnotation.addChild(responseAnnotationEntry);
            _.forEach(httpMethodJsonObject.responses, (codeObj, code) => {
                let responseAnnotation = BallerinaASTFactory.createAnnotation({ packageName: 'swagger', identifier: 'Response' });

                let codeAnnotationEntry = resourceDefinition.getFactory().createAnnotationEntry({
                    leftValue: 'code',
                    rightValue: JSON.stringify(code)
                });
                responseAnnotation.addChild(codeAnnotationEntry);

                // description
                let descriptionAnnotationEntry = resourceDefinition.getFactory().createAnnotationEntry({
                    leftValue: 'description',
                    rightValue: JSON.stringify(codeObj.description)
                });
                responseAnnotation.addChild(descriptionAnnotationEntry);

                responsesAnnotationArray.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: '',
                    rightValue: responseAnnotation
                }));
            });

            let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
            let resourceConfigAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'Responses');
            resourceDefinition.addChild(responsesAnnotation, resourceConfigAnnotationIndex, true);
        }
    }

    /**
     * Creates the @ParametersInfo annotation for a given {@link ResourceDefinition} using the http method object of the
     * swagger JSON.
     * @param {ServiceDefinition} resourceDefinition The resource definition to be updated.
     * @private
     */
    _createParametersAnnotation(resourceDefinition, httpMethodJsonObject) {
        if (!_.isUndefined(httpMethodJsonObject.parameters)) {
            let parametersAnnotation = BallerinaASTFactory.createAnnotation({
                fullPackageName: 'ballerina.net.http.swagger', 
                packageName: 'swagger',
                identifier: 'ParametersInfo'
            });

            // Creating the responses array entry
            let parametersAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
            let parameterAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({ rightValue: parametersAnnotationArray });
            parametersAnnotation.addChild(parameterAnnotationEntry);
            _.forEach(httpMethodJsonObject.parameters, parameter => {
                let responseAnnotation = BallerinaASTFactory.createAnnotation({
                    packageName: 'swagger',
                    identifier: 'ParameterInfo'
                });

                if (!_.isUndefined(parameter.in)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'in',
                        rightValue: JSON.stringify(parameter.in)
                    }));
                }

                if (!_.isUndefined(parameter.name)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'name',
                        rightValue: JSON.stringify(parameter.name)
                    }));
                }

                if (!_.isUndefined(parameter.description)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'description',
                        rightValue: JSON.stringify(parameter.description)
                    }));
                }

                if (!_.isUndefined(parameter.required)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'required',
                        rightValue: parameter.required
                    }));
                }

                if (!_.isUndefined(parameter.allowEmptyValue)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'allowEmptyValue',
                        rightValue: JSON.stringify(parameter.allowEmptyValue)
                    }));
                }

                if (!_.isUndefined(parameter.type)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'type',
                        rightValue: JSON.stringify(parameter.type)
                    }));
                }

                if (!_.isUndefined(parameter.format)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'format',
                        rightValue: JSON.stringify(parameter.format)
                    }));
                }

                if (!_.isUndefined(parameter.collectionFormat)) {
                    responseAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                        leftValue: 'collectionFormat',
                        rightValue: JSON.stringify(parameter.collectionFormat)
                    }));
                }

                parametersAnnotationArray.addChild(BallerinaASTFactory.createAnnotationEntry({
                    leftValue: '',
                    rightValue: responseAnnotation
                }));
            });

            let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
            let resourceConfigAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'ParametersInfo');
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
        let newResourceDefinition = DefaultBallerinaASTFactory.createResourceDefinition();

        //if an operation id is defined set it as resource name.
        if(httpMethodJsonObject.operationId){
            newResourceDefinition.setResourceName(httpMethodJsonObject.operationId);
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
                        let annotationNode = BallerinaASTFactory.createAnnotation({ identifier: key });
                        this._defaultSwaggerToASTConverter(value, annotationNode);
                        astNode.addChild(annotationNode, undefined, true);
                    } else if (_.isArrayLikeObject(value)) {
                        let entryArrayNode = BallerinaASTFactory.createAnnotationEntryArray();
                        _.forEach(value, arrayValue => {
                            this._defaultSwaggerToASTConverter(arrayValue, entryArrayNode);
                        });
                        astNode.addChild(BallerinaASTFactory.createAnnotationEntry({
                            leftValue: key,
                            rightValue: entryArrayNode
                        }), undefined, true);
                    } else {
                        astNode.addChild(BallerinaASTFactory.createAnnotationEntry({
                            leftValue: key,
                            rightValue: '\"' + value + '\"'
                        }), undefined, true);
                    }
                });
            } else if (_.isArrayLikeObject(jsonObject)) {
                let annotationEntryArray = astNode.getFactory().createAnnotationEntryArray();
                _.forEach(jsonObject, arrayItem => {
                    annotationEntryArray.addChild(astNode.getFactory().createAnnotationEntry({
                        leftValue: '',
                        rightValue: '\"' + arrayItem + '\"'
                    }), undefined, true);
                });
                astNode.addChild(astNode.getFactory().createAnnotationEntry({ rightValue: annotationEntryArray }), undefined, true);
            } else {
                astNode.addChild(astNode.getFactory().createAnnotationEntry(
                    { rightValue: '\"' + jsonObject + '\"' }), undefined, true);
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
    _removeExistingAnnotation(existingAnnotations, annotationPackage, annotationIdentifier) {
        let removedChildIndex = _.size(existingAnnotations);
        _.forEach(existingAnnotations, existingAnnotation => {
            if (_.isEqual(existingAnnotation.getPackageName(), annotationPackage) &&
                _.isEqual(existingAnnotation.getIdentifier(), annotationIdentifier)) {
                removedChildIndex = existingAnnotation.getParent().getIndexOfChild(existingAnnotation);
                existingAnnotation.getParent().removeChild(existingAnnotation, true);
                return false;
            }
        });

        return removedChildIndex;
    }
}

export default SwaggerParser;

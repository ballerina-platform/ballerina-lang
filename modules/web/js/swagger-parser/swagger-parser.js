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

class SwaggerParser {

    /**
     * @constructs
     * @param {string} swaggerDefintiion - The swagger defintion as a string. This can be YAML or JSON.
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
     *
     */
    mergeToService(serviceDefinition) {
        let serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);

        // Creating ServiceInfo annotation.
        this._createServiceInfoAnnotation(serviceDefinition);
        this._createSwaggerAnnotation(serviceDefinition);
        this._createServiceConfigAnnotation(serviceDefinition);

        // Creating basePath annotation
        let basePathAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'http', 'BasePath');
        let basePathAnnotation = this._createSimpleAnnotation({
            annotation: {packageName: 'http', identifier: 'BasePath', supported: true},
            swaggerJsonNode: this._swaggerJson.basePath,
        });
        serviceDefinition.addChild(basePathAnnotation, basePathAnnotationIndex, true);

        // Creating consumes annotation
        if (!_.isUndefined(this._swaggerJson.consumes)) {
            let consumesAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'http', 'Consumes');
            let consumesAnnotation = this._createSimpleAnnotation({
                annotation: {packageName: 'http', identifier: 'consumes', supported: true},
                swaggerJsonNode: this._swaggerJson.consumes,
            });
            serviceDefinition.addChild(consumesAnnotation, consumesAnnotationIndex, true);
        }

        // Creating consumes annotation
        if (!_.isUndefined(this._swaggerJson.produces)) {
            let producesAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'http', 'produces');
            let producesAnnotation = this._createSimpleAnnotation({
                annotation: {packageName: 'http', identifier: 'produces', supported: true},
                swaggerJsonNode: this._swaggerJson.produces,
            });
            serviceDefinition.addChild(producesAnnotation, producesAnnotationIndex, true);
        }

        // Updating/Creating resources using path annotation
        _.forEach(this._swaggerJson.paths, (httpMethodObject, pathString) => {
            _.forEach(serviceDefinition.getResourceDefinitions(), resourceDefinition => {
                let httpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();
                let pathAnnotation = resourceDefinition.getPathAnnotation();

                _.forEach(Object.keys(httpMethodObject), httpMethodAsString => {
                    if (!_.isUndefined(httpMethodAnnotation) && !_.isUndefined(pathAnnotation) &&
                        _.isEqual(pathString, pathAnnotation.getChildren()[0].getRightValue().toLowerCase().replace(/"/g, '')) &&
                        _.isEqual(httpMethodAsString, httpMethodAnnotation.getIdentifier().toLowerCase())) {
                        this._mergeToResource(resourceDefinition, pathString, httpMethodAsString, httpMethodObject[httpMethodAsString]);
                    } else {
                        this._createNewResource(serviceDefinition, pathString, httpMethodAsString, httpMethodObject[httpMethodAsString]);
                    }
                });
            });
        });
    }

    _createServiceInfoAnnotation(serviceDefinition) {
        let serviceInfoAnnotation = BallerinaASTFactory.createAnnotation({
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
                annotation: {packageName: 'swagger', identifier: 'contact', supported: true},
                swaggerJsonNode: this._swaggerJson.info.contact,
            });

            serviceInfoAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
                leftValue: 'contact',
                rightValue: contactAnnotation
            }));
        }

        if (!_.isUndefined(this._swaggerJson.info.license)) {
            let licenseAnnotation = this._createSimpleAnnotation({
                annotation: {packageName: 'swagger', identifier: 'contact', supported: true},
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

    _createSwaggerAnnotation(serviceDefinition) {
        let swaggerAnnotation = BallerinaASTFactory.createAnnotation({packageName: 'swagger', identifier: 'Swagger'});
        swaggerAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({
            leftValue: 'version',
            rightValue: JSON.stringify(this._swaggerJson.swagger)
        }));
        let serviceDefinitionAnnotations = serviceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let swaggerAnnotationIndex = this._removeExistingAnnotation(serviceDefinitionAnnotations, 'swagger', 'Swagger');
        serviceDefinition.addChild(swaggerAnnotation, swaggerAnnotationIndex, true);
    }

    _createServiceConfigAnnotation(serviceDefinition) {
        let serviceConfigAnnotation = BallerinaASTFactory.createAnnotation({
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

    _mergeToResource(resourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        // Create path annotation
        // This is not needed to be done as the path value is kept mapped from the swagger editor to the composer AST.

        // Creating http method annotation
        // This is not needed to be done as the http method value is kept mapped from the swagger editor to the composer
        // AST.

        // this._createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject);

        let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);

        // Creating consumes annotation
        if (!_.isUndefined(httpMethodJsonObject.consumes)) {
            let consumesAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'http', 'Consumes');
            let consumesAnnotation = this._createSimpleAnnotation({
                annotation: {packageName: 'http', identifier: 'consumes', supported: true},
                swaggerJsonNode: httpMethodJsonObject.consumes,
            });
            resourceDefinition.addChild(consumesAnnotation, consumesAnnotationIndex, true);
        }

        // Creating consumes annotation
        if (!_.isUndefined(httpMethodJsonObject.produces)) {
            let producesAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'http', 'produces');
            let producesAnnotation = this._createSimpleAnnotation({
                annotation: {packageName: 'http', identifier: 'produces', supported: true},
                swaggerJsonNode: httpMethodJsonObject.produces,
            });
            resourceDefinition.addChild(producesAnnotation, producesAnnotationIndex, true);
        }

        this._createResponsesAnnotation(resourceDefinition, httpMethodJsonObject);

    }

    _createResourceConfigAnnotation(resourceDefinition, httpMethodJsonObject) {
        let resourceConfigAnnotation = resourceDefinition.getFactory().createAnnotation({
            packageName: 'swagger',
            identifier: 'ResourceConfig'
        });

        let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let resourceConfigAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'ResourceConfig');
        resourceDefinition.addChild(resourceConfigAnnotation, resourceConfigAnnotationIndex, true);
    }

    _createResponsesAnnotation(resourceDefinition, httpMethodJsonObject) {
        let responsesAnnotation = resourceDefinition.getFactory().createAnnotation({
            packageName: 'swagger',
            identifier: 'Responses'
        });
        let responseEntryArray = resourceDefinition.getFactory().createAnnotationEntryArray();
        responsesAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({rightValue: responseEntryArray}));
        _.forEach(httpMethodJsonObject.responses, (codeObj, code) => {
            let responseAnnotation = resourceDefinition.getFactory().createAnnotation({
                packageName: 'swagger',
                identifier: 'Response'
            });
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

            responseEntryArray.addChild(responseAnnotation);
        });

        let resourceDefinitionAnnotations = resourceDefinition.getChildrenOfType(BallerinaASTFactory.isAnnotation);
        let resourceConfigAnnotationIndex = this._removeExistingAnnotation(resourceDefinitionAnnotations, 'swagger', 'Responses');
        resourceDefinition.addChild(responsesAnnotation, resourceConfigAnnotationIndex, true);
    }

    _createNewResource(serviceDefinition, pathString, httpMethodAsString, httpMethodJsonObject) {
        let newResourceDefinition = DefaultBallerinaASTFactory.createResourceDefinition();

        // Create path annotation
        let pathAnnotation = BallerinaASTFactory.createAnnotation({packageName: 'http', identifier: 'Path'});
        pathAnnotation.addChild(BallerinaASTFactory.createAnnotationEntry({rightValue: '\"' + pathString + '\"'}));
        newResourceDefinition.addChild(pathAnnotation, undefined, true);

        // Creating http method annotation
        let httpMethodAnnotation = BallerinaASTFactory.createAnnotation({
            packageName: 'http',
            identifier: httpMethodAsString.toUpperCase()
        });
        newResourceDefinition.addChild(httpMethodAnnotation, undefined, true);

        this._mergeToResource(newResourceDefinition, pathString, httpMethodAsString, httpMethodJsonObject);
        serviceDefinition.addChild(newResourceDefinition, undefined, true, true);
        newResourceDefinition.generateUniqueIdentifiers();
    }

    _defaultSwaggerToASTConverter(jsonObject, astNode) {
        if (!_.isUndefined(jsonObject)) {
            if (_.isPlainObject(jsonObject)) {
                _.forEach(jsonObject, (value, key) => {
                    if (_.isPlainObject(value)) {
                        let annotationNode = BallerinaASTFactory.createAnnotation({identifier: key});
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
                astNode.addChild(astNode.getFactory().createAnnotationEntry({rightValue: annotationEntryArray}), undefined, true);
            } else {
                astNode.addChild(astNode.getFactory().createAnnotationEntry(
                    {rightValue: '\"' + jsonObject + '\"'}), undefined, true);
            }
        }
    }

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

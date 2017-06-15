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
import _ from 'lodash';
import AbstractSwaggerJsonGenVisitor from './abstract-swagger-json-gen-visitor';

/**
 * The {@link ResourceDefinition} visitor for generating its JSON swagger.
 * @extends AbstractSwaggerJsonGenVisitor
 */
class ResourceDefinitionVisitor extends AbstractSwaggerJsonGenVisitor {

    canVisitResourceDefinition() {
        return true;
    }

    beginVisitResourceDefinition(resourceDefinition) {
        // Creating path element that maps to the path annotation of the source.
        const pathAnnotation = resourceDefinition.getPathAnnotation();
        let pathValue = `/${resourceDefinition.getResourceName()}`; // default path
        if (!_.isUndefined(pathAnnotation)) {
            pathValue = pathAnnotation.getChildren()[0].getRightValue().replace(/"/g, '');
        }

        // Check if path already exists
        if (!_.has(this.getSwaggerJson(), pathValue)) {
            _.set(this.getSwaggerJson(), pathValue, {});
        }

        const pathJson = _.get(this.getSwaggerJson(), pathValue);

        // Creating http method element that maps to the http:<Method> annotation.
        const httpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();
        const httpMethodValue = !_.isUndefined(httpMethodAnnotation) ?
            httpMethodAnnotation.getIdentifier().toLowerCase() : 'get';
        _.set(pathJson, httpMethodValue, {});

        const httpMethodJson = _.get(pathJson, httpMethodValue);

        // Creating annotations
        let existingAnnotations = resourceDefinition.getChildrenOfType(resourceDefinition.getFactory().isAnnotation);
        // Removing http:Path and http:<Method> annotations
        existingAnnotations = _.remove(existingAnnotations.slice(), existingAnnotation =>
                    (!(!_.isUndefined(httpMethodAnnotation) &&
                        (_.isEqual(existingAnnotation.getPackageName(), httpMethodAnnotation.getPackageName()) &&
                        _.isEqual(existingAnnotation.getIdentifier(), httpMethodAnnotation.getIdentifier()))) ||
                    (!_.isUndefined(pathAnnotation) &&
                        (_.isEqual(existingAnnotation.getPackageName(), pathAnnotation.getPackageName()) &&
                        _.isEqual(existingAnnotation.getIdentifier(), pathAnnotation.getIdentifier())))));

        // Creating default annotations
        _.set(httpMethodJson, 'operationId', resourceDefinition.getResourceName());
        _.set(httpMethodJson, 'responses.default.description', 'Default Response');

        // Creating the annotation
        _.forEach(existingAnnotations, (existingAnnotation) => {
            if (_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'ResourceConfig')) {
                this.parseResourceConfigAnnotation(existingAnnotation, httpMethodJson);
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'ResourceInfo')) {
                this.parseResourceInfoAnnotation(existingAnnotation, httpMethodJson);
            } else if ((_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'ParametersInfo'))) {
                this.parseParameterInfoAnnotation(existingAnnotation, httpMethodJson);
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'Responses')) {
                this.parseResponsesAnnotation(existingAnnotation, httpMethodJson);
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'http') && _.isEqual(existingAnnotation.getIdentifier(), 'Consumes')) {
                const consumesAnnotationEntryArray = existingAnnotation.getChildren()[0].getRightValue();
                const consumes = [];
                _.forEach(consumesAnnotationEntryArray.getChildren(), (consumesAnnotationEntry) => {
                    consumes.push(this.removeDoubleQuotes(consumesAnnotationEntry.getRightValue()));
                });
                _.set(httpMethodJson, 'consumes', consumes);
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'http') && _.isEqual(existingAnnotation.getIdentifier(), 'Produces')) {
                const producesAnnotationEntryArray = existingAnnotation.getChildren()[0].getRightValue();
                const produces = [];
                _.forEach(producesAnnotationEntryArray.getChildren(), (producesAnnotationEntry) => {
                    produces.push(this.removeDoubleQuotes(producesAnnotationEntry.getRightValue()));
                });
                _.set(httpMethodJson, 'produces', produces);
            }
        });

        // Updating json with related to non-annotation models.
        this.addParametersAsAnnotations(resourceDefinition, httpMethodJson);
    }

    visitResourceDefinition() {
    }

    endVisitResourceDefinition() {
    }

    /**
     * Creates the swagger JSON using the @ResourceConfig annotation..
     * @param {Annotation} existingAnnotation The @ResourceConfig annotation of the resource definition.
     * @param {object} httpMethodJson The swagger json to be updated.
     */
    parseResourceConfigAnnotation(existingAnnotation, httpMethodJson) {
        // Setting values.
        _.forEach(existingAnnotation.getChildren(), (annotationEntry) => {
            if (_.isEqual(annotationEntry.getLeftValue(), 'schemes')) {
                const schemesAnnotationEntryArray = annotationEntry.getRightValue();
                const schemes = [];
                _.forEach(schemesAnnotationEntryArray.getChildren(), (schemesAnnotationEntry) => {
                    schemes.push(schemesAnnotationEntry.getRightValue());
                });
                _.set(httpMethodJson, 'schemes', schemes);
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'authorizations')) {
                // TODO : Implement authorizations
            }
        });
    }

    /**
     * Creates the swagger JSON using the @ResourceInfo annotation..
     * @param {Annotation} resourceInfoAnnotation The @ResourceInfo annotation of the resource definition.
     * @param {object} httpMethodJson The swagger json to be updated.
     */
    parseResourceInfoAnnotation(resourceInfoAnnotation, httpMethodJson) {
        // Setting values.
        _.forEach(resourceInfoAnnotation.getChildren(), (annotationEntry) => {
            if (_.isEqual(annotationEntry.getLeftValue(), 'tags')) {
                const tagsAnnotationEntryArray = annotationEntry.getRightValue();
                const tags = [];
                _.forEach(tagsAnnotationEntryArray.getChildren(), (tagsAnnotationEntry) => {
                    tags.push(tagsAnnotationEntry.getRightValue());
                });
                _.set(httpMethodJson, 'tags', tags);
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'summary')) {
                _.set(httpMethodJson, 'summary', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'description')) {
                _.set(httpMethodJson, 'description', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'operationId')) {
                _.set(httpMethodJson, 'operationId', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'externalDocs')) {
                // TODO : Implement externalDocs
            }
        });
    }

    /**
     * Creates the swagger JSON using the @ParametersInfo annotation. Adds missing path params as they are required.
     * @param {Annotation} parametersAnnotation The @ParametersInfo annotation of the resource definition.
     * @param {object} httpMethodJson The swagger json to be updated.
     * @param {ResourceDefinition} resourceDefinition The resource definition which is being visited
     */
    parseParameterInfoAnnotation(parametersAnnotation, httpMethodJson) {
        const parametersAnnotationArray = parametersAnnotation.getChildren()[0].getRightValue();
        const parameters = [];
        _.forEach(parametersAnnotationArray.getChildren(), (parametersAnnotationArrayEntry) => {
            const tempParameterObj = {};
            _.forEach(parametersAnnotationArrayEntry.getRightValue().getChildren(), (responseAnnotationEntry) => {
                if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'in')) {
                    _.set(tempParameterObj, 'in', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'name')) {
                    _.set(tempParameterObj, 'name', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'description')) {
                    _.set(tempParameterObj, 'description', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'required')) {
                    if (_.isString(responseAnnotationEntry.getRightValue())) {
                        _.set(tempParameterObj, 'required', responseAnnotationEntry.getRightValue() === 'true');
                    } else {
                        _.set(tempParameterObj, 'required', responseAnnotationEntry.getRightValue());
                    }
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'allowEmptyValue')) {
                    _.set(tempParameterObj, 'allowEmptyValue', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'type')) {
                    _.set(tempParameterObj, 'type', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'format')) {
                    _.set(tempParameterObj, 'format', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'schema')) {
                    _.set(tempParameterObj, 'schema', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'collectionFormat')) {
                    _.set(tempParameterObj, 'collectionFormat', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'items')) {
                    // TODO : Implement items annotation.
                }
            });

            parameters.push(tempParameterObj);
        });

        _.set(httpMethodJson, 'parameters', parameters);
    }

    /**
     * Creates the swagger JSON using the @Responses annotation..
     * @param {Annotation} responsesAnnotation The @Responses annotation of the resource definition.
     * @param {object} httpMethodJson The swagger json to be updated.
     */
    parseResponsesAnnotation(responsesAnnotation, httpMethodJson) {
        // Removing default responses
        _.unset(httpMethodJson, 'responses');

        // Creating the responses according the responses annotation.
        const responsesAnnotationArray = responsesAnnotation.getChildren()[0].getRightValue();
        const responses = {};
        _.forEach(responsesAnnotationArray.getChildren(), (responseAnnotationArrayEntry) => {
            const tempCodesObj = {};
            const codesObjects = [];
            _.forEach(responseAnnotationArrayEntry.getRightValue().getChildren(), (responseAnnotationEntry) => {
                if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'code')) {
                    _.set(tempCodesObj, 'code', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'description')) {
                    _.set(tempCodesObj, 'description', this.removeDoubleQuotes(responseAnnotationEntry.getRightValue()));
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'response')) {
                    // TODO : support response annotation
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'headers')) {
                    // TODO : support headers annotation
                } else if (_.isEqual(responseAnnotationEntry.getLeftValue(), 'examples')) {
                    // TODO : support examples annotation
                }
            });

            codesObjects.push(tempCodesObj);

            _.forEach(codesObjects, (codeObj) => {
                const responseCode = codeObj.code;
                _.unset(codeObj, 'code');
                _.set(responses, responseCode, codeObj);
            });
        });

        _.set(httpMethodJson, 'responses', responses);
    }

    /**
     * Adds the parameters of the resource definition to "parameters" array in the swagger json.
     *
     * @param {ResourceDefinition} resourceDefinition The resource definition which has the parameters.
     * @param {Object} httpMethodJson The http method json which needs to be updated.
     *
     * @memberof ResourceDefinitionVisitor
     */
    addParametersAsAnnotations(resourceDefinition, httpMethodJson) {
        // Ignoring the first argument as it is "message" by default.
        if (resourceDefinition.getArguments().length > 1) {
            let parametersArray = [];
            if (_.has(httpMethodJson, 'parameters')) {
                parametersArray = _.get(httpMethodJson, 'parameters');
            } else {
                _.set(httpMethodJson, 'parameters', parametersArray);
            }

            for (const [index, param] of resourceDefinition.getArguments().entries()) {
                if (index > 0 && param.getChildren().length > 0 && param.getChildren()[0].getChildren().length > 0 &&
                        resourceDefinition.getFactory().isAnnotationEntry(param.getChildren()[0].getChildren()[0])) {
                    let paramAlreadyExists = false;
                    for (const existingParameter of parametersArray) {
                        // Check if parameter already exists.
                        const annotation = param.getChildren()[0];
                        const annotationValue = annotation.getChildren()[0].getRightValue();
                        if (this.removeDoubleQuotes(annotationValue) === existingParameter.name) {
                            paramAlreadyExists = true;
                        }
                    }

                    // Add if parameter does not exist
                    if (!paramAlreadyExists) {
                        // Setting values by the type of the parameter.
                        const annotation = param.getChildren()[0];
                        if (resourceDefinition.getFactory().isAnnotation(annotation)) {
                            const tempParameterObj = {};
                            const annotationValue = annotation.getChildren()[0].getRightValue();

                            _.set(tempParameterObj, 'name', this.removeDoubleQuotes(annotationValue));
                            if (annotation.getIdentifier() === 'PathParam') {
                                _.set(tempParameterObj, 'required', true);
                                _.set(tempParameterObj, 'in', 'path');
                            } else if (annotation.getIdentifier() === 'QueryParam') {
                                _.set(tempParameterObj, 'required', false);
                                _.set(tempParameterObj, 'in', 'query');
                            }

                            // Setting the type of the parameter.
                            if (annotationValue.startsWith('\"')) {
                                _.set(tempParameterObj, 'type', 'string');
                            } else if (!_.isNaN(parseInt(annotationValue))) {
                                _.set(tempParameterObj, 'type', 'integer');
                            } else if (annotationValue === 'true' || annotationValue === 'false') {
                                _.set(tempParameterObj, 'type', 'boolean');
                            } else if (annotationValue.includes('[') && annotationValue.includes(']')) {
                                _.set(tempParameterObj, 'type', 'array');
                            }

                            parametersArray.push(tempParameterObj);
                        }
                    }
                }
            }
        }
    }
}

export default ResourceDefinitionVisitor;

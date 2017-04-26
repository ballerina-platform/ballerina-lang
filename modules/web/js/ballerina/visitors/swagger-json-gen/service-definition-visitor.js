/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import log from 'log';
import AbstractSwaggerJsonGenVisitor from './abstract-swagger-json-gen-visitor';
import ResourceDefinitionVisitor from './resource-definition-visitor';

/**
 * @param parent
 * @constructor
 */
class ServiceDefinitionVisitor extends AbstractSwaggerJsonGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitServiceDefinition(serviceDefinition) {
        return true;
    }

    beginVisitServiceDefinition(serviceDefinition) {
        let existingAnnotations = serviceDefinition.getChildrenOfType(serviceDefinition.getFactory().isAnnotation);

        _.forEach(existingAnnotations, existingAnnotation => {
            if (_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'ServiceInfo')) {
                this.parseServiceInfoAnnotation(existingAnnotation, this.getSwaggerJson());
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'http') && _.isEqual(existingAnnotation.getIdentifier(), 'BasePath')) {
                _.set(this.getSwaggerJson(), 'basePath', existingAnnotation.getChildren()[0].getRightValue().replace(/"/g, ''));
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'Swagger')) {
                _.set(this.getSwaggerJson(), 'swagger', existingAnnotation.getChildren()[0].getRightValue().replace(/"/g, ''));
                // TODO : impl extension
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'swagger') && _.isEqual(existingAnnotation.getIdentifier(), 'ServiceConfig')) {
                this.parseServiceConfigAnnotation(existingAnnotation, this.getSwaggerJson());
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'http') && _.isEqual(existingAnnotation.getIdentifier(), 'Consumes')) {
                let consumesAnnotationEntryArray = existingAnnotation.getRightValue();
                let consumes = [];
                _.forEach(consumesAnnotationEntryArray.getChildren(), schemesAnnotationEntry => {
                    consumes.push(schemesAnnotationEntry.getRightValue());
                });
                _.set(this.getSwaggerJson(), 'consumes', consumes);
            } else if (_.isEqual(existingAnnotation.getPackageName(), 'http') && _.isEqual(existingAnnotation.getIdentifier(), 'Produces')) {
                let producesAnnotationEntryArray = existingAnnotation.getRightValue();
                let produces = [];
                _.forEach(producesAnnotationEntryArray.getChildren(), schemesAnnotationEntry => {
                    produces.push(schemesAnnotationEntry.getRightValue());
                });
                _.set(this.getSwaggerJson(), 'produces', produces);
            }
        });

        // Setting required 'info' annotation
        if (_.isUndefined(this.getSwaggerJson().info)) {
            this.getSwaggerJson().info = {version: '1.0.0', title: 'Sample Service'};
        } else {
            if (_.isUndefined(this.getSwaggerJson().info.version)) {
                this.getSwaggerJson().info.version = '1.0.0';
            }
            if (_.isUndefined(this.getSwaggerJson().info.title)) {
                this.getSwaggerJson().info.title = 'Sample Service';
            }
        }

        log.debug('Begin Visit Service Definition');
    }

    visitServiceDefinition(serviceDefinition) {
        log.debug('Visit Service Definition');
    }

    endVisitServiceDefinition(serviceDefinition) {
        log.debug('End Visit Service Definition');
    }

    visitResourceDefinition(resourceDefinition) {
        const resourceDefinitionVisitor = new ResourceDefinitionVisitor(this);
        this.getSwaggerJson().paths = _.get(this.getSwaggerJson(), 'paths', {});
        resourceDefinitionVisitor.setSwaggerJson(this.getSwaggerJson().paths);
        resourceDefinition.accept(resourceDefinitionVisitor);
    }

    parseServiceInfoAnnotation(existingAnnotation, swaggerJson) {
        // Setting default values
        _.set(swaggerJson, 'info.title', 'Sample Service');
        _.set(swaggerJson, 'info.version', '1.0.0');

        // Setting values.
        _.forEach(existingAnnotation.getChildren(), annotationEntry => {
            if (_.isEqual(annotationEntry.getLeftValue(), 'title')) {
                _.set(swaggerJson, 'info.title', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'version')) {
                _.set(swaggerJson, 'info.version', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'description')) {
                _.set(swaggerJson, 'info.description', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'termsOfService')) {
                _.set(swaggerJson, 'info.termsOfService', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'contact')) {
                let contactAnnotation = annotationEntry.getRightValue();
                _.forEach(contactAnnotation.getChildren(), contactAnnotationEntry => {
                    if (_.isEqual(contactAnnotationEntry.getLeftValue(), 'name')) {
                        _.set(swaggerJson, 'info.contact.name', this.removeDoubleQuotes(annotationEntry.getRightValue()));
                    } else if (_.isEqual(contactAnnotationEntry.getLeftValue(), 'email')) {
                        _.set(swaggerJson, 'info.contact.email', this.removeDoubleQuotes(annotationEntry.getRightValue()));
                    } else if (_.isEqual(contactAnnotationEntry.getLeftValue(), 'url')) {
                        _.set(swaggerJson, 'info.contact.url', this.removeDoubleQuotes(annotationEntry.getRightValue()));
                    }
                });
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'license')) {
                let licenseAnnotation = annotationEntry.getRightValue();
                _.forEach(licenseAnnotation.getChildren(), licenseAnnotationEntry => {
                    if (_.isEqual(licenseAnnotationEntry.getLeftValue(), 'name')) {
                        _.set(swaggerJson, 'info.license.name', this.removeDoubleQuotes(annotationEntry.getRightValue()));
                    } else if (_.isEqual(licenseAnnotationEntry.getLeftValue(), 'url')) {
                        _.set(swaggerJson, 'info.license.url', this.removeDoubleQuotes(annotationEntry.getRightValue()));
                    }
                });
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'externalDoc')) {
                // TODO : impl externalDoc
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'tags')) {
                // TODO : impl tag
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'organization')) {
                // TODO : impl organization
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'developers')) {
                // TODO : impl developers
            }
        });
    }

    parseServiceConfigAnnotation(existingAnnotation, swaggerJson) {
        // Setting values.
        _.forEach(existingAnnotation.getChildren(), annotationEntry => {
            if (_.isEqual(annotationEntry.getLeftValue(), 'host')) {
                _.set(swaggerJson, 'host', this.removeDoubleQuotes(annotationEntry.getRightValue()));
            } else if (_.isEqual(annotationEntry.getLeftValue(), 'schemes')) {
                let schemesAnnotationEntryArray = annotationEntry.getRightValue();
                let schemes = [];
                _.forEach(schemesAnnotationEntryArray.getChildren(), schemesAnnotationEntry => {
                    schemes.push(schemesAnnotationEntry.getRightValue());
                });
                _.set(swaggerJson, 'schemes', schemes);
            }

            // TODO : security definitions/authorizations.
        });
    }


}

export default ServiceDefinitionVisitor;

/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.ballerina.swagger.convertor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.util.Json;
import org.ballerinalang.ballerina.swagger.convertor.ConverterUtils;
import org.ballerinalang.ballerina.swagger.convertor.service.model.Organization;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.net.http.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;

/**
 * SwaggerServiceMapper provides functionality for reading and writing Swagger, either to and from ballerina service, or
 * to, as well as related functionality for performing conversions between swagger and ballerina.
 */
public class SwaggerServiceMapper {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerServiceMapper.class);
    private String httpAlias;
    private String swaggerAlias;
    private ObjectMapper objectMapper;

    /**
     * Initializes a service parser for swagger.
     *
     * @param httpAlias    The alias for ballerina/http module.
     * @param swaggerAlias The alias for ballerina.swagger module.
     */
    public SwaggerServiceMapper(String httpAlias, String swaggerAlias) {
        // Default object mapper is JSON mapper available in swagger utils.
        this.httpAlias = httpAlias;
        this.swaggerAlias = swaggerAlias;
        this.objectMapper = Json.mapper();
    }

    /**
     * Retrieves the String definition of a Swagger object.
     *
     * @param swagger Swagger definition
     * @return String representation of current service object.
     */
    public String generateSwaggerString(Swagger swagger) {
        try {
            return objectMapper.writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            logger.error("Error while generating swagger string from definition" + e);
            return "Error";
        }
    }

    /**
     * This method will convert ballerina @Service to swaggers @Swagger object.
     *
     * @param service ballerina @Service object to be map to swagger definition
     * @return Swagger object which represent current service.
     */
    public Swagger convertServiceToSwagger(ServiceNode service) {
        Swagger swagger = new Swagger();
        return convertServiceToSwagger(service, swagger);
    }

    /**
     * This method will convert ballerina @Service to swaggers @Swagger object.
     *
     * @param service ballerina @Service object to be map to swagger definition
     * @param swagger Swagger model to populate
     * @return Swagger object which represent current service.
     */
    public Swagger convertServiceToSwagger(ServiceNode service, Swagger swagger) {
        // Setting default values.
        swagger.setBasePath('/' + service.getName().getValue());
        this.parseServiceInfoAnnotationAttachment(service, swagger);
        this.parseConfigAnnotationAttachment(service, swagger);

        SwaggerResourceMapper resourceMapper = new SwaggerResourceMapper(swagger, this.httpAlias, this.swaggerAlias);
        swagger.setPaths(resourceMapper.convertResourceToPath(service.getResources()));
        return swagger;
    }

    /**
     * Parses the 'ServiceInfo' annotation and build the swagger definition for it.
     *
     * @param service The ballerina service which has the 'ServiceInfo' annotation attachment.
     * @param swagger The swagger definition to be built up.
     */
    private void parseServiceInfoAnnotationAttachment(ServiceNode service, Swagger swagger) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ServiceInfo", swaggerAlias,
                service.getAnnotationAttachments());
        
        Info info = new Info().version("1.0.0").title(service.getName().getValue());
        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();

            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);
            if (attributes.containsKey(ConverterConstants.ATTR_SERVICE_VERSION)) {
                info.version(
                        ConverterUtils.getStringLiteralValue(attributes.get(ConverterConstants.ATTR_SERVICE_VERSION)));
            }
            if (attributes.containsKey(ConverterConstants.ATTR_TITLE)) {
                info.title(ConverterUtils.getStringLiteralValue(attributes.get(ConverterConstants.ATTR_TITLE)));
            }
            if (attributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                info.description(
                        ConverterUtils.getStringLiteralValue(attributes.get(ConverterConstants.ATTR_DESCRIPTION)));
            }
            if (attributes.containsKey(ConverterConstants.ATTR_TERMS)) {
                info.termsOfService(
                        ConverterUtils.getStringLiteralValue(attributes.get(ConverterConstants.ATTR_TERMS)));
            }
            this.createContactModel(attributes.get("contact"), info);
            this.createLicenseModel(attributes.get("license"), info);

            this.createExternalDocModel(attributes.get("externalDoc"), swagger);
            this.createTagModel(attributes.get("tags"), swagger);
            this.createOrganizationModel(attributes.get("organization"), info);
        }
        swagger.setInfo(info);
    }

    /**
     * Creates vendor extension for organization.
     *
     * @param annotationExpression The annotation attribute value for organization vendor extension.
     * @param info                 The info definition.
     */
    private void createOrganizationModel(BLangExpression annotationExpression, Info info) {
        if (null != annotationExpression) {
            BLangRecordLiteral orgAnnotation = (BLangRecordLiteral) annotationExpression;
            Map<String, BLangExpression> organizationAttributes =
                    ConverterUtils.listToMap(orgAnnotation.getKeyValuePairs());
            Organization organization = new Organization();

            if (organizationAttributes.containsKey(ConverterConstants.ATTR_NAME)) {
                organization.setName(
                        ConverterUtils.getStringLiteralValue(organizationAttributes.get(ConverterConstants.ATTR_NAME)));
            }
            if (organizationAttributes.containsKey(ConverterConstants.ATTR_URL)) {
                organization.setUrl(ConverterUtils
                        .getStringLiteralValue(organizationAttributes.get(ConverterConstants.ATTR_URL)));
            }
            info.setVendorExtension("x-organization", organization);
        }
    }

    /**
     * Creates tag swagger definition.
     *
     * @param annotationExpression The ballerina annotation attribute value for tag.
     * @param swagger              The swagger definition which the tags needs to be build on.
     */
    private void createTagModel(BLangExpression annotationExpression, Swagger swagger) {
        if (null != annotationExpression) {
            List<Tag> tags = new LinkedList<>();
            BLangArrayLiteral tagArray = (BLangArrayLiteral) annotationExpression;

            for (ExpressionNode expr : tagArray.getExpressions()) {
                List<BLangRecordKeyValue> tagList = ((BLangRecordLiteral) expr).getKeyValuePairs();
                Map<String, BLangExpression> tagAttributes = ConverterUtils.listToMap(tagList);
                Tag tag = new Tag();

                if (tagAttributes.containsKey(ConverterConstants.ATTR_NAME)) {
                    tag.setName(ConverterUtils.getStringLiteralValue(tagAttributes.get(ConverterConstants.ATTR_NAME)));
                }
                if (tagAttributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                    tag.setDescription(ConverterUtils
                            .getStringLiteralValue(tagAttributes.get(ConverterConstants.ATTR_DESCRIPTION)));
                }

                tags.add(tag);
            }

            swagger.setTags(Lists.reverse(tags));
        }
    }

    /**
     * Creates external docs swagger definition.
     *
     * @param annotationExpression The ballerina annotation attribute value for external docs.
     * @param swagger              The swagger definition which the external docs needs to be build on.
     */
    private void createExternalDocModel(BLangExpression annotationExpression, Swagger swagger) {
        if (null != annotationExpression) {
            BLangRecordLiteral docAnnotation = (BLangRecordLiteral) annotationExpression;
            Map<String, BLangExpression> externalDocAttributes =
                    ConverterUtils.listToMap(docAnnotation.getKeyValuePairs());
            ExternalDocs externalDocs = new ExternalDocs();

            if (externalDocAttributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                externalDocs.setDescription(ConverterUtils
                        .getStringLiteralValue(externalDocAttributes.get(ConverterConstants.ATTR_DESCRIPTION)));
            }
            if (externalDocAttributes.containsKey(ConverterConstants.ATTR_URL)) {
                externalDocs.setUrl(ConverterUtils
                        .getStringLiteralValue(externalDocAttributes.get(ConverterConstants.ATTR_URL)));
            }
    
            swagger.setExternalDocs(externalDocs);
        }
    }

    /**
     * Creates the contact swagger definition.
     *
     * @param annotationExpression The ballerina annotation attribute value for contact.
     * @param info                 The info definition which the contact needs to be build on.
     */
    private void createContactModel(BLangExpression annotationExpression, Info info) {
        if (null != annotationExpression) {
            BLangRecordLiteral contactAnnotation = (BLangRecordLiteral) annotationExpression;
            Map<String, BLangExpression> contactAttributes =
                    ConverterUtils.listToMap(contactAnnotation.getKeyValuePairs());
            Contact contact = new Contact();

            if (contactAttributes.containsKey(ConverterConstants.ATTR_NAME)) {
                contact.setName(
                        ConverterUtils.getStringLiteralValue(contactAttributes.get(ConverterConstants.ATTR_NAME)));
            }
            if (contactAttributes.containsKey("email")) {
                contact.setEmail(ConverterUtils.getStringLiteralValue(contactAttributes.get("email")));
            }
            if (contactAttributes.containsKey(ConverterConstants.ATTR_URL)) {
                contact.setUrl(
                        ConverterUtils.getStringLiteralValue(contactAttributes.get(ConverterConstants.ATTR_URL)));
            }
    
            info.setContact(contact);
        }
    }

    /**
     * Creates the license swagger definition.
     *
     * @param annotationExpression The ballerina annotation attribute value for license.
     * @param info                 The info definition which the license needs to be build on.
     */
    private void createLicenseModel(BLangExpression annotationExpression, Info info) {
        if (null != annotationExpression) {
            BLangRecordLiteral licenseAnnotation = (BLangRecordLiteral) annotationExpression;
            Map<String, BLangExpression> licenseAttributes =
                    ConverterUtils.listToMap(licenseAnnotation.getKeyValuePairs());
            License license = new License();

            if (licenseAttributes.containsKey(ConverterConstants.ATTR_NAME)) {
                license.setName(
                        ConverterUtils.getStringLiteralValue(licenseAttributes.get(ConverterConstants.ATTR_NAME)));
            }
            if (licenseAttributes.containsKey(ConverterConstants.ATTR_URL)) {
                license.setUrl(
                        ConverterUtils.getStringLiteralValue(licenseAttributes.get(ConverterConstants.ATTR_URL)));
            }
            
            info.setLicense(license);
        }
    }

    /**
     * Parses the ballerina/http@config annotation and builds swagger definition. Also create the consumes and
     * produces annotations.
     *
     * @param service The ballerina service which has the annotation.
     * @param swagger The swagger to build up.
     */
    private void parseConfigAnnotationAttachment(ServiceNode service, Swagger swagger) {
        AnnotationAttachmentNode annotation = ConverterUtils
                .getAnnotationFromList(HttpConstants.ANN_NAME_HTTP_SERVICE_CONFIG, httpAlias,
                        service.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();

            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);
            if (attributes.containsKey(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH)) {
                swagger.setBasePath(
                        ConverterUtils.getStringLiteralValue(attributes.get(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH)));
            }
        }
    }

}

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

package org.ballerinalang.ballerina.openapi.convertor.service;

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
import org.ballerinalang.ballerina.openapi.convertor.ConverterUtils;
import org.ballerinalang.ballerina.openapi.convertor.service.model.Organization;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.net.http.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;

/**
 * OpenApiServiceMapper provides functionality for reading and writing OpenApi, either to and from ballerina service, or
 * to, as well as related functionality for performing conversions between openapi and ballerina.
 */
public class OpenApiServiceMapper {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiServiceMapper.class);
    private String httpAlias;
    private String openApiAlias;
    private ObjectMapper objectMapper;

    /**
     * Initializes a service parser for OpenApi.
     *
     * @param httpAlias    The alias for ballerina/http module.
     * @param openApiAlias The alias for ballerina.openapi module.
     */
    public OpenApiServiceMapper(String httpAlias, String openApiAlias) {
        // Default object mapper is JSON mapper available in openApi utils.
        this.httpAlias = httpAlias;
        this.openApiAlias = openApiAlias;
        this.objectMapper = Json.mapper();
    }

    /**
     * Retrieves the String definition of a OpenApi object.
     *
     * @param openapi OpenApi definition
     * @return String representation of current service object.
     */
    public String generateOpenApiString(Swagger openapi) {
        try {
            return objectMapper.writeValueAsString(openapi);
        } catch (JsonProcessingException e) {
            logger.error("Error while generating openApi string from definition" + e);
            return "Error";
        }
    }

    /**
     * This method will convert ballerina @Service to OpenApi @OpenApi object.
     *
     * @param service ballerina @Service object to be map to openapi definition
     * @return OpenApi object which represent current service.
     */
    public Swagger convertServiceToOpenApi(BLangService service) {
        Swagger openapi = new Swagger();
        return convertServiceToOpenApi(service, openapi);
    }

    /**
     * This method will convert ballerina @Service to openApi @OpenApi object.
     *
     * @param service ballerina @Service object to be map to openApi definition
     * @param openapi OpenApi model to populate
     * @return OpenApi object which represent current service.
     */
    public Swagger convertServiceToOpenApi(BLangService service, Swagger openapi) {
        // Setting default values.
        openapi.setBasePath('/' + service.getName().getValue());
        this.parseServiceInfoAnnotationAttachment(service, openapi);
        this.parseConfigAnnotationAttachment(service, openapi);

        OpenApiResourceMapper resourceMapper = new OpenApiResourceMapper(openapi, this.httpAlias, this.openApiAlias);
        openapi.setPaths(resourceMapper.convertResourceToPath(service.getResources()));
        return openapi;
    }

    /**
     * Parses the 'ServiceInfo' annotation and build the openApi definition for it.
     *
     * @param service The ballerina service which has the 'ServiceInfo' annotation attachment.
     * @param openapi The openapi definition to be built up.
     */
    private void parseServiceInfoAnnotationAttachment(ServiceNode service, Swagger openapi) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ServiceInfo", openApiAlias,
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

            this.createExternalDocModel(attributes.get("externalDoc"), openapi);
            this.createTagModel(attributes.get("tags"), openapi);
            this.createOrganizationModel(attributes.get("organization"), info);
        }
        openapi.setInfo(info);
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
     * Creates tag openApi definition.
     *
     * @param annotationExpression The ballerina annotation attribute value for tag.
     * @param openApi              The openApi definition which the tags needs to be build on.
     */
    private void createTagModel(BLangExpression annotationExpression, Swagger openApi) {
        if (null != annotationExpression) {
            List<Tag> tags = new LinkedList<>();
            BLangListConstructorExpr tagArray = (BLangListConstructorExpr) annotationExpression;

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

            openApi.setTags(Lists.reverse(tags));
        }
    }

    /**
     * Creates external docs openApi definition.
     *
     * @param annotationExpression The ballerina annotation attribute value for external docs.
     * @param openApi              The openApi definition which the external docs needs to be build on.
     */
    private void createExternalDocModel(BLangExpression annotationExpression, Swagger openApi) {
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
    
            openApi.setExternalDocs(externalDocs);
        }
    }

    /**
     * Creates the contact openApi definition.
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
     * Creates the license openApi definition.
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
     * Parses the ballerina/http@config annotation and builds openApi definition. Also create the consumes and
     * produces annotations.
     *
     * @param service The ballerina service which has the annotation.
     * @param openApi The openApi to build up.
     */
    private void parseConfigAnnotationAttachment(ServiceNode service, Swagger openApi) {
        AnnotationAttachmentNode annotation = ConverterUtils
                .getAnnotationFromList(HttpConstants.ANN_NAME_HTTP_SERVICE_CONFIG, httpAlias,
                        service.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();

            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);
            if (attributes.containsKey(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH)) {
                openApi.setBasePath(
                        ConverterUtils.getStringLiteralValue(attributes.get(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH)));
            }
        }
    }

}

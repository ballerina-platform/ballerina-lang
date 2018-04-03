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

package org.ballerinalang.swagger.code.generator;

import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.swagger.code.generator.model.Developer;
import org.ballerinalang.swagger.code.generator.model.Organization;
import org.ballerinalang.swagger.code.generator.util.SwaggerConstants;
import org.ballerinalang.swagger.code.generator.util.SwaggerUtils;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeKeyValuePair;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The following class uses a ServiceInfo object and generates the swagger definition model by parsing the resources and
 * annotation attachments.
 */
class SwaggerServiceMapper {

    /**
     * This method will convert ServiceInfo to a swagger model
     *
     * @param service ballerina @Service object to be map to swagger definition
     * @return Swagger object which represent current service.
     */
    Swagger convertServiceToSwagger(ServiceInfo service) {
        Swagger swagger = new Swagger();
        //// Setting default values.
        // Setting service name
        swagger.setBasePath('/' + service.getName());
        
        this.parseServiceInfoAnnotationAttachment(service, swagger);
        // TODO: parseSwaggerAnnotationAttachment(service, swagger);
        this.parseServiceConfigAnnotationAttachment(service, swagger);
        this.parseConfigAnnotationAttachment(service, swagger);
        
        SwaggerResourceMapper resourceMapper = new SwaggerResourceMapper(swagger);
        resourceMapper.mapResourcesToPathPaths(service.getResourceInfoEntries());
        return swagger;
    }
    
    /**
     * Parses the 'ServiceConfig' annotation attachment.
     * @param service The ballerina service which has that annotation attachment.
     * @param swagger The swagger definition to build up.
     */
    private void parseServiceConfigAnnotationAttachment(ServiceInfo service, Swagger swagger) {
        AnnAttachmentInfo swaggerConfigAnnotation = service.getAnnotationAttachmentInfo(
                SwaggerConstants.SWAGGER_PACKAGE_PATH, "ServiceConfig");
        if (null != swaggerConfigAnnotation) {
            Map<String, AnnAttributeValue> swaggerConfigAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap(
                    swaggerConfigAnnotation);
            if (null != swaggerConfigAnnAttributeValueMap.get("host")) {
                swagger.setHost(swaggerConfigAnnAttributeValueMap.get("host").getStringValue());
            }
            if (null != swaggerConfigAnnAttributeValueMap.get("schemes")) {
                if (swaggerConfigAnnAttributeValueMap.get("schemes").getAttributeValueArray().length > 0) {
                    List<Scheme> schemes = new LinkedList<>();
                    for (AnnAttributeValue schemeValue : swaggerConfigAnnAttributeValueMap.get("schemes")
                            .getAttributeValueArray()) {
                        if (null != Scheme.forValue(schemeValue.getStringValue())) {
                            schemes.add(Scheme.forValue(schemeValue.getStringValue()));
                        }
                    }
                    if (schemes.size() > 0) {
                        swagger.setSchemes(schemes);
                    }
                }
            }
            this.createSecurityDefinitionsModel(swaggerConfigAnnAttributeValueMap.get("authorizations"), swagger);
        }
    }
    
    /**
     * Creates the security definition models for swagger definition.
     * @param annotationAttributeValue The annotation attribute value for security definitions.
     * @param swagger The swagger definition.
     */
    private void createSecurityDefinitionsModel(AnnAttributeValue annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue) {
            Map<String, SecuritySchemeDefinition> securitySchemeDefinitionMap = new HashMap<>();
            for (AnnAttributeValue authorizationValues : annotationAttributeValue.getAttributeValueArray()) {
                AnnAttachmentInfo authAnnotationAttachment = authorizationValues.getAnnotationAttachmentValue();
                Map<String, AnnAttributeValue> authAnnAttributeValueMap =
                        SwaggerUtils.convertToAttributeMap(authAnnotationAttachment);
                if (null != authAnnAttributeValueMap.get("name") && null != authAnnAttributeValueMap.get("authType")) {
                    String name = authAnnAttributeValueMap.get("name").getStringValue();
                    String type = authAnnAttributeValueMap.get("authType").getStringValue();
                    String description = "";
                    if (null != authAnnAttributeValueMap.get("description")) {
                        description = authAnnAttributeValueMap.get("description").getStringValue();
                    }
                    if ("basic".equals(type)) {
                        BasicAuthDefinition basicAuthDefinition = new BasicAuthDefinition();
                        basicAuthDefinition.setDescription(description);
                        securitySchemeDefinitionMap.put(name, basicAuthDefinition);
                    } else if ("apiKey".equals(type)) {
                        ApiKeyAuthDefinition apiKeyAuthDefinition = new ApiKeyAuthDefinition();
                        apiKeyAuthDefinition.setName(authAnnAttributeValueMap.get("apiName").getStringValue());
                        apiKeyAuthDefinition.setIn(In.forValue(authAnnAttributeValueMap.get("in").getStringValue()));
                        apiKeyAuthDefinition.setDescription(description);
                        securitySchemeDefinitionMap.put(name, apiKeyAuthDefinition);
                    } else if ("oauth2".equals(type)) {
                        OAuth2Definition oAuth2Definition = new OAuth2Definition();
                        oAuth2Definition.setFlow(authAnnAttributeValueMap.get("flow").getStringValue());
                        oAuth2Definition.setAuthorizationUrl(authAnnAttributeValueMap.get("authorizationUrl")
                                .getStringValue());
                        oAuth2Definition.setTokenUrl(authAnnAttributeValueMap.get("tokenUrl").getStringValue());
    
                        this.createSecurityDefinitionScopesModel(authAnnAttributeValueMap.get("authorizationScopes"),
                                oAuth2Definition);
                        
                        oAuth2Definition.setDescription(description);
                        securitySchemeDefinitionMap.put(name, oAuth2Definition);
                    }
                }
            }
            swagger.setSecurityDefinitions(securitySchemeDefinitionMap);
        }
    }
    
    /**
     * Creates the security definition scopes for oAuth2.
     * @param authorizationScopes The annotation attribute value of authorization scopes.
     * @param oAuth2Definition The oAuth2 definition.
     */
    private void createSecurityDefinitionScopesModel(AnnAttributeValue authorizationScopes,
                                                     OAuth2Definition oAuth2Definition) {
        if (null != authorizationScopes) {
            Map<String, String> scopes = new HashMap<>();
            for (AnnAttributeValue authScopeValue : authorizationScopes.getAttributeValueArray()) {
                AnnAttachmentInfo authScopeAnnotationAttachment = authScopeValue.getAnnotationAttachmentValue();
                Map<String, AnnAttributeValue> authScopeAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                        (authScopeAnnotationAttachment);
                String name = authScopeAnnAttributeValueMap.get("name").getStringValue();
                String description = authScopeAnnAttributeValueMap.get("description").getStringValue();
                
                scopes.put(name, description);
            }
            oAuth2Definition.setScopes(scopes);
        }
    }
    
    /**
     * Parses the 'ServiceInfo' annotation and build the swagger definition for it.
     * @param service The ballerina service which has the 'ServiceInfo' annotation attachment.
     * @param swagger The swagger definition to be built up.
     */
    private void parseServiceInfoAnnotationAttachment(ServiceInfo service, Swagger swagger) {
        AnnAttachmentInfo swaggerInfoAnnotation = service.getAnnotationAttachmentInfo(
                SwaggerConstants.SWAGGER_PACKAGE_PATH, "ServiceInfo");
        
        Info info = new Info()
                .version("1.0.0")
                .title(service.getName());
        if (null != swaggerInfoAnnotation) {
            for (AnnAttributeKeyValuePair annAttributeKeyValuePair : swaggerInfoAnnotation.getAttributeKeyValuePairs
                    ()) {
                if ("version".equals(annAttributeKeyValuePair.getAttributeName())) {
                    info.version(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("title".equals(annAttributeKeyValuePair.getAttributeName())) {
                    info.title(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("description".equals(annAttributeKeyValuePair.getAttributeName())) {
                    info.description(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("termsOfService".equals(annAttributeKeyValuePair.getAttributeName())) {
                    info.termsOfService(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("contact".equals(annAttributeKeyValuePair.getAttributeName())) {
                    this.createContactModel(annAttributeKeyValuePair.getAttributeValue(), info);
                } else if ("license".equals(annAttributeKeyValuePair.getAttributeName())) {
                    this.createLicenseModel(annAttributeKeyValuePair.getAttributeValue(), info);
                } else if ("externalDoc".equals(annAttributeKeyValuePair.getAttributeName())) {
                    this.createExternalDocModel(annAttributeKeyValuePair.getAttributeValue(), swagger);
                } else if ("tags".equals(annAttributeKeyValuePair.getAttributeName())) {
                    this.createTagModel(annAttributeKeyValuePair.getAttributeValue(), swagger);
                } else if ("organization".equals(annAttributeKeyValuePair.getAttributeName())) {
                    this.createOrganizationModel(annAttributeKeyValuePair.getAttributeValue(), info);
                } else if ("developers".equals(annAttributeKeyValuePair.getAttributeName())) {
                    this.createDevelopersModel(annAttributeKeyValuePair.getAttributeValue(), info);
                }
            }
        }
        swagger.setInfo(info);
    }
    
    /**
     * Creates vendor extension for developers.
     * @param annotationAttributeValue The annotation attribute value for developer vendor extension.
     * @param info The info definition.
     */
    private void createDevelopersModel(AnnAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue && annotationAttributeValue.getAttributeValueArray().length > 0) {
            Developer[] developers = new Developer[annotationAttributeValue.getAttributeValueArray().length];
            for (int i = 0; i < annotationAttributeValue.getAttributeValueArray().length; i++) {
                AnnAttachmentInfo developerAnnotation = annotationAttributeValue.getAttributeValueArray()[i]
                        .getAnnotationAttachmentValue();
                Developer developer = new Developer();
                for (AnnAttributeKeyValuePair annAttributeKeyValuePair : developerAnnotation
                        .getAttributeKeyValuePairs()) {
                    if ("name".equals(annAttributeKeyValuePair.getAttributeName())) {
                        developer.setName(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                    } else if ("email".equals(annAttributeKeyValuePair.getAttributeName())) {
                        developer.setEmail(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                    }
                }
                developers[i] = developer;
            }
            info.setVendorExtension("x-developers", developers);
        }
    }
    
    /**
     * Creates vendor extension for organization.
     * @param annotationAttributeValue The annotation attribute value for organization vendor extension.
     * @param info The info definition.
     */
    private void createOrganizationModel(AnnAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnAttachmentInfo organizationAnnotationAttachment = annotationAttributeValue
                    .getAnnotationAttachmentValue();
            Organization organization = new Organization();
            for (AnnAttributeKeyValuePair annAttributeKeyValuePair : organizationAnnotationAttachment
                    .getAttributeKeyValuePairs()) {
                if ("name".equals(annAttributeKeyValuePair.getAttributeName())) {
                    organization.setName(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("url".equals(annAttributeKeyValuePair.getAttributeName())) {
                    organization.setUrl(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                }
            }
            info.setVendorExtension("x-organization", organization);
        }
    }
    
    /**
     * Creates tag swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for tag.
     * @param swagger The swagger definition which the tags needs to be build on.
     */
    private void createTagModel(AnnAttributeValue annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue && annotationAttributeValue.getAttributeValueArray().length > 0) {
            List<Tag> tags = new LinkedList<>();
            for (AnnAttributeValue tagAttributeValue : annotationAttributeValue.getAttributeValueArray()) {
                AnnAttachmentInfo tagAnnotationAttachment = tagAttributeValue.getAnnotationAttachmentValue();
                Tag tag = new Tag();
                for (AnnAttributeKeyValuePair annAttributeKeyValuePair : tagAnnotationAttachment
                        .getAttributeKeyValuePairs()) {
                    if ("name".equals(annAttributeKeyValuePair.getAttributeName())) {
                        tag.setName(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                    } else if ("description".equals(annAttributeKeyValuePair.getAttributeName())) {
                        tag.setDescription(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                    }
                }
    
                tags.add(tag);
            }
            swagger.setTags(tags);
        }
    }
    
    /**
     * Creates external docs swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for external docs.
     * @param swagger The swagger definition which the external docs needs to be build on.
     */
    private void createExternalDocModel(AnnAttributeValue annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue) {
            AnnAttachmentInfo externalDocAnnotationAttachment = annotationAttributeValue.getAnnotationAttachmentValue();
            ExternalDocs externalDoc = new ExternalDocs();
            for (AnnAttributeKeyValuePair annAttributeKeyValuePair : externalDocAnnotationAttachment
                    .getAttributeKeyValuePairs()) {
                if ("description".equals(annAttributeKeyValuePair.getAttributeName())) {
                    externalDoc.setDescription(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("url".equals(annAttributeKeyValuePair.getAttributeName())) {
                    externalDoc.setUrl(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                }
            }
    
            swagger.setExternalDocs(externalDoc);
        }
    }
    
    /**
     * Creates the contact swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for contact.
     * @param info The info definition which the contact needs to be build on.
     */
    private void createContactModel(AnnAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnAttachmentInfo contactAnnotationAttachment = annotationAttributeValue.getAnnotationAttachmentValue();
            Contact contact = new Contact();
            for (AnnAttributeKeyValuePair annAttributeKeyValuePair : contactAnnotationAttachment
                    .getAttributeKeyValuePairs()) {
                if ("name".equals(annAttributeKeyValuePair.getAttributeName())) {
                    contact.setName(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("email".equals(annAttributeKeyValuePair.getAttributeName())) {
                    contact.setEmail(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("url".equals(annAttributeKeyValuePair.getAttributeName())) {
                    contact.setUrl(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                }
            }
    
            info.setContact(contact);
        }
    }
    
    /**
     * Creates the license swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for license.
     * @param info The info definition which the license needs to be build on.
     */
    private void createLicenseModel(AnnAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnAttachmentInfo licenseAnnotationAttachment = annotationAttributeValue.getAnnotationAttachmentValue();
            License license = new License();
            for (AnnAttributeKeyValuePair annAttributeKeyValuePair : licenseAnnotationAttachment
                    .getAttributeKeyValuePairs()) {
                if ("name".equals(annAttributeKeyValuePair.getAttributeName())) {
                    license.setName(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                } else if ("url".equals(annAttributeKeyValuePair.getAttributeName())) {
                    license.setUrl(annAttributeKeyValuePair.getAttributeValue().getStringValue());
                }
            }
            
            info.setLicense(license);
        }
    }
    
    /**
     * Parses the ballerina/http@config annotation and builds swagger definition. Also create the consumes and
     * produces annotations.
     * @param service The ballerina service which has the annotation.
     * @param swagger The swagger to build up.
     */
    private void parseConfigAnnotationAttachment(ServiceInfo service, Swagger swagger) {
        AnnAttachmentInfo httpConfigAnnotationAttachment = service.getAnnotationAttachmentInfo(
                HttpConstants.HTTP_PACKAGE_PATH, HttpConstants.ANN_NAME_CONFIG);
        if (null != httpConfigAnnotationAttachment) {
            Map<String, AnnAttributeValue> httpConfigAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                    (httpConfigAnnotationAttachment);
            if (null != httpConfigAnnAttributeValueMap.get(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH)) {
                swagger.setBasePath(httpConfigAnnAttributeValueMap.get(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH)
                        .getStringValue());
            }
            if (null != httpConfigAnnAttributeValueMap.get(HttpConstants.ANN_CONFIG_ATTR_HOST) &&
                null != httpConfigAnnAttributeValueMap.get(HttpConstants.ANN_CONFIG_ATTR_PORT)) {
                swagger.setHost(httpConfigAnnAttributeValueMap.get(HttpConstants.ANN_CONFIG_ATTR_HOST)
                                        .getStringValue() + ":" +
                                httpConfigAnnAttributeValueMap.get(HttpConstants.ANN_CONFIG_ATTR_PORT).getIntValue());
            }
        }
    }
}

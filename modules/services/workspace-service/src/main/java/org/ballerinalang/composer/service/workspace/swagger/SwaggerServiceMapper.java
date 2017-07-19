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

package org.ballerinalang.composer.service.workspace.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.swagger.util.Json;
import org.ballerinalang.composer.service.workspace.swagger.model.Developer;
import org.ballerinalang.composer.service.workspace.swagger.model.Organization;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SwaggerServiceMapper provides functionality for reading and writing Swagger, either to and from ballerina service, or
 * to, as well as related functionality for performing conversions between swagger and ballerina.
 */
public class SwaggerServiceMapper {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerServiceMapper.class);
    private static final String SWAGGER_PACKAGE_PATH = "ballerina.net.http.swagger";
    private static final String SWAGGER_PACKAGE = "swagger";
    private static final String HTTP_PACKAGE_PATH = "ballerina.net.http";
    private static final String HTTP_PACKAGE = "http";
    private ObjectMapper objectMapper;

    public SwaggerServiceMapper() {
        // Default object mapper is JSON mapper available in swagger utils.
        this.objectMapper = Json.mapper();
    }
    /**
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
    public Swagger convertServiceToSwagger(Service service) {
        Swagger swagger = new Swagger();
        // Setting default values.
        swagger.setBasePath('/' + service.getName());
        
        this.parseServiceInfoAnnotationAttachment(service, swagger);
        // TODO: parseSwaggerAnnotationAttachment(service, swagger);
        this.parseServiceConfigAnnotationAttachment(service, swagger);
        this.parseConfigAnnotationAttachment(service, swagger);
        
        SwaggerResourceMapper resourceMapper = new SwaggerResourceMapper();
        swagger.setPaths(resourceMapper.convertResourceToPath(service.getResources()));
        return swagger;
    }
    
    /**
     * Parses the 'ServiceConfig' annotation attachment.
     * @param service The ballerina service which has that annotation attachment.
     * @param swagger The swagger definition to build up.
     */
    private void parseServiceConfigAnnotationAttachment(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> swaggerConfigAnnotation = Arrays.stream(service.getAnnotations())
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ServiceConfig".equals(a.getName()))
                .findFirst();
        if (swaggerConfigAnnotation.isPresent()) {
            if (null != swaggerConfigAnnotation.get().getAttribute("host")) {
                swagger.setHost(swaggerConfigAnnotation.get().getAttribute("host").getLiteralValue().stringValue());
            }
            if (null != swaggerConfigAnnotation.get().getAttribute("schemes")) {
                if (swaggerConfigAnnotation.get().getAttribute("schemes").getValueArray().length > 0) {
                    List<Scheme> schemes = new LinkedList<>();
                    for (AnnotationAttributeValue schemeValue : swaggerConfigAnnotation.get()
                            .getAttribute("schemes").getValueArray()) {
                        schemes.add(Scheme.forValue(schemeValue.getLiteralValue().stringValue()));
                    }
                    swagger.setSchemes(schemes);
                }
            }
            this.createSecurityDefinitionsModel(swaggerConfigAnnotation.get().getAttributeNameValuePairs()
                    .get("authorizations"), swagger);
        }
    }
    
    /**
     * Creates the security definition models for swagger definition.
     * @param annotationAttributeValue The annotation attribute value for security definitions.
     * @param swagger The swagger definition.
     */
    private void createSecurityDefinitionsModel(AnnotationAttributeValue annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue) {
            Map<String, SecuritySchemeDefinition> securitySchemeDefinitionMap = new HashMap<>();
            for (AnnotationAttributeValue authorizationValues : annotationAttributeValue.getValueArray()) {
                AnnotationAttachment authAnnotationAttachment = authorizationValues.getAnnotationValue();
                if (null != authAnnotationAttachment.getAttribute("name") &&
                    null != authAnnotationAttachment.getAttribute("type")) {
                    String name = authAnnotationAttachment.getAttribute("name").getLiteralValue().stringValue();
                    String type = authAnnotationAttachment.getAttribute("type").getLiteralValue().stringValue();
                    String description = "";
                    if (null != authAnnotationAttachment.getAttributeNameValuePairs().get("description")) {
                        description = authAnnotationAttachment.getAttributeNameValuePairs().get("description")
                                .getLiteralValue().stringValue();
                    }
                    if ("basic".equals(type)) {
                        BasicAuthDefinition basicAuthDefinition = new BasicAuthDefinition();
                        basicAuthDefinition.setDescription(description);
                        securitySchemeDefinitionMap.put(name, basicAuthDefinition);
                    } else if ("apiKey".equals(type)) {
                        ApiKeyAuthDefinition apiKeyAuthDefinition = new ApiKeyAuthDefinition();
                        apiKeyAuthDefinition.setName(authAnnotationAttachment
                                .getAttribute("apiName").getLiteralValue().stringValue());
                        apiKeyAuthDefinition.setIn(In.forValue(authAnnotationAttachment
                                .getAttribute("in").getLiteralValue().stringValue()));
                        apiKeyAuthDefinition.setDescription(description);
                        securitySchemeDefinitionMap.put(name, apiKeyAuthDefinition);
                    } else if ("oauth2".equals(type)) {
                        OAuth2Definition oAuth2Definition = new OAuth2Definition();
                        oAuth2Definition.setFlow(authAnnotationAttachment
                                .getAttribute("flow").getLiteralValue().stringValue());
                        oAuth2Definition.setAuthorizationUrl(authAnnotationAttachment
                                .getAttribute("authorizationUrl").getLiteralValue().stringValue());
                        oAuth2Definition.setTokenUrl(authAnnotationAttachment.getAttribute("tokenUrl")
                                .getLiteralValue().stringValue());
    
                        this.createSecurityDefinitionScopesModel(authAnnotationAttachment
                                .getAttribute("authorizationScopes"), oAuth2Definition);
                        
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
    private void createSecurityDefinitionScopesModel(AnnotationAttributeValue authorizationScopes,
                                                     OAuth2Definition oAuth2Definition) {
        Map<String, String> scopes = new HashMap<>();
        for (AnnotationAttributeValue authScopeValue : authorizationScopes.getValueArray()) {
            AnnotationAttachment authScopeAnnotationAttachment = authScopeValue.getAnnotationValue();
            String name = authScopeAnnotationAttachment.getAttribute("name").getLiteralValue().stringValue();
            String description = authScopeAnnotationAttachment
                    .getAttribute("description").getLiteralValue().stringValue();
            scopes.put(name, description);
        }
        oAuth2Definition.setScopes(scopes);
    }
    
    /**
     * Parses the 'ServiceInfo' annotation and build the swagger definition for it.
     * @param service The ballerina service which has the 'ServiceInfo' annotation attachment.
     * @param swagger The swagger definition to be built up.
     */
    private void parseServiceInfoAnnotationAttachment(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> swaggerInfoAnnotation = Arrays.stream(service.getAnnotations())
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ServiceInfo".equals(a.getName()))
                .findFirst();
        
        Info info = new Info()
                .version("1.0.0")
                .title(service.getName());
        if (swaggerInfoAnnotation.isPresent()) {
            if (null != swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("version")) {
                info.version(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("version")
                        .getLiteralValue().stringValue());
            }
            if (null != swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("title")) {
                info.title(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("title")
                        .getLiteralValue().stringValue());
            }
            if (null != swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("description")) {
                info.description(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("description")
                        .getLiteralValue().stringValue());
            }
            if (null != swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("termsOfService")) {
                info.termsOfService(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("termsOfService")
                        .getLiteralValue().stringValue());
            }
            this.createContactModel(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("contact"), info);
            this.createLicenseModel(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("license"), info);
    
            this.createExternalDocsModel(swaggerInfoAnnotation.get().getAttributeNameValuePairs()
                    .get("externalDocs"), swagger);
            this.createTagModel(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("tags"), swagger);
            this.createOrganizationModel(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("organization"),
                    info);
            this.createDevelopersModel(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("developers"),
                    info);
        }
        swagger.setInfo(info);
    }
    
    /**
     * Creates vendor extension for developers.
     * @param annotationAttributeValue The annotation attribute value for developer vendor extension.
     * @param info The info definition.
     */
    private void createDevelopersModel(AnnotationAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            if (annotationAttributeValue.getValueArray().length > 0) {
                Developer[] developers = new Developer[annotationAttributeValue.getValueArray().length];
                for (int i = 0; i < annotationAttributeValue.getValueArray().length; i++) {
                    AnnotationAttachment developerAnnotation = annotationAttributeValue.getValueArray()[i]
                            .getAnnotationValue();
                    Developer developer = new Developer();
                    if (null != developerAnnotation.getAttributeNameValuePairs().get("name")) {
                        developer.setName(developerAnnotation.getAttributeNameValuePairs().get("name")
                                .getLiteralValue().stringValue());
                    }
                    if (null != developerAnnotation.getAttributeNameValuePairs().get("email")) {
                        developer.setEmail(developerAnnotation.getAttributeNameValuePairs().get("email")
                                .getLiteralValue().stringValue());
                    }
                    developers[i] = developer;
                }
                info.setVendorExtension("x-developers", developers);
            }
        }
    }
    
    /**
     * Creates vendor extension for organization.
     * @param annotationAttributeValue The annotation attribute value for organization vendor extension.
     * @param info The info definition.
     */
    private void createOrganizationModel(AnnotationAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnotationAttachment organizationAnnotationAttachment = annotationAttributeValue.getAnnotationValue();
            Organization organization = new Organization();
            if (null != organizationAnnotationAttachment.getAttributeNameValuePairs().get("name")) {
                organization.setName(organizationAnnotationAttachment.getAttributeNameValuePairs().get("name")
                        .getLiteralValue().stringValue());
            }
            if (null != organizationAnnotationAttachment.getAttributeNameValuePairs().get("url")) {
                organization.setUrl(organizationAnnotationAttachment.getAttributeNameValuePairs().get("url")
                        .getLiteralValue().stringValue());
            }
            info.setVendorExtension("x-organization", organization);
        }
    }
    
    /**
     * Creates tag swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for tag.
     * @param swagger The swagger definition which the tags needs to be build on.
     */
    private void createTagModel(AnnotationAttributeValue annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue && annotationAttributeValue.getValueArray().length > 0) {
            List<Tag> tags = new LinkedList<>();
            for (AnnotationAttributeValue tagAttributeValue : annotationAttributeValue.getValueArray()) {
                AnnotationAttachment tagAnnotationAttachment = tagAttributeValue.getAnnotationValue();
                Tag tag = new Tag();
                if (null != tagAnnotationAttachment.getAttributeNameValuePairs().get("name")) {
                    tag.setName(tagAnnotationAttachment.getAttributeNameValuePairs().get("name")
                            .getLiteralValue().stringValue());
                }
                if (null != tagAnnotationAttachment.getAttributeNameValuePairs().get("description")) {
                    tag.setDescription(tagAnnotationAttachment.getAttributeNameValuePairs().get("description")
                            .getLiteralValue().stringValue());
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
    private void createExternalDocsModel(AnnotationAttributeValue annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue) {
            AnnotationAttachment externalDocAnnotationAttachment = annotationAttributeValue.getAnnotationValue();
            ExternalDocs externalDocs = new ExternalDocs();
            if (null != externalDocAnnotationAttachment.getAttributeNameValuePairs().get("description")) {
                externalDocs.setDescription(externalDocAnnotationAttachment.getAttributeNameValuePairs()
                        .get("description").getLiteralValue().stringValue());
            }
            if (null != externalDocAnnotationAttachment.getAttributeNameValuePairs().get("url")) {
                externalDocs.setUrl(externalDocAnnotationAttachment.getAttributeNameValuePairs().get("url")
                        .getLiteralValue().stringValue());
            }
    
            swagger.setExternalDocs(externalDocs);
        }
    }
    
    /**
     * Creates the contact swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for contact.
     * @param info The info definition which the contact needs to be build on.
     */
    private void createContactModel(AnnotationAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnotationAttachment contactAnnotationAttachment = annotationAttributeValue.getAnnotationValue();
            Contact contact = new Contact();
            if (null != contactAnnotationAttachment.getAttributeNameValuePairs().get("name")) {
                contact.setName(contactAnnotationAttachment.getAttributeNameValuePairs().get("name")
                        .getLiteralValue().stringValue());
            }
            if (null != contactAnnotationAttachment.getAttributeNameValuePairs().get("email")) {
                contact.setEmail(contactAnnotationAttachment.getAttributeNameValuePairs().get("email")
                        .getLiteralValue().stringValue());
            }
            if (null != contactAnnotationAttachment.getAttributeNameValuePairs().get("url")) {
                contact.setUrl(contactAnnotationAttachment.getAttributeNameValuePairs().get("url")
                        .getLiteralValue().stringValue());
            }
    
            info.setContact(contact);
        }
    }
    
    /**
     * Creates the license swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for license.
     * @param info The info definition which the license needs to be build on.
     */
    private void createLicenseModel(AnnotationAttributeValue annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnotationAttachment licenseAnnotationAttachment = annotationAttributeValue.getAnnotationValue();
            License license = new License();
            if (null != licenseAnnotationAttachment.getAttributeNameValuePairs().get("name")) {
                license.setName(licenseAnnotationAttachment.getAttributeNameValuePairs().get("name")
                        .getLiteralValue().stringValue());
            }
            if (null != licenseAnnotationAttachment.getAttributeNameValuePairs().get("url")) {
                license.setUrl(licenseAnnotationAttachment.getAttributeNameValuePairs().get("url")
                        .getLiteralValue().stringValue());
            }
            
            info.setLicense(license);
        }
    }
    
    /**
     * Parses the ballerina.net.http@config annotation and builds swagger definition. Also create the consumes and
     * produces annotations.
     * @param service The ballerina service which has the annotation.
     * @param swagger The swagger to build up.
     */
    private void parseConfigAnnotationAttachment(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> httpConfigAnnotationAttachment = Arrays.stream(service.getAnnotations())
                .filter(a -> this.checkIfHttpAnnotation(a) && "config".equals(a.getName()))
                .findFirst();
        if (httpConfigAnnotationAttachment.isPresent()) {
            if (null != httpConfigAnnotationAttachment.get().getAttributeNameValuePairs().get("basePath")) {
                swagger.setBasePath(httpConfigAnnotationAttachment.get().getAttributeNameValuePairs().get("basePath")
                        .getLiteralValue().stringValue());
            }
        }
    
        Optional<AnnotationAttachment> consumesAnnotationAttachment = Arrays.stream(service.getAnnotations())
                .filter(a -> this.checkIfHttpAnnotation(a) && "Consumes".equals(a.getName()))
                .findFirst();
        if (consumesAnnotationAttachment.isPresent()) {
            List<String> consumes = new LinkedList<>();
            AnnotationAttributeValue[] consumesValues = consumesAnnotationAttachment.get().getAttributeNameValuePairs()
                    .get("value").getValueArray();
            for (AnnotationAttributeValue consumesValue : consumesValues) {
                consumes.add(consumesValue.getLiteralValue().stringValue());
            }
    
            swagger.setConsumes(consumes);
        }
    
        Optional<AnnotationAttachment> producesAnnotationAttachment = Arrays.stream(service.getAnnotations())
                .filter(a -> this.checkIfHttpAnnotation(a) && "Produces".equals(a.getName()))
                .findFirst();
        if (producesAnnotationAttachment.isPresent()) {
            List<String> produces = new LinkedList<>();
            AnnotationAttributeValue[] producesValues = producesAnnotationAttachment.get().getAttributeNameValuePairs()
                    .get("value").getValueArray();
            for (AnnotationAttributeValue consumesValue : producesValues) {
                produces.add(consumesValue.getLiteralValue().stringValue());
            }
        
            swagger.setProduces(produces);
        }
    }
    
    /**
     * Checks if an annotation belongs to ballerina.net.http.swagger package.
     * @param annotationAttachment The annotation.
     * @return true if belongs to ballerina.net.http.swagger package, else false.
     */
    private boolean checkIfSwaggerAnnotation(AnnotationAttachment annotationAttachment) {
        return SWAGGER_PACKAGE_PATH.equals(annotationAttachment.getPkgPath()) &&
               SWAGGER_PACKAGE.equals(annotationAttachment.getPkgName());
    }
    
    /**
     * Checks if an annotation belongs to ballerina.net.http package.
     * @param annotationAttachment The annotation.
     * @return true if belongs to ballerina.net.http package, else false.
     */
    private boolean checkIfHttpAnnotation(AnnotationAttachment annotationAttachment) {
        return HTTP_PACKAGE_PATH.equals(annotationAttachment.getPkgPath()) &&
               HTTP_PACKAGE.equals(annotationAttachment.getPkgName());
    }
}

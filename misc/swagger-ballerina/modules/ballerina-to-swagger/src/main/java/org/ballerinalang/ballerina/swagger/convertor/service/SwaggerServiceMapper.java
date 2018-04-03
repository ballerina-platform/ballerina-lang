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
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.util.Json;
import org.ballerinalang.ballerina.swagger.convertor.service.model.Developer;
import org.ballerinalang.ballerina.swagger.convertor.service.model.Organization;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @param httpAlias The alias for ballerina/http package.
     * @param swaggerAlias The alias for ballerina.swagger package.
     */
    public SwaggerServiceMapper(String httpAlias, String swaggerAlias) {
        // Default object mapper is JSON mapper available in swagger utils.
        this.httpAlias = httpAlias;
        this.swaggerAlias = swaggerAlias;
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
    public Swagger convertServiceToSwagger(ServiceNode service) {
        Swagger swagger = new Swagger();
        // Setting default values.
        swagger.setBasePath('/' + service.getName().getValue());
        
        this.parseServiceInfoAnnotationAttachment(service, swagger);
        // TODO: parseSwaggerAnnotationAttachment(service, swagger);
        this.parseServiceConfigAnnotationAttachment(service, swagger);
        this.parseConfigAnnotationAttachment(service, swagger);
        
        SwaggerResourceMapper resourceMapper = new SwaggerResourceMapper(swagger, this.httpAlias, this.swaggerAlias);
        swagger.setPaths(resourceMapper.convertResourceToPath(service.getResources()));
        return swagger;
    }
    
    /**
     * Parses the 'ServiceConfig' annotation attachment.
     * @param service The ballerina service which has that annotation attachment.
     * @param swagger The swagger definition to build up.
     */
    private void parseServiceConfigAnnotationAttachment(ServiceNode service, Swagger swagger) {
        Optional<? extends AnnotationAttachmentNode> swaggerConfigAnnotation = service.getAnnotationAttachments()
                .stream()
                .filter(a -> null != swaggerAlias && this.swaggerAlias.equals(a.getPackageAlias().getValue()) &&
                             "ServiceConfig".equals(a.getAnnotationName().getValue()))
                .findFirst();
        
        if (swaggerConfigAnnotation.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> serviceConfigAttributes =
                    this.listToMap(swaggerConfigAnnotation.get());
            if (serviceConfigAttributes.containsKey("host")) {
                swagger.setHost(this.getStringLiteralValue(serviceConfigAttributes.get("host")));
            }
            if (serviceConfigAttributes.containsKey("schemes") &&
                                                    serviceConfigAttributes.get("schemes").getValueArray().size() > 0) {
                List<Scheme> schemes = new LinkedList<>();
                for (AnnotationAttachmentAttributeValueNode schemesNodes : serviceConfigAttributes.get("schemes")
                        .getValueArray()) {
                    String schemeStringValue = this.getStringLiteralValue(schemesNodes);
                    if (null != Scheme.forValue(schemeStringValue)) {
                        schemes.add(Scheme.forValue(schemeStringValue));
                    }
                }
                if (schemes.size() > 0) {
                    schemes = Lists.reverse(schemes);
                    swagger.setSchemes(schemes);
                }
            }
            this.createSecurityDefinitionsModel(serviceConfigAttributes.get("authorizations"), swagger);
        }
    }
    
    /**
     * Creates the security definition models for swagger definition.
     * @param annotationAttributeValue The annotation attribute value for security definitions.
     * @param swagger The swagger definition.
     */
    private void createSecurityDefinitionsModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue,
                                                                                                    Swagger swagger) {
        if (null != annotationAttributeValue) {
            Map<String, SecuritySchemeDefinition> securitySchemeDefinitionMap = new HashMap<>();
            for (AnnotationAttachmentAttributeValueNode authorizationValues :
                                                                            annotationAttributeValue.getValueArray()) {
                if (authorizationValues instanceof AnnotationAttachmentNode) {
                    AnnotationAttachmentNode authAnnotationAttachment = (AnnotationAttachmentNode) authorizationValues;
                    Map<String, AnnotationAttachmentAttributeValueNode> authAttributes =
                                                                            this.listToMap(authAnnotationAttachment);
                    if (authAttributes.containsKey("name") && authAttributes.containsKey("authType")) {
                        String name = this.getStringLiteralValue(authAttributes.get("name"));
                        String type = this.getStringLiteralValue(authAttributes.get("authType"));
                        String description = "";
                        if (authAttributes.containsKey("description")) {
                            description = this.getStringLiteralValue(authAttributes.get("description"));
                        }
                        if ("basic".equals(type)) {
                            BasicAuthDefinition basicAuthDefinition = new BasicAuthDefinition();
                            basicAuthDefinition.setDescription(description);
                            securitySchemeDefinitionMap.put(name, basicAuthDefinition);
                        } else if ("apiKey".equals(type)) {
                            ApiKeyAuthDefinition apiKeyAuthDefinition = new ApiKeyAuthDefinition();
                            apiKeyAuthDefinition.setName(this.getStringLiteralValue(authAttributes.get("apiName")));
                            apiKeyAuthDefinition.setIn(In.forValue(this.getStringLiteralValue(authAttributes
                                                                                                        .get("in"))));
                            apiKeyAuthDefinition.setDescription(description);
                            securitySchemeDefinitionMap.put(name, apiKeyAuthDefinition);
                        } else if ("oauth2".equals(type)) {
                            OAuth2Definition oAuth2Definition = new OAuth2Definition();
                            oAuth2Definition.setFlow(this.getStringLiteralValue(authAttributes.get("flow")));
                            oAuth2Definition.setAuthorizationUrl(this.getStringLiteralValue(authAttributes
                                                                                            .get("authorizationUrl")));
                            oAuth2Definition.setTokenUrl(this.getStringLiteralValue(authAttributes.get("tokenUrl")));
    
                            this.createSecurityDefinitionScopesModel(authAttributes.get("authorizationScopes"),
                                                                                                    oAuth2Definition);
    
                            oAuth2Definition.setDescription(description);
                            securitySchemeDefinitionMap.put(name, oAuth2Definition);
                        }
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
    private void createSecurityDefinitionScopesModel(AnnotationAttachmentAttributeValueNode authorizationScopes,
                                                     OAuth2Definition oAuth2Definition) {
        Map<String, String> scopes = new HashMap<>();
        for (AnnotationAttachmentAttributeValueNode authScopeValue : authorizationScopes.getValueArray()) {
            if (authScopeValue instanceof AnnotationAttachmentNode) {
                AnnotationAttachmentNode authScopeAnnotationAttachment = (AnnotationAttachmentNode) authScopeValue;
                Map<String, AnnotationAttachmentAttributeValueNode> authScopeAttributes =
                        this.listToMap(authScopeAnnotationAttachment);
                String name = this.getStringLiteralValue(authScopeAttributes.get("name"));
                String description = this.getStringLiteralValue(authScopeAttributes.get("description"));
                scopes.put(name, description);
            }
        }
        oAuth2Definition.setScopes(scopes);
    }
    
    /**
     * Parses the 'ServiceInfo' annotation and build the swagger definition for it.
     * @param service The ballerina service which has the 'ServiceInfo' annotation attachment.
     * @param swagger The swagger definition to be built up.
     */
    private void parseServiceInfoAnnotationAttachment(ServiceNode service, Swagger swagger) {
        Optional<? extends AnnotationAttachmentNode> swaggerInfoAnnotation = service.getAnnotationAttachments().stream()
                .filter(a -> null != swaggerAlias && this.swaggerAlias.equals(a.getPackageAlias().getValue()) &&
                             "ServiceInfo".equals(a.getAnnotationName().getValue()))
                .findFirst();
        
        Info info = new Info()
                .version("1.0.0")
                .title(service.getName().getValue());
        if (swaggerInfoAnnotation.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> attributes =
                                                                        this.listToMap(swaggerInfoAnnotation.get());
            if (attributes.containsKey("serviceVersion")) {
                info.version(this.getStringLiteralValue(attributes.get("serviceVersion")));
            }
            if (attributes.containsKey("title")) {
                info.title(this.getStringLiteralValue(attributes.get("title")));
            }
            if (attributes.containsKey("description")) {
                info.description(this.getStringLiteralValue(attributes.get("description")));
            }
            if (attributes.containsKey("termsOfService")) {
                info.termsOfService(this.getStringLiteralValue(attributes.get("termsOfService")));
            }
            this.createContactModel(attributes.get("contact"), info);
            this.createLicenseModel(attributes.get("license"), info);
    
            this.createExternalDocModel(attributes.get("externalDoc"), swagger);
            this.createTagModel(attributes.get("tags"), swagger);
            this.createOrganizationModel(attributes.get("organization"), info);
            this.createDevelopersModel(attributes.get("developers"), info);
        }
        swagger.setInfo(info);
    }
    
    /**
     * Creates vendor extension for developers.
     * @param annotationAttributeValue The annotation attribute value for developer vendor extension.
     * @param info The info definition.
     */
    private void createDevelopersModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            List<Developer> developers = new LinkedList<>();
            for (AnnotationAttachmentAttributeValueNode value : annotationAttributeValue.getValueArray()) {
                AnnotationAttachmentNode developerAnnotation = (AnnotationAttachmentNode) value.getValue();
                Map<String, AnnotationAttachmentAttributeValueNode> developerAttributes =
                                                                                    this.listToMap(developerAnnotation);
                Developer developer = new Developer();
                if (developerAttributes.containsKey("name")) {
                    developer.setName(this.getStringLiteralValue(developerAttributes.get("name")));
                }
                if (developerAttributes.containsKey("email")) {
                    developer.setEmail(this.getStringLiteralValue(developerAttributes.get("email")));
                }
                developers.add(developer);
            }
            info.setVendorExtension("x-developers", Lists.reverse(developers));
        }
    }
    
    /**
     * Creates vendor extension for organization.
     * @param annotationAttributeValue The annotation attribute value for organization vendor extension.
     * @param info The info definition.
     */
    private void createOrganizationModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnotationAttachmentNode organizationAnnotationAttachment =
                                                        (AnnotationAttachmentNode) annotationAttributeValue.getValue();
            
            Map<String, AnnotationAttachmentAttributeValueNode> organizationAttributes =
                    this.listToMap(organizationAnnotationAttachment);
            Organization organization = new Organization();
            if (organizationAttributes.containsKey("name")) {
                organization.setName(this.getStringLiteralValue(organizationAttributes.get("name")));
            }
            if (organizationAttributes.containsKey("url")) {
                organization.setUrl(this.getStringLiteralValue(organizationAttributes.get("url")));
            }
            info.setVendorExtension("x-organization", organization);
        }
    }
    
    /**
     * Creates tag swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for tag.
     * @param swagger The swagger definition which the tags needs to be build on.
     */
    private void createTagModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Swagger swagger) {
        if (null != annotationAttributeValue) {
            List<Tag> tags = new LinkedList<>();
            for (AnnotationAttachmentAttributeValueNode value : annotationAttributeValue.getValueArray()) {
                AnnotationAttachmentNode tagAnnotation = (AnnotationAttachmentNode) value.getValue();
                Map<String, AnnotationAttachmentAttributeValueNode> tagAttributes =
                        this.listToMap(tagAnnotation);
                Tag tag = new Tag();
                if (tagAttributes.containsKey("name")) {
                    tag.setName(this.getStringLiteralValue(tagAttributes.get("name")));
                }
                if (tagAttributes.containsKey("description")) {
                    tag.setDescription(this.getStringLiteralValue(tagAttributes.get("description")));
                }
    
                tags.add(tag);
            }
    
            swagger.setTags(Lists.reverse(tags));
        }
    }
    
    /**
     * Creates external docs swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for external docs.
     * @param swagger The swagger definition which the external docs needs to be build on.
     */
    private void createExternalDocModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue,
                                                                                                    Swagger swagger) {
        if (null != annotationAttributeValue) {
            AnnotationAttachmentNode externalDocAnnotationAttachment =
                                                        (AnnotationAttachmentNode) annotationAttributeValue.getValue();
            Map<String, AnnotationAttachmentAttributeValueNode> externalDocAttributes =
                    this.listToMap(externalDocAnnotationAttachment);
            ExternalDocs externalDocs = new ExternalDocs();
            if (externalDocAttributes.containsKey("description")) {
                externalDocs.setDescription(this.getStringLiteralValue(externalDocAttributes.get("description")));
            }
            if (externalDocAttributes.containsKey("url")) {
                externalDocs.setUrl(this.getStringLiteralValue(externalDocAttributes.get("url")));
            }
    
            swagger.setExternalDocs(externalDocs);
        }
    }
    
    /**
     * Creates the contact swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for contact.
     * @param info The info definition which the contact needs to be build on.
     */
    private void createContactModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnotationAttachmentNode contactAnnotationAttachment =
                                                        (AnnotationAttachmentNode) annotationAttributeValue.getValue();
            
            Map<String, AnnotationAttachmentAttributeValueNode> contactAttributes =
                    this.listToMap(contactAnnotationAttachment);
            Contact contact = new Contact();
            if (contactAttributes.containsKey("name")) {
                contact.setName(this.getStringLiteralValue(contactAttributes.get("name")));
            }
            if (contactAttributes.containsKey("email")) {
                contact.setEmail(this.getStringLiteralValue(contactAttributes.get("email")));
            }
            if (contactAttributes.containsKey("url")) {
                contact.setUrl(this.getStringLiteralValue(contactAttributes.get("url")));
            }
    
            info.setContact(contact);
        }
    }
    
    /**
     * Creates the license swagger definition.
     * @param annotationAttributeValue The ballerina annotation attribute value for license.
     * @param info The info definition which the license needs to be build on.
     */
    private void createLicenseModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Info info) {
        if (null != annotationAttributeValue) {
            AnnotationAttachmentNode licenseAnnotationAttachment =
                                                        (AnnotationAttachmentNode) annotationAttributeValue.getValue();
            Map<String, AnnotationAttachmentAttributeValueNode> licenseAttributes =
                    this.listToMap(licenseAnnotationAttachment);
            License license = new License();
            if (licenseAttributes.containsKey("name")) {
                license.setName(this.getStringLiteralValue(licenseAttributes.get("name")));
            }
            if (licenseAttributes.containsKey("url")) {
                license.setUrl(this.getStringLiteralValue(licenseAttributes.get("url")));
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
    private void parseConfigAnnotationAttachment(ServiceNode service, Swagger swagger) {
        Optional<? extends AnnotationAttachmentNode> httpConfigAnnotationAttachment = service.getAnnotationAttachments()
                .stream()
                .filter(a -> null != this.httpAlias && this.httpAlias.equals(a.getPackageAlias().getValue()) &&
                             "configuration".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (httpConfigAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> configAttributes =
                    this.listToMap(httpConfigAnnotationAttachment.get());
            if (configAttributes.containsKey("basePath")) {
                swagger.setBasePath(this.getStringLiteralValue(configAttributes.get("basePath")));
            }
            if (configAttributes.containsKey("host") && configAttributes.containsKey("port")) {
                swagger.setHost(this.getStringLiteralValue(configAttributes.get("host")) + ":" +
                                                           this.getStringLiteralValue(configAttributes.get("port")));
            }
        }
    
        Optional<? extends AnnotationAttachmentNode> consumesAnnotationAttachment = service.getAnnotationAttachments()
                .stream()
                .filter(a -> null != this.httpAlias && this.httpAlias.equals(a.getPackageAlias().getValue()) &&
                             "Consumes".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (consumesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> consumesAttributes =
                    this.listToMap(consumesAnnotationAttachment.get());
            List<String> consumes = new LinkedList<>();
            for (AnnotationAttachmentAttributeValueNode consumesValue :
                                                                    consumesAttributes.get("value").getValueArray()) {
                consumes.add(this.getStringLiteralValue(consumesValue));
            }
    
            swagger.setConsumes(consumes);
        }
    
        Optional<? extends AnnotationAttachmentNode> producesAnnotationAttachment = service.getAnnotationAttachments()
                .stream()
                .filter(a -> null != this.httpAlias && this.httpAlias.equals(a.getPackageAlias().getValue()) &&
                             "Produces".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (producesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> consumesAttributes =
                    this.listToMap(producesAnnotationAttachment.get());
            List<String> produces = new LinkedList<>();
            for (AnnotationAttachmentAttributeValueNode consumesValue :
                                                                    consumesAttributes.get("value").getValueArray()) {
                produces.add(this.getStringLiteralValue(consumesValue));
            }
        
            swagger.setProduces(produces);
        }
    }
    
    /**
     * Converts the attributes of an annotation to a map of key being attribute key and value being an annotation
     * attachment value.
     * @param annotation The annotation attachment node.
     * @return A map of attributes.
     */
    private Map<String, AnnotationAttachmentAttributeValueNode> listToMap(AnnotationAttachmentNode annotation) {
        return annotation.getAttributes().stream().collect(
                Collectors.toMap(attribute -> attribute.getName().getValue(), AnnotationAttachmentAttributeNode
                        ::getValue));
    }
    
    /**
     * Coverts the string value of an annotation attachment to a string.
     * @param valueNode The annotation attachment.
     * @return The string value.
     */
    private String getStringLiteralValue(AnnotationAttachmentAttributeValueNode valueNode) {
        return ((LiteralNode) valueNode.getValue()).getValue().toString();
    }
}

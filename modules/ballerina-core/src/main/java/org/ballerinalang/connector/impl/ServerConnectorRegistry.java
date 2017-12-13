/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.AnnotationValueType;
import org.ballerinalang.connector.api.BallerinaServerConnector;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeKeyValuePair;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * {@code ServerConnectorRegistry} This will hold all server connectors registered at ballerina side.
 * It will also be responsible for notifying service registration events to the connector implementation.
 *
 * @since 0.94
 */
public class ServerConnectorRegistry {

    private Map<String, BallerinaServerConnector> serverConnectorMap = new HashMap<>();
    private boolean initialized = false;

    public void initServerConnectors() {
        if (initialized) {
            return;
        }
        ServiceLoader<BallerinaServerConnector> serverConnectorServiceLoader =
                ServiceLoader.load(BallerinaServerConnector.class);
        serverConnectorServiceLoader.forEach(serverConnector -> {
            serverConnector.getProtocolPackages().forEach(protocolPkg -> {
                if (!serverConnectorMap.containsKey(protocolPkg)) {
                    serverConnectorMap.put(protocolPkg, serverConnector);
                } else {
                    throw new BLangRuntimeException("Multiple server connectors in the runtime for" +
                                                            " given protocol package - " + protocolPkg);
                }
            });
        });
        initialized = true;
    }

    /**
     * This method will notify underline server connectors about the deployment complete event.
     */
    public void deploymentComplete() {
        serverConnectorMap.values(

        ).forEach(sc -> sc.deploymentComplete());
    }

    /**
     * This method is used to register service to relevant server connector implementation.
     *
     * @param serviceInfo to be registered.
     */
    public void registerService(ServiceInfo serviceInfo) {
        if (!serverConnectorMap.containsKey(serviceInfo.getProtocolPkgPath())) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_SERVICE_PROTOCOL,
                    serviceInfo.getProtocolPkgPath());
        }
        Service service = buildService(serviceInfo);
        serverConnectorMap.get(serviceInfo.getProtocolPkgPath()).serviceRegistered(service);
    }

    /**
     * This method is used to get {@code BallerinaServerConnector} instance for the given protocol package.
     *
     * @param protocolPkgPath of the server connector.
     * @return ballerinaServerConnector object.
     */
    public BallerinaServerConnector getBallerinaServerConnector(String protocolPkgPath) {
        return serverConnectorMap.get(protocolPkgPath);
    }

    private Service buildService(ServiceInfo serviceInfo) {
        BService service =
                new BService(serviceInfo.getName(), serviceInfo.getPackagePath(), serviceInfo.getProtocolPkgPath());
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) serviceInfo.getAttributeInfo(
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo != null) {
            for (AnnAttachmentInfo annotationInfo : attributeInfo.getAttachmentInfoEntries()) {
                BAnnotation annotation = buildAnnotation(annotationInfo);
                service.addAnnotation(annotation.getKey(), annotation);
            }
        }
        for (ResourceInfo resourceInfo : serviceInfo.getResourceInfoEntries()) {
            BResource resource = buildResource(resourceInfo);
            service.addResource(resource.getName(), resource);
        }
        return service;
    }

    private BResource buildResource(ResourceInfo resourceInfo) {
        BResource resource = new BResource(resourceInfo.getName(), resourceInfo);
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) resourceInfo.getAttributeInfo(
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo != null) {
            for (AnnAttachmentInfo annotationInfo : attributeInfo.getAttachmentInfoEntries()) {
                BAnnotation annotation = buildAnnotation(annotationInfo);
                resource.addAnnotation(annotation.getKey(), annotation);
            }
        }
        return resource;
    }

    private BAnnotation buildAnnotation(AnnAttachmentInfo annAttachmentInfo) {
        BAnnotation annotation = new BAnnotation(annAttachmentInfo.getName(), annAttachmentInfo.getPkgPath());

        for (AnnAttributeKeyValuePair keyValuePair : annAttachmentInfo.getAttributeKeyValuePairs()) {
            AnnAttributeValue attributeValue = keyValuePair.getAttributeValue();
            AnnAttrValue annotationValue = getAttributeValue(attributeValue);
            annotation.addAnnotationValue(keyValuePair.getAttributeName(), annotationValue);
        }
        return annotation;
    }


    private AnnAttrValue getAttributeValue(AnnAttributeValue attributeValue) {
        BAnnAttrValue annotationValue = null;
        switch (attributeValue.getTypeDesc()) {
            case TypeSignature.SIG_INT:
                annotationValue = new BAnnAttrValue(AnnotationValueType.INT);
                annotationValue.setIntValue(attributeValue.getIntValue());
                break;
            case TypeSignature.SIG_FLOAT:
                annotationValue = new BAnnAttrValue(AnnotationValueType.FLOAT);
                annotationValue.setFloatValue(attributeValue.getFloatValue());
                break;
            case TypeSignature.SIG_STRING:
                annotationValue = new BAnnAttrValue(AnnotationValueType.STRING);
                annotationValue.setStringValue(attributeValue.getStringValue());
                break;
            case TypeSignature.SIG_BOOLEAN:
                annotationValue = new BAnnAttrValue(AnnotationValueType.BOOLEAN);
                annotationValue.setBooleanValue(attributeValue.getBooleanValue());
                break;
            case TypeSignature.SIG_ANNOTATION:
                annotationValue = new BAnnAttrValue(AnnotationValueType.ANNOTATION);
                annotationValue.setAnnotation(buildAnnotation(attributeValue.getAnnotationAttachmentValue()));
                break;
            case TypeSignature.SIG_ARRAY:
                int length = attributeValue.getAttributeValueArray().length;
                AnnAttrValue[] annotationValues = new AnnAttrValue[length];
                AnnAttributeValue[] annAttributeValues = attributeValue.getAttributeValueArray();
                for (int i = 0; i < length; i++) {
                    annotationValues[i] = getAttributeValue(annAttributeValues[i]);
                }
                annotationValue = new BAnnAttrValue(AnnotationValueType.ARRAY);
                annotationValue.setAnnotationValueArray(annotationValues);
                break;
            default:
        }
        return annotationValue;
    }


}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.config.EndPointConfiguration;
import org.ballerinalang.net.grpc.ssl.SSLConfig;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.util.List;

/**
 * Util class of connector functions.
 */
public class ConnectorUtil {
    public static EndPointConfiguration generateServiceConfiguration(Struct serviceEndpointConfig) {
        EndPointConfiguration endPointConfiguration = new EndPointConfiguration();
        endPointConfiguration.setPort(serviceEndpointConfig.getIntField("port"));
        endPointConfiguration.setHost(serviceEndpointConfig.getStringField("host"));
        return endPointConfiguration;
    }
    
    public static SSLHandlerFactory getSSLConfigs(Annotation serviceAnnotation) {
        if (serviceAnnotation == null) {
            return null;
        }
        AnnAttrValue keyStoreFile = serviceAnnotation.getAnnAttrValue("keyStoreFile");
        AnnAttrValue keyStorePassword = serviceAnnotation.getAnnAttrValue("keyStorePassword");
        AnnAttrValue certPassword = serviceAnnotation.getAnnAttrValue("certPassword");
        if (keyStoreFile == null || certPassword == null) {
            return null;
        } else {
            SSLConfig sslConfig = new SSLConfig(new File(keyStoreFile.getStringValue())
                    , keyStorePassword.getStringValue());
            sslConfig.setCertPass(certPassword.getStringValue());
            sslConfig.setTLSStoreType("PKCS12");
            SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
            return sslHandlerFactory;
        }
    }
    
    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service.getAnnotationList(pkgPath, "serviceConfig");
        
        if (annotationList == null) {
            return null;
        }
        
        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }
        
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
    
    public static Annotation getResourceConfigAnnotation(Resource resource, String pkgPath) {
        List<Annotation> annotationList = resource.getAnnotationList(pkgPath, "resourceConfig");
        
        if (annotationList == null) {
            return null;
        }
        
        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple resource configuration annotations found in resource: " +
                            resource.getServiceName() + "." + resource.getName());
        }
        
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}

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

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.List;

/**
 * Util class of connector functions.
 */
public class ConnectorUtil {
    public static EndpointConfiguration generateServiceConfiguration(Struct serviceEndpointConfig) {
        EndpointConfiguration endPointConfiguration = new EndpointConfiguration();
        endPointConfiguration.setPort((Math.toIntExact(serviceEndpointConfig.getIntField("port"))));
        endPointConfiguration.setHost(serviceEndpointConfig.getStringField("host"));
        return endPointConfiguration;
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

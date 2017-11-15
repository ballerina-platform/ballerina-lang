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
package org.ballerinalang.net.http;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.BallerinaServerConnector;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * {@code HttpServerConnector} This is the http implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.94
 */
@JavaSPIService("org.ballerinalang.connector.api.BallerinaServerConnector")
public class HttpServerConnector implements BallerinaServerConnector {
    private static final Logger log = LoggerFactory.getLogger(HttpServerConnector.class);

    private CopyOnWriteArrayList<String> sortedServiceURIs = new CopyOnWriteArrayList<>();

    public HttpServerConnector() {
    }

    @Override
    public String getProtocolPackage() {
        return Constants.PROTOCOL_PACKAGE_HTTP;
    }

    @Override
    public void serviceRegistered(Service service) throws BallerinaConnectorException {
        HttpService httpService = new HttpService(service);
        HTTPServicesRegistry.getInstance().registerService(httpService);
        CorsPopulator.populateServiceCors(httpService);
        List<HttpResource> resources = new ArrayList<>();
        for (Resource resource : service.getResources()) {
            HttpResource httpResource = buildHttpResource(resource);
            validateResourceSignature(httpResource);
            try {
                httpService.getUriTemplate().parse(httpResource.getPath(), httpResource);
            } catch (URITemplateException e) {
                throw new BallerinaConnectorException(e.getMessage());
            }
            CorsPopulator.processResourceCors(httpResource, httpService);
            resources.add(httpResource);
        }
        httpService.setResources(resources);
        httpService.setAllAllowMethods(DispatcherUtil.getAllResourceMethods(httpService));
        //basePath will get cached after registering service
        sortedServiceURIs.add(httpService.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());
    }

    @Override
    public void serviceUnregistered(Service service) throws BallerinaConnectorException {
        HttpService httpService = new HttpService(service);
        HTTPServicesRegistry.getInstance().unregisterService(httpService);

        //basePath will get cached after unregistering the service
        sortedServiceURIs.remove(httpService.getBasePath());
    }

    @Override
    public void deploymentComplete() throws BallerinaConnectorException {
        HttpUtil.startPendingHttpConnectors();
    }

    public HttpService findService(HTTPCarbonMessage cMsg) {

        try {
            String interfaceId = getInterface(cMsg);
            Map<String, HttpService> servicesOnInterface = HTTPServicesRegistry
                    .getInstance().getServicesInfoByInterface(interfaceId);
            if (servicesOnInterface == null) {
                throw new BallerinaConnectorException("No services found for interface : " + interfaceId);
            }
            String uriStr = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
            //replace multiple slashes from single slash if exist in request path to enable
            // dispatchers when request path contains multiple slashes
            URI requestUri = URI.create(uriStr.replaceAll("//+", Constants.DEFAULT_BASE_PATH));
            if (requestUri == null) {
                throw new BallerinaConnectorException("uri not found in the message or found an invalid URI.");
            }

            // Most of the time we will find service from here
            String basePath = findTheMostSpecificBasePath(requestUri.getPath(), servicesOnInterface);
            HttpService service = servicesOnInterface.get(basePath);
            if (service == null) {
                cMsg.setProperty(Constants.HTTP_STATUS_CODE, 404);
                throw new BallerinaConnectorException("no matching service found for path : " + uriStr);
            }

            String subPath = URIUtil.getSubPath(requestUri.getPath(), basePath);
            cMsg.setProperty(Constants.BASE_PATH, basePath);
            cMsg.setProperty(Constants.SUB_PATH, subPath);
            cMsg.setProperty(Constants.QUERY_STR, requestUri.getQuery());
            //store query params comes with request as it is
            cMsg.setProperty(Constants.RAW_QUERY_STR, requestUri.getRawQuery());

            return service;
        } catch (Throwable e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }


    protected String getInterface(HTTPCarbonMessage cMsg) {
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = Constants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

    private String findTheMostSpecificBasePath(String requestURIPath, Map<String, HttpService> services) {
        for (Object key : sortedServiceURIs) {
            if (!requestURIPath.toLowerCase().contains(key.toString().toLowerCase())) {
                continue;
            }
            if (requestURIPath.length() <= key.toString().length()) {
                return key.toString();
            }
            if (requestURIPath.charAt(key.toString().length()) == '/') {
                return key.toString();
            }
        }
        if (services.containsKey(Constants.DEFAULT_BASE_PATH)) {
            return Constants.DEFAULT_BASE_PATH;
        }
        return null;
    }

    private void validateResourceSignature(HttpResource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();

        if (paramDetails.size() < 2) {
            throw new BallerinaConnectorException("resource signature parameter count should be more than two");
        }

        //Validate request parameter
        ParamDetail reqParamDetail = paramDetails.get(0);
        if (reqParamDetail == null) {
            throw new BallerinaConnectorException("request parameter cannot be null");
        }
        if (reqParamDetail.getVarType().getPackagePath() == null
                || !reqParamDetail.getVarType().getPackagePath().equals(Constants.PROTOCOL_PACKAGE_HTTP)
                || !reqParamDetail.getVarType().getName().equals(Constants.REQUEST)) {
            throw new BallerinaConnectorException("request parameter should be of type - "
                    + Constants.PROTOCOL_PACKAGE_HTTP + ":" + Constants.REQUEST);
        }

        //validate response parameter
        ParamDetail respParamDetail = paramDetails.get(1);
        if (respParamDetail == null) {
            throw new BallerinaConnectorException("response parameter cannot be null");
        }
        if (respParamDetail.getVarType().getPackagePath() == null
                || !respParamDetail.getVarType().getPackagePath().equals(Constants.PROTOCOL_PACKAGE_HTTP)
                || !respParamDetail.getVarType().getName().equals(Constants.RESPONSE)) {
            throw new BallerinaConnectorException("response parameter should be of type - "
                    + Constants.PROTOCOL_PACKAGE_HTTP + ":" + Constants.REQUEST);
        }

        //validate rest of the parameters
        for (int i = 2; i < paramDetails.size(); i++) {
            ParamDetail paramDetail = paramDetails.get(i);
            if (!paramDetail.getVarType().getName().equals(Constants.TYPE_STRING)) {
                throw new BallerinaConnectorException("incompatible resource signature parameter type");
            }
        }
    }

    private HttpResource buildHttpResource(Resource resource) {
        HttpResource httpResource = new HttpResource(resource);
        Annotation rConfigAnnotation = resource.getAnnotation(Constants.HTTP_PACKAGE_PATH,
                Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnotation == null) {
            if (log.isDebugEnabled()) {
                log.debug("resourceConfig not specified in the Resource, using default sub path");
            }
            httpResource.setPath(resource.getName());
            return httpResource;
        }
        String subPath;
        AnnAttrValue pathAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_PATH);
        if (pathAttrVal == null) {
            if (log.isDebugEnabled()) {
                log.debug("Path not specified in the Resource, using default sub path");
            }
            subPath = resource.getName();
        } else {
            subPath = pathAttrVal.getStringValue().trim();
        }
        if (subPath.isEmpty()) {
            subPath = Constants.DEFAULT_BASE_PATH;
        }
        httpResource.setPath(subPath);

        AnnAttrValue methodsAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_METHODS);
        if (methodsAttrVal != null) {
            httpResource.setMethods(DispatcherUtil.getValueList(methodsAttrVal, null));
        }
        AnnAttrValue consumesAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_CONSUMES);
        if (consumesAttrVal != null) {
            httpResource.setConsumes(DispatcherUtil.getValueList(consumesAttrVal, null));
        }
        AnnAttrValue producesAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_PRODUCES);
        if (producesAttrVal != null) {
            httpResource.setProduces(DispatcherUtil.getValueList(producesAttrVal, null));
        }
        if (httpResource.getProduces() != null) {
            List<String> subAttributeValues = httpResource.getProduces().stream()
                    .map(mediaType -> mediaType.trim()
                            .substring(0, mediaType.indexOf("/")))
                    .distinct().collect(Collectors.toList());
            httpResource.setProducesSubTypes(subAttributeValues);
        }
        return httpResource;
    }
}

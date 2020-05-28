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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_CHUNKING;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_INTERRUPTIBLE;
import static org.ballerinalang.net.http.HttpConstants.DEFAULT_BASE_PATH;
import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.http.HttpConstants.DOLLAR;
import static org.ballerinalang.net.http.HttpConstants.PACKAGE_BALLERINA_BUILTIN;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpUtil.checkConfigAnnotationAvailability;

/**
 * {@code HttpService} This is the http wrapper for the {@code Service} implementation.
 *
 * @since 0.94
 */
public class HttpService implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(HttpService.class);

    protected static final BString BASE_PATH_FIELD = StringUtils.fromString("basePath");
    private static final String COMPRESSION_FIELD = "compression";
    private static final BString CORS_FIELD = StringUtils.fromString("cors");
    private static final BString VERSIONING_FIELD = StringUtils.fromString("versioning");
    private static final BString HOST_FIELD = StringUtils.fromString("host");

    private ObjectValue balService;
    private List<HttpResource> resources;
    private List<HttpResource> upgradeToWebSocketResources;
    private List<String> allAllowedMethods;
    private String basePath;
    private CorsHeaders corsHeaders;
    private URITemplate<HttpResource, HttpCarbonMessage> uriTemplate;
    private boolean keepAlive = true; //default behavior
    private MapValue<BString, Object> compression;
    private String hostName;
    private boolean interruptible;
    private String chunkingConfig;

    protected HttpService(ObjectValue service) {
        this.balService = service;
    }

    public java.lang.Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    private void setCompressionConfig(MapValue<BString, Object> compression) {
        this.compression = compression;
    }

    public MapValue<BString, Object> getCompressionConfig() {
        return this.compression;
    }

    public void setChunkingConfig(String chunkingConfig) {
        this.chunkingConfig = chunkingConfig;
    }

    public String getChunkingConfig() {
        return chunkingConfig;
    }

    public String getName() {
        return HttpUtil.getServiceName(balService);
    }

    public String getPackage() {
        return balService.getType().getPackage().getName();
    }

    public ObjectValue getBalService() {
        return balService;
    }

    public List<HttpResource>   getResources() {
        return resources;
    }

    public void setResources(List<HttpResource> resources) {
        this.resources = resources;
    }

    public List<String> getAllAllowedMethods() {
        return allAllowedMethods;
    }

    public void setAllAllowedMethods(List<String> allAllowMethods) {
        this.allAllowedMethods = allAllowMethods;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isInterruptible() {
        return interruptible;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        if (basePath == null || basePath.trim().isEmpty()) {
            this.basePath = DEFAULT_BASE_PATH.concat(this.getName().startsWith(DOLLAR) ? "" : this.getName());
        } else {
            this.basePath = HttpUtil.sanitizeBasePath(basePath);
        }
    }

    public CorsHeaders getCorsHeaders() {
        return corsHeaders;
    }

    public void setCorsHeaders(CorsHeaders corsHeaders) {
        this.corsHeaders = corsHeaders;
        if (this.corsHeaders == null || !this.corsHeaders.isAvailable()) {
            return;
        }
        if (this.corsHeaders.getAllowOrigins() == null) {
            this.corsHeaders.setAllowOrigins(Stream.of("*").collect(Collectors.toList()));
        }
        if (this.corsHeaders.getAllowMethods() == null) {
            this.corsHeaders.setAllowMethods(Stream.of("*").collect(Collectors.toList()));
        }
    }

    public List<HttpResource> getUpgradeToWebSocketResources() {
        return upgradeToWebSocketResources;
    }

    public void setUpgradeToWebSocketResources(
            List<HttpResource> upgradeToWebSocketResources) {
        this.upgradeToWebSocketResources = upgradeToWebSocketResources;
    }

    public URITemplate<HttpResource, HttpCarbonMessage> getUriTemplate() throws URITemplateException {
        if (uriTemplate == null) {
            uriTemplate = new URITemplate<>(new Literal<>(new HttpResourceDataElement(), "/"));
        }
        return uriTemplate;
    }

    public static List<HttpService> buildHttpService(ObjectValue service) {
        List<HttpService> serviceList = new ArrayList<>();
        List<String> basePathList = new ArrayList<>();
        HttpService httpService = new HttpService(service);
        MapValue<BString, Object> serviceConfig = getHttpServiceConfigAnnotation(service);
        httpService.setInterruptible(hasInterruptibleAnnotation(service));

        if (checkConfigAnnotationAvailability(serviceConfig)) {

            httpService.setCompressionConfig(
                    (MapValue<BString, Object>) serviceConfig.get(ANN_CONFIG_ATTR_COMPRESSION));
            httpService.setChunkingConfig(serviceConfig.get(ANN_CONFIG_ATTR_CHUNKING).toString());
            httpService.setCorsHeaders(CorsHeaders.buildCorsHeaders(serviceConfig.getMapValue(CORS_FIELD)));
            httpService.setHostName(serviceConfig.getStringValue(HOST_FIELD).getValue().trim());

            String basePath = serviceConfig.getStringValue(BASE_PATH_FIELD).getValue().replaceAll(
                    HttpConstants.REGEX, HttpConstants.SINGLE_SLASH);
            if (basePath.contains(HttpConstants.VERSION)) {
                prepareBasePathList(serviceConfig.getMapValue(VERSIONING_FIELD),
                                    serviceConfig.getStringValue(BASE_PATH_FIELD).getValue(), basePathList,
                                    httpService.getBalService().getType().getPackage().getVersion());
            } else {
                basePathList.add(basePath);
            }
        } else {
            log.debug("serviceConfig not specified in the Service instance, using default base path");
            //service name cannot start with / hence concat
            String basePath = httpService.getName().startsWith(DOLLAR) ? DEFAULT_BASE_PATH :
                    DEFAULT_BASE_PATH.concat(httpService.getName());
            basePathList.add(basePath);
            httpService.setHostName(DEFAULT_HOST);
        }

        processResources(httpService);

        httpService.setAllAllowedMethods(DispatcherUtil.getAllResourceMethods(httpService));

        if (basePathList.size() == 1) {
            httpService.setBasePath(basePathList.get(0));
            serviceList.add(httpService);
            return serviceList;
        }

        for (String basePath : basePathList) {
            HttpService tempHttpService;
            try {
                tempHttpService = (HttpService) httpService.clone();
            } catch (CloneNotSupportedException e) {
                throw new BallerinaConnectorException("Service registration failed");
            }
            tempHttpService.setBasePath(basePath);
            serviceList.add(tempHttpService);
        }
        return serviceList;
    }

    private static void processResources(HttpService httpService) {
        List<HttpResource> httpResources = new ArrayList<>();
        List<HttpResource> upgradeToWebSocketResources = new ArrayList<>();
        for (AttachedFunction resource : httpService.getBalService().getType().getAttachedFunctions()) {
            if (!Flags.isFlagOn(resource.flags, Flags.RESOURCE)) {
                continue;
            }
            MapValue resourceConfigAnnotation = HttpResource.getResourceConfigAnnotation(resource);
            if (websocketUpgradeResource(resourceConfigAnnotation)) {
                HttpResource upgradeResource = HttpResource.buildHttpResource(resource, httpService);
                upgradeToWebSocketResources.add(upgradeResource);
            } else {
                HttpResource httpResource = HttpResource.buildHttpResource(resource, httpService);
                try {
                    httpService.getUriTemplate().parse(httpResource.getPath(), httpResource,
                                                       new HttpResourceElementFactory());
                } catch (URITemplateException | UnsupportedEncodingException e) {
                    throw new BallerinaConnectorException(e.getMessage());
                }
                httpResources.add(httpResource);
            }
        }
        httpService.setResources(httpResources);
        httpService.setUpgradeToWebSocketResources(upgradeToWebSocketResources);
    }

    private static boolean websocketUpgradeResource(MapValue resourceConfigAnnotation) {
        return checkConfigAnnotationAvailability(resourceConfigAnnotation)
                && resourceConfigAnnotation.getMapValue(HttpConstants.ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE) != null;
    }

    private static void prepareBasePathList(MapValue versioningConfig, String basePath, List<String> basePathList,
                                            String packageVersion) {
        String patternAnnotValue = HttpConstants.DEFAULT_VERSION;
        Boolean allowNoVersionAnnotValue = false;
        Boolean matchMajorVersionAnnotValue = false;
        if (versioningConfig != null) {
            patternAnnotValue = versioningConfig.getStringValue(HttpConstants.ANN_CONFIG_ATTR_PATTERN).getValue();
            allowNoVersionAnnotValue = versioningConfig.getBooleanValue(HttpConstants.ANN_CONFIG_ATTR_ALLOW_NO_VERSION);
            matchMajorVersionAnnotValue = versioningConfig.getBooleanValue(
                    HttpConstants.ANN_CONFIG_ATTR_MATCH_MAJOR_VERSION);
        }
        patternAnnotValue = patternAnnotValue.toLowerCase();
        basePathList.add(replaceServiceVersion(basePath, packageVersion, patternAnnotValue));

        if (allowNoVersionAnnotValue) {
            basePathList.add(basePath.replace(HttpConstants.VERSION, "").replace("//", "/"));
        }
        if (matchMajorVersionAnnotValue) {
            String patternWithMajor = patternAnnotValue.replace(HttpConstants.MINOR_VERSION, "");
            patternWithMajor = patternWithMajor.endsWith(".") ?
                    patternWithMajor.substring(0, patternWithMajor.length() - 1) : patternWithMajor;
            basePathList.add(replaceServiceVersion(basePath, packageVersion, patternWithMajor));
        }
    }

    private static String replaceServiceVersion(String basePath, String version, String pattern) {
        pattern = pattern.toLowerCase();
        String[] versionElements = version.split("\\.");
        String majorVersion = versionElements[0];
        String minorVersion = versionElements.length > 1 ? versionElements[1] : "";

        if (pattern.contains(HttpConstants.MAJOR_VERSION) || pattern.contains(HttpConstants.MINOR_VERSION)) {
            String patternReplaced = pattern.replace(HttpConstants.MAJOR_VERSION, majorVersion);
            String result = patternReplaced.replace(HttpConstants.MINOR_VERSION, minorVersion);

            return basePath.replace(HttpConstants.VERSION, result);
        }
        throw new BallerinaConnectorException("Invalid versioning pattern: expect \"" + HttpConstants.MAJOR_VERSION +
                                              "," + HttpConstants.MINOR_VERSION + "\" elements");
    }

    private static MapValue getHttpServiceConfigAnnotation(ObjectValue service) {
        return getServiceConfigAnnotation(service, PROTOCOL_PACKAGE_HTTP, HttpConstants.ANN_NAME_HTTP_SERVICE_CONFIG);
    }

    protected static MapValue getServiceConfigAnnotation(ObjectValue service, String packagePath,
                                                         String annotationName) {
        return (MapValue) service.getType().getAnnotation(packagePath.replaceAll(HttpConstants.REGEX,
                HttpConstants.SINGLE_SLASH), annotationName);
    }

    private static boolean hasInterruptibleAnnotation(ObjectValue service) {
        return service.getType().getAnnotation(PACKAGE_BALLERINA_BUILTIN, ANN_NAME_INTERRUPTIBLE) != null;
    }

    private String urlDecode(String basePath) {
        try {
            basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
        return basePath;
    }
}

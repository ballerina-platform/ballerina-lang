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

package org.ballerinalang.test.net.websocket.service;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test WebSocketService.
 */
public class WebSocketServiceTest {
    @Test(description = "Default WebSocket configurations")
    public void testDefault() {
        Service service = mock(Service.class);
        String serviceName = "riyafaService";
        when(service.getName()).thenReturn(serviceName);
        when(service.getResources()).thenReturn(new Resource[]{});
        WebSocketService webSocketService = new WebSocketService(service);
        String path = webSocketService.getBasePath();
        Assert.assertEquals(path, "/" + serviceName, "Incorrect path name");
        Assert.assertEquals(webSocketService.getIdleTimeoutInSeconds(), 0, "Incorrect idle timeout");
        Assert.assertEquals(webSocketService.getMaxFrameSize(), 65536, "Incorrect maxFrameSize");
        Assert.assertNull(webSocketService.getNegotiableSubProtocols());
    }

    @Test(description = "Given WebSocketServiceConfig struct")
    public void testGiven() {
        Service service = mock(Service.class);
        Annotation annotation = mock(Annotation.class);
        Struct struct = mock(Struct.class);
        Value value = mock(Value.class);
        String protocol = "xml";
        when(value.getStringValue()).thenReturn(protocol);
        when(struct.getArrayField(WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS)).thenReturn(new Value[]{value});
        when(struct.getIntField(WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT)).thenReturn(60L);
        when(struct.getIntField(WebSocketConstants.ANNOTATION_ATTR_MAX_FRAME_SIZE)).thenReturn(100000L);
        String pathName = "/path";
        when(struct.getStringField(WebSocketConstants.ANNOTATION_ATTR_PATH)).thenReturn(pathName);
        when(annotation.getValue()).thenReturn(struct);
        List<Annotation> annotationList = new ArrayList<>();
        annotationList.add(annotation);
        when(service.getAnnotationList(HttpConstants.PROTOCOL_PACKAGE_HTTP,
                                       WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION)).thenReturn(
                annotationList);

        when(service.getResources()).thenReturn(new Resource[]{});
        WebSocketService webSocketService = new WebSocketService(service);
        String path = webSocketService.getBasePath();
        Assert.assertEquals(path, pathName, "Incorrect path name");
        Assert.assertEquals(webSocketService.getIdleTimeoutInSeconds(), 60, "Incorrect idle timeout");
        Assert.assertEquals(webSocketService.getMaxFrameSize(), 100000, "Incorrect maxFrameSize");
        Assert.assertEquals(webSocketService.getNegotiableSubProtocols(), new String[]{protocol});
    }
}

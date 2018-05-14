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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_HUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;

/**
 * Native function to retrieve annotations specified for the WebSub subscriber service.
 *
 * @since 0.970.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "retrieveIntendedTopic",
        returnType = {@ReturnType(type = TypeKind.STRING)}
)
public class RetrieveIntendedTopic extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        Service webSubService = BLangConnectorSPIUtil.getService(context.getProgramFile(),
                                                                 context.getServiceInfo().getType());
        Struct annotationStruct = webSubService.getAnnotationList(
                                        WEBSUB_PACKAGE, ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG).get(0).getValue();

        if (!annotationStruct.getStringField(ANN_WEBSUB_ATTR_HUB).isEmpty()
                && !annotationStruct.getStringField(ANN_WEBSUB_ATTR_TOPIC).isEmpty()) {
            context.setReturnValues(new BString(annotationStruct.getStringField(ANN_WEBSUB_ATTR_TOPIC)));
        } else {
            //TODO:Fix to retrieve topic discovered for resource URL - need to check here if the resource URL is
            //specified, and if specified, need to identify and return the topic set for the WebSubHtpService
            context.setReturnValues(new BString(""));
        }
    }

}

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

package org.ballerinalang.net.jms;

import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.jms.Message;

/**
 * Dispatcher that handles the resources of a JMS Service.
 */
public class JMSDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JMSDispatcher.class);

    public static BValue[] getSignatureParameters(Resource resource, Message jmsCarbonMessage) {
        BStruct message = ConnectorUtils.createStruct(resource, Constants.PROTOCOL_PACKAGE_JMS, Constants.JMS_MESSAGE);
        message.addNativeData(Constants.JMS_API_MESSAGE, jmsCarbonMessage);

        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = message;

        return bValues;
    }

}

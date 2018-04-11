/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.MessageDataSource;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.Constants.XML_SUFFIX;

/**
 * Get the entity body in xml form.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getXml",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity", structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.XML), @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class GetXml extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BXML result;
        try {
            BStruct entityStruct = (BStruct) context.getRefArgument(FIRST_PARAMETER_INDEX);
            String baseType = HeaderUtil.getBaseType(entityStruct);
            if (baseType != null && baseType.toLowerCase().endsWith(XML_SUFFIX)) {
                MessageDataSource dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
                if (dataSource != null) {
                    if (dataSource instanceof BXML) {
                        result = (BXML) dataSource;
                    } else {
                        // else, build the XML from the string representation of the payload.
                        result = XMLUtils.parse(dataSource.getMessageAsString());
                    }
                } else {
                    result = EntityBodyHandler.constructXmlDataSource(entityStruct);
                    EntityBodyHandler.addMessageDataSource(entityStruct, result);
                    //Set byte channel to null, once the message data source has been constructed
                    entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                }
                context.setReturnValues(result);
            } else {
                context.setReturnValues(MimeUtil.createEntityError(context, "Entity body is not xml " +
                        "compatible since the received content-type is : " + baseType));
            }
        } catch (Throwable e) {
            context.setReturnValues(MimeUtil.createEntityError(context,
                    "Error occurred while retrieving xml data from entity : " + e.getMessage()));
        }
    }
}

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
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;

/**
 * Get the entity body as a string. Content-Type will not be checked with this method.
 *
 * @since 0.967.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getBodyAsString",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity", structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.STRING), @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class GetBodyAsString extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BString result;
        try {
            BStruct entityStruct = (BStruct) context.getRefArgument(FIRST_PARAMETER_INDEX);
            MessageDataSource dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (dataSource != null) {
                result = new BString(dataSource.getMessageAsString());
            } else {
                StringDataSource stringDataSource = EntityBodyHandler.constructStringDataSource(entityStruct);
                result = new BString(stringDataSource.getMessageAsString());
                EntityBodyHandler.addMessageDataSource(entityStruct, stringDataSource);
                //Set byte channel to null, once the message data source has been constructed
                entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
            }
            context.setReturnValues(result);

        } catch (Throwable e) {
            context.setReturnValues(MimeUtil.createEntityError(context,
                    "Error occurred while retrieving text data from entity : " + e.getMessage()));
        }
    }
}

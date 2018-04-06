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
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;

/**
 * Extract body parts from a given entity.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getBodyParts",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity", structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY), @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class GetBodyParts extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BRefValueArray partsArray;
        try {
            BStruct entityStruct = (BStruct) context.getRefArgument(FIRST_PARAMETER_INDEX);
            //Get the body parts from entity's multipart data field, if they've been already been decoded
            partsArray = EntityBodyHandler.getBodyPartArray(entityStruct);
            if (partsArray == null || partsArray.size() < 1) {
                Channel byteChannel = EntityBodyHandler.getByteChannel(entityStruct);
                if (byteChannel != null) {
                    EntityBodyHandler.decodeEntityBody(context, entityStruct, byteChannel);
                    //Check the body part availability for the second time, since the parts will be by this
                    // time populated from bytechannel
                    partsArray = EntityBodyHandler.getBodyPartArray(entityStruct);
                    //Set byte channel that belongs to parent entity to null, once the message body parts have
                    // been decoded
                    entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
                }
            }
            context.setReturnValues(partsArray);
        } catch (Throwable e) {
            context.setReturnValues(MimeUtil.createEntityError(context,
                    "Error occurred while extracting body parts from entity: " + e.getMessage()));
        }
    }
}

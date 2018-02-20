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
import org.ballerinalang.mime.util.EntityBody;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;

/**
 * Extract body parts from a given entity.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        packageName = "ballerina.mime",
        functionName = "getBodyParts",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Entity", structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class GetBodyParts extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BRefValueArray partsArray;
        try {
            BStruct entityStruct = (BStruct) this.getRefArgument(context, FIRST_PARAMETER_INDEX);
            //Get the body parts from entity's multipart data field, if they have already been decoded
            partsArray = getBodyPartArray(entityStruct);
            if (partsArray == null || partsArray.size() < 1) {
                EntityBody entityBody = MimeUtil.constructEntityBody(entityStruct);
                if (entityBody != null) {
                    String contentType = MimeUtil.getContentTypeWithParameters(entityStruct);
                    if (MimeUtil.isNotNullAndEmpty(contentType) && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
                        //Populate nested parts and set them to parent entity's multipart data field
                        if (entityBody.isStream()) {
                            MultipartDecoder.parseBody(context, entityStruct, contentType,
                                    entityBody.getEntityWrapper().getInputStream());
                        } else {
                            FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
                            MultipartDecoder.parseBody(context, entityStruct, contentType,
                                    fileIOChannel.getInputStream());
                        }
                    }
                }
                //Check the body part availability for the second time, since the parts will be by this time populated
                // from bytechannel
                partsArray = getBodyPartArray(entityStruct);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error occurred while extracting body parts from entity: " + e.getMessage());
        }
        return this.getBValues(partsArray);
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param entityStruct Represent a ballerina entity
     * @return An array of body parts
     */
    private BRefValueArray getBodyPartArray(BStruct entityStruct) {
        return entityStruct.getRefField(MULTIPART_DATA_INDEX) != null ?
                (BRefValueArray) entityStruct.getRefField(MULTIPART_DATA_INDEX) : null;
    }
}

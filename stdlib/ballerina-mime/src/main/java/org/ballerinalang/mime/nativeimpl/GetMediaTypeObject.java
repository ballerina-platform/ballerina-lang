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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.Constants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.Constants.STRING_INDEX;

/**
 * Construct MediaType struct from Content-Type string.
 *
 * @since 0.96
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
        functionName = "getMediaTypeObject",
        args = {@Argument(name = "contentType",
                type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT)},
        isPublic = true)
public class GetMediaTypeObject extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        try {
            String contentType = context.getStringArgument(STRING_INDEX);
            BStruct mediaType = ConnectorUtils.createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_MIME,
                            Constants.MEDIA_TYPE);
            mediaType = MimeUtil.parseMediaType(mediaType, contentType);
            context.setReturnValues(mediaType);
        } catch (Throwable e) {
            context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
        }
    }
}

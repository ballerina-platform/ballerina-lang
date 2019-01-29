/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.mime.nativeimpl.contentdisposition;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.MimeConstants.STRING_INDEX;

/**
 * Construct ContentDisposition object given a content-disposition value.
 *
 * @since  0.970.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
        functionName = "getContentDispositionObject",
        args = {@Argument(name = "contentDisposition",
                type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true)
public class GetContentDispositionObject extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        String contentDisposition = context.getStringArgument(STRING_INDEX);
        BMap<String, BValue> contentDispositionObj = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME,
                                                                                       CONTENT_DISPOSITION_STRUCT);
        MimeUtil.populateContentDispositionObject(contentDispositionObj, contentDisposition);
        context.setReturnValues(contentDispositionObj);
    }
}

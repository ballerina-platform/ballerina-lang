/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.ballerinalang.nativeimpl.builtin.jsonlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.BuiltInUtils;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Converts a JSON to the corresponding XML representation.
 *
 * @since 0.90
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "json.toXML",
        args = {@Argument(name = "j", type = TypeKind.JSON),
                @Argument(name = "options", type = TypeKind.RECORD, structType = "Options",
                          structPackage = BALLERINA_BUILTIN_PKG)},
        returnType = { @ReturnType(type = TypeKind.XML), @ReturnType(type = TypeKind.RECORD) },
        isPublic = true
)
public class ToXML extends BlockingNativeCallableUnit {

    private static final String XML_OPTIONS_ATTRIBUTE_PREFIX = "attributePrefix";
    private static final String XML_OPTIONS_ARRAY_ENTRY_TAG = "arrayEntryTag";

    @Override
    public void execute(Context ctx) {
        BXML<?> xml;
        BError error;
        try {
            // Accessing Parameters
            BValue json = ctx.getNullableRefArgument(0);
            if (json == null) {
                error = BuiltInUtils.createConversionError(ctx, "cannot convert null json to xml");
                ctx.setReturnValues(error);
                return;
            }

            BMap<String, BValue> optionsStruct = ((BMap<String, BValue>) ctx.getRefArgument(1));
            String attributePrefix = optionsStruct.get(XML_OPTIONS_ATTRIBUTE_PREFIX).stringValue();
            String arrayEntryTag = optionsStruct.get(XML_OPTIONS_ARRAY_ENTRY_TAG).stringValue();
            //Converting to xml.
            xml = JSONUtils.convertToXML(json, attributePrefix, arrayEntryTag);
            ctx.setReturnValues(xml);
        } catch (Exception e) {
            error = BuiltInUtils.createConversionError(ctx, "failed to convert json to xml: " + e.getMessage());
            ctx.setReturnValues(error);
        }
    }
}

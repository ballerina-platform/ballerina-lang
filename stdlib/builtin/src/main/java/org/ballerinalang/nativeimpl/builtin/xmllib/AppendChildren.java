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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.nativeimpl.builtin.xmllib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Append children to an XML if its an element type XML. Error otherwise.
 * New children will be appended at the end of the existing children.
 * 
 * @since 0.982.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "xml.appendChildren",
        args = {@Argument(name = "children", type = TypeKind.XML)},
        isPublic = true
)
public class AppendChildren extends BlockingNativeCallableUnit {

    private static final String OPERATION = "add children to xml element";

    @Override
    public void execute(Context ctx) {
        try {
            BXML<?> xml = (BXML<?>) ctx.getRefArgument(0);
            BXML<?> children = (BXML<?>) ctx.getRefArgument(1);
            xml.addChildren(children);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }

        // Setting output value.
        ctx.setReturnValues();
    }
}

/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.nativeimpl.lang.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;

/**
 * Native function ballerina.lang.xml:toString.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xml",
        functionName = "toString",
        args = {@Argument(name = "x", type = TypeEnum.XML)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class ToString extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(ToString.class);

    @Override
    public BValue[] execute(Context ctx) {
        String xmlStr = null;
        try {
            // Accessing Parameters.
            BXML xml = (BXML) getArgument(ctx, 0);
            xmlStr = xml.stringValue();
            if (log.isDebugEnabled()) {
                log.debug("XML toString: " + xmlStr);
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("convert xml to string", e);
        }
        // Setting output value.
        return getBValues(new BString(xmlStr));
    }
}

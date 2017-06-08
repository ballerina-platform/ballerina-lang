/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.lang.xmls;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set a value of an attribute in the xml
 * 
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "setAttribute",
        args = {@Argument(name = "x", type = TypeEnum.XML),
                @Argument(name = "qname", type = TypeEnum.STRING),
                @Argument(name = "prefix", type = TypeEnum.STRING),
                @Argument(name = "value", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Set a value of an attribute in the xml") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "An XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "qname",
        value = "Qualified name of the attribute") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "prefix",
        value = "Namespace prefix of the attribute") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "value",
        value = "Value of the attribute") })
public class SetAttribute extends AbstractNativeFunction {

    private static final String OPERATION = "set attribute to xml";
    private static Pattern qnamePattern = Pattern.compile("(\\{.*\\})(.*)");
    private static Pattern namespacePattern = Pattern.compile("^\\{.*\\}");
    
    @Override
    public BValue[] execute(Context ctx) {
        try {
            // Accessing Parameters.
            BXML xml = (BXML) getArgument(ctx, 0);
            BString qname = (BString) getArgument(ctx, 1);
            BString prefix = (BString) getArgument(ctx, 2);
            BString value = (BString) getArgument(ctx, 3);
            
            String namespace;
            String localName;
            
            // TODO: remove regex matching once qname type is implemented in ballerina
            if (qname.stringValue().matches(namespacePattern.pattern())) {
                namespace = qname.stringValue();
                localName = "";
            } else if (qname.stringValue().matches(qnamePattern.pattern())) {
                Matcher matcher = qnamePattern.matcher(qname.stringValue());
                matcher.find();
                namespace = matcher.group(1);
                namespace = namespace.substring(1, namespace.length() - 1);
                localName = matcher.group(2);
            } else {
                namespace = "";
                localName = qname.stringValue();
            }
            
            xml.setAttribute(namespace, prefix.stringValue(), localName, value.stringValue());
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }

        // Setting output value.
        return VOID_RETURN;
    }
}

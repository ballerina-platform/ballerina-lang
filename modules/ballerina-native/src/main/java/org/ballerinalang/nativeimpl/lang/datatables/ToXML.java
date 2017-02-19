/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.lang.datatables;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function to get the XML representation of the datatable.
 * ballerina.model.datatables:toXml(datatable, string, string)
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.datatables",
        functionName = "toXml",
        args = {@Argument(name = "dt", type = TypeEnum.DATATABLE),
                @Argument(name = "rootWrapper", type = TypeEnum.STRING),
                @Argument(name = "rowWrapper", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Outputs the dataset in XML format as a stream. This function will add 'results'"
                + " and 'result' if the root wrapper and row wrapper elements are not provided. ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "dt",
        value = "The datatable object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "rootWrapper",
        value = "The root wrapper element") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "rowWrapper",
        value = "The row wrapper element") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "xml",
        value = "The resulting dataset in XML format with given root wrapper and row wrapper."
                + " The default will be results and result if the wrapper isn't provided. ") })
public class ToXML extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BDataTable dataTable = (BDataTable) getArgument(ctx, 0);
        String rootWrapper = null;
        String rowWrapper = null;
        if (getArgument(ctx, 1) != null) {
            rootWrapper = getArgument(ctx, 1).stringValue();
        }
        if (getArgument(ctx, 2) != null) {
            rowWrapper = getArgument(ctx, 2).stringValue();
        }
        return getBValues(dataTable.toXML(rootWrapper, rowWrapper));
    }
}

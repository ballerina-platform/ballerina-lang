/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.core;

import org.ballerinalang.testerina.core.entity.TesterinaAnnotation;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Process annotations added to a test function.
 *
 * @since 0.963.0
 */
public class AnnotationProcessor {

    private static final String CONFIG_ANNOTATION_NAME = "config";
    private static final String DEFAULT_TEST_GROUP_NAME = "default";

    /**
     * Takes @{@link FunctionInfo} as a input and process annotations attached to the function.
     *
     * @param functionInfo ballerina FunctionInfo object
     * @return @{@link TesterinaAnnotation} object containing annotation information
     */
    public static TesterinaAnnotation processAnnotations(FunctionInfo functionInfo) {
        TesterinaAnnotation testerinaAnnotation = new TesterinaAnnotation();

        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) functionInfo
                .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

        // If no annotations present set default group
        if (attributeInfo.getAttachmentInfoEntries().length == 0) {
            testerinaAnnotation.setGroups(Arrays.asList(DEFAULT_TEST_GROUP_NAME));
        } else {
            for (AnnAttachmentInfo attachmentInfo : attributeInfo.getAttachmentInfoEntries()) {
                if (!attachmentInfo.getName().equals(CONFIG_ANNOTATION_NAME)) {
                    continue;
                }
                // Check if disabled property is present in the annotation
                if (attachmentInfo
                            .getAttributeValue(TesterinaAnnotation.ConfigAnnotationProps.TEST_DISABLED.getName())
                    != null) {
                    testerinaAnnotation.setDisabled(attachmentInfo
                            .getAttributeValue(TesterinaAnnotation.ConfigAnnotationProps.TEST_DISABLED.getName())
                            .getBooleanValue());
                }
                // check if groups are present in the annotation
                if (attachmentInfo.getAttributeValue(TesterinaAnnotation.ConfigAnnotationProps.TEST_GROUP.getName())
                    != null) {
                    testerinaAnnotation.setGroups(Arrays.stream(attachmentInfo
                            .getAttributeValue(TesterinaAnnotation.ConfigAnnotationProps.TEST_GROUP.getName())
                            .getAttributeValueArray()).map(f -> f.getStringValue()).collect(Collectors.toList()));
                } else {
                    // If groups are not present add it to a default group
                    testerinaAnnotation.setGroups(Arrays.asList(DEFAULT_TEST_GROUP_NAME));
                }
                if (attachmentInfo
                            .getAttributeValue(TesterinaAnnotation.ConfigAnnotationProps.TEST_VALUE_SET.getName())
                    != null) {
                    testerinaAnnotation.setValueSet(Arrays.stream(attachmentInfo
                            .getAttributeValue(TesterinaAnnotation.ConfigAnnotationProps.TEST_VALUE_SET.getName())
                            .getAttributeValueArray()).map(f -> f.getStringValue().split(","))
                            .collect(Collectors.toList()));
                }
                break;
            }
        }
        return testerinaAnnotation;
    }
}

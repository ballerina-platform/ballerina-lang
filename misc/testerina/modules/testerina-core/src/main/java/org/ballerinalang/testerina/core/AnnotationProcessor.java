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
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Process annotations added to a test function.
 *
 * @since 0.963.0
 */
public class AnnotationProcessor {

    private static final String CONFIG_ANNOTATION_NAME = "config";
    private static final String DEFAULT_TEST_GROUP_NAME = "default";

    private static final String GROUP_ANNOTATION_NAME = "groups";
    private static final String VALUE_SET_ANNOTATION_NAME = "valueSets";
    private static final String TEST_DISABLE_ANNOTATION_NAME = "disabled";

    /**
     * Takes @{@link FunctionInfo} as a input and process annotations attached to the function.
     * Processor only returns the tFunctions that should only be executed.
     * All the test function that are excluded due to disabling or group filtering will not be returned.
     *
     * @param programFile  Ballerina program file
     * @param functionInfo ballerina FunctionInfo object
     * @return @{@link TesterinaAnnotation} object containing annotation information
     */
    public static TesterinaFunction processAnnotations(ProgramFile programFile,
                                                       FunctionInfo functionInfo, List<String> groups,
                                                       boolean excludeGroups) {

        TesterinaFunction tFunction = new TesterinaFunction(programFile, functionInfo,
                TesterinaFunction.Type.TEST);
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) functionInfo
                .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

        if (attributeInfo.getAttachmentInfoEntries().length == 0) {
            if ((groups != null) && (isGroupAvailable(groups, Collections.singletonList(DEFAULT_TEST_GROUP_NAME))
                                     == excludeGroups)) {
                tFunction.setRunTest();
            }
        } else {
            for (AnnAttachmentInfo attachmentInfo : attributeInfo.getAttachmentInfoEntries()) {
                // Only reads the config annotation related to testerina
                if (!attachmentInfo.getName().equals(CONFIG_ANNOTATION_NAME)) {
                    continue;
                }
                // Check if disabled property is present in the annotation
                if (attachmentInfo
                            .getAttributeValue(TEST_DISABLE_ANNOTATION_NAME)
                    != null && attachmentInfo.getAttributeValue(TEST_DISABLE_ANNOTATION_NAME).getBooleanValue()) {
                    // If disable property is present disable the test, no further processing is needed
                    tFunction.setRunTest();
                    continue;
                }
                // Check whether user has provided a group list
                if (groups != null) {
                    // check if groups attribute is present in the annotation
                    if (attachmentInfo.getAttributeValue(GROUP_ANNOTATION_NAME) != null) {
                        // Check whether function is included in group filter
                        // against the user provided flag to include or exclude groups
                        if (isGroupAvailable(groups,
                                Arrays.stream(attachmentInfo.getAttributeValue(GROUP_ANNOTATION_NAME)
                                        .getAttributeValueArray()).map(AnnAttributeValue::getStringValue)
                                        .collect(Collectors.toList())) == excludeGroups) {
                            tFunction.setRunTest();
                        }
                        // If groups are not present this belongs to default group
                        // check whether user provided groups has default group
                    } else if (isGroupAvailable(groups, Arrays.asList(DEFAULT_TEST_GROUP_NAME)) == excludeGroups) {
                        tFunction.setRunTest();
                    }
                }
                // Check the availability of value sets
                if (attachmentInfo.getAttributeValue(VALUE_SET_ANNOTATION_NAME) != null) {
                    // extracts the value sets
                    tFunction.setValueSet(Arrays.stream(attachmentInfo
                            .getAttributeValue(VALUE_SET_ANNOTATION_NAME)
                            .getAttributeValueArray()).map(f -> f.getStringValue().split(","))
                            .collect(Collectors.toList()));
                }
                break;
            }
        }
        return tFunction;
    }

    /**
     * Check whether there is a common element in two Lists.
     *
     * @param inputGroups    String @{@link List} to match
     * @param functionGroups String @{@link List} to match agains
     * @return true if a match is found
     */
    private static boolean isGroupAvailable(List<String> inputGroups, List<String> functionGroups) {
        for (String group : inputGroups) {
            for (String funcGroup : functionGroups) {
                if (group.equals(funcGroup)) {
                    return true;
                }
            }
        }
        return false;
    }
}

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
 */

package org.ballerinalang.testerina.core;

import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by yasassri on 2/27/18.
 */
public class TAnnotProcessor {

    private static final String TEST_ANNOTATION_NAME = "config";
    private static final String BEFORE_SUITE_ANNOTATION_NAME = "beforeSuite";
    private static final String AFTER_SUITE_ANNOTATION_NAME = "beforeSuite";
    private static final String BEFORE_TEST_ANNOTATION_NAME = "before";
    private static final String AFTER_TEST_ANNOTATION_NAME = "after";
    private static final String BEFORE_EACH_ANNOTATION_NAME = "beforeEach";
    private static final String AFTER_EACH_ANNOTATION_NAME = "afterEach";

    public static void processAnnotations(ProgramFile programFile,
                                          PackageInfo packageInfo,
                                          Map<String, TestSuite> testSuites) {

        TestSuite suite = testSuites.get(packageInfo.getPkgPath());

        for (FunctionInfo functionInfo : packageInfo.getFunctionInfoEntries()) {

            AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) functionInfo
                    .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

            if (attributeInfo.getAttachmentInfoEntries().length == 0) {
                // NO Annotations present so do nothing.
                continue;
            } else {
                Test test = new Test();
                for ( AnnAttachmentInfo attachmentInfo : attributeInfo.getAttachmentInfoEntries()) {
                    if (attachmentInfo.getName().equals(BEFORE_SUITE_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addBeforeSuiteFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type.TEST));
                    }

                    if (attachmentInfo.getName().equals(AFTER_SUITE_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addAfterSuiteFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type.TEST));
                    }

                    if (attachmentInfo.getName().equals(AFTER_SUITE_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addAfterSuiteFunction(new TesterinaFunction(programFile, functionInfo, TesterinaFunction.Type.TEST));
                    }
                }
            }

        }

        List<TestSuite> suites = new ArrayList<>();

        if (attributeInfo.getAttachmentInfoEntries().length == 0) {
            // NO Annotations present so do nothing.
        } else {
            for (AnnAttachmentInfo attachmentInfo : attributeInfo.getAttachmentInfoEntries()) {
                // Only reads the config annotation related to testerina
                if (!attachmentInfo.getName().equals()) {
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
}

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
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;

/**
 * Created by yasassri on 2/27/18.
 */
public class TAnnotProcessor {

    private static final String TEST_ANNOTATION_NAME = "config";
    private static final String BEFORE_SUITE_ANNOTATION_NAME = "beforeSuite";
    private static final String AFTER_SUITE_ANNOTATION_NAME = "afterSuite";
    private static final String BEFORE_TEST_ANNOTATION_NAME = "before";
    private static final String AFTER_TEST_ANNOTATION_NAME = "after";
    private static final String BEFORE_EACH_ANNOTATION_NAME = "beforeEach";
    private static final String AFTER_EACH_ANNOTATION_NAME = "afterEach";

    public static void processAnnotations(ProgramFile programFile,
                                          PackageInfo packageInfo,
                                          TestSuite suite) {

        FunctionInfo functionInfo[] = packageInfo.getFunctionInfoEntries();
        for (int k = 0; k < functionInfo.length; k++) {

            AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) functionInfo[k]
                    .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

            if (attributeInfo.getAttachmentInfoEntries().length == 0) {
                // NO Annotations present so do nothing.
                continue;
            } else {
                for (AnnAttachmentInfo attachmentInfo : attributeInfo.getAttachmentInfoEntries()) {
                    if (attachmentInfo.getName().equals(BEFORE_SUITE_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addBeforeSuiteFunction(
                                new TesterinaFunction(programFile, functionInfo[k], TesterinaFunction.Type.TEST));
                    } else if (attachmentInfo.getName().equals(AFTER_SUITE_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addAfterSuiteFunction(
                                new TesterinaFunction(programFile, functionInfo[k], TesterinaFunction.Type.TEST));
                    } else if (attachmentInfo.getName().equals(BEFORE_EACH_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addBeforeEachFunction(
                                new TesterinaFunction(programFile, functionInfo[k], TesterinaFunction.Type.TEST));
                    } else if (attachmentInfo.getName().equals(AFTER_EACH_ANNOTATION_NAME)) {
                        // modify the type
                        suite.addAfterEachFunction(
                                new TesterinaFunction(programFile, functionInfo[k], TesterinaFunction.Type.TEST));
                    } else {
                        if (attachmentInfo.getName().equals(TEST_ANNOTATION_NAME)) {
                            Test test = new Test();
                            test.setTestFunctions(
                                    new TesterinaFunction(programFile, functionInfo[k], TesterinaFunction.Type.TEST));
                            int j = k - 1;
                            while (j >= 0) {
                                AnnotationAttributeInfo attributeInfo2 = (AnnotationAttributeInfo) functionInfo[j]
                                        .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

                                for (AnnAttachmentInfo attachmentInfo2 : attributeInfo2.getAttachmentInfoEntries()) {
                                    if (attachmentInfo2.getName().equals(BEFORE_TEST_ANNOTATION_NAME)) {
                                        test.addBeforeTestFunctions(new TesterinaFunction(programFile, functionInfo[j],
                                                TesterinaFunction.Type.TEST));
                                    } else {
                                        break;
                                    }
                                }
                                j--;
                            }

                            int l = k + 1;
                            while (l < functionInfo.length) {
                                AnnotationAttributeInfo attributeInfo2 = (AnnotationAttributeInfo) functionInfo[l]
                                        .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);

                                for (AnnAttachmentInfo attachmentInfo2 : attributeInfo2.getAttachmentInfoEntries()) {
                                    if (attachmentInfo2.getName().equals(AFTER_TEST_ANNOTATION_NAME)) {
                                        test.addAfterTestFunctions(new TesterinaFunction(programFile, functionInfo[l],
                                                TesterinaFunction.Type.TEST));
                                        k = l;
                                    } else {
                                        break;
                                    }
                                }
                                l--;
                            }
                            suite.addTests(test);
                        }
                    }
                }
            }
        }
    }
}

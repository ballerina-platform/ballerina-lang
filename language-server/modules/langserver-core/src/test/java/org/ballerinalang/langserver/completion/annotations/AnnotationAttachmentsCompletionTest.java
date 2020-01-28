/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completion.annotations;

import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

/**
 * Completion item tests for annotation attachments.
 */
public class AnnotationAttachmentsCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(AnnotationAttachmentsCompletionTest.class);
    
    @BeforeClass
    public void loadAnnotationCache() {
        LSAnnotationCache.initiate();
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for annotation attachments");
        return new Object[][] {
                {"annotationAccessExpression1.json", "annotation"},
                {"annotationAccessExpression2.json", "annotation"},
                {"annotationAccessExpression3.json", "annotation"},
                {"annotationAccessExpression4.json", "annotation"},
                {"annotationAccessExpression5.json", "annotation"},
                {"annotationAccessExpression6.json", "annotation"},
                {"annotationBodyCompletion1.json", "annotation"},
//                {"annotationBodyCompletion2.json", "annotation"},
                {"annotationBodyCompletion3.json", "annotation"},
                {"annotationBodyCompletion4.json", "annotation"},
//                {"annotationBodyCompletion5.json", "annotation"},
//                {"annotationBodyCompletion6.json", "annotation"},
                {"annotationInSameModule1.json", "annotation"},
                {"annotationInSameModule2.json", "annotation"},
                {"annotationInSameModule3.json", "annotation"},
                {"annotationInSameModule4.json", "annotation"},
                {"annotationInSameModule5.json", "annotation"},
                {"serviceAnnotation1.json", "annotation"},
                {"serviceAnnotation2.json", "annotation"},
                {"resourceAnnotation1.json", "annotation"},
                {"resourceAnnotation2.json", "annotation"},
                {"resourceAnnotation3.json", "annotation"},
                {"resourceAnnotation4.json", "annotation"},
                {"resourceAnnotation5.json", "annotation"},
                {"resourceAnnotation6.json", "annotation"},
                {"resourceAnnotation7.json", "annotation"},
                {"functionAnnotation1.json", "annotation"},
                {"listenerAnnotation1.json", "annotation"},
                {"externalAnnotation1.json", "annotation"},
                {"externalAnnotation2.json", "annotation"},
                {"workerAnnotation1.json", "annotation"},
        };
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services.dispatching;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;
import java.io.File;

/**
 * Test class for testing negative cases for path params.
 *
 * i.e: /foo/{abc}/bar and /foo/{xyz}/bar should give an error.
 */
public class UriTemplateDispatcherNegativeTest {

    private CompileResult compileResult;

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*Two resources have the same addressable URI, echo2 and echo1.*")
    public void testTwoResourcesWithSamePathWithPathParam() {
        compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/dispatching/uri-template-matching-negative-1.bal").getPath()).getAbsolutePath());
        BServiceUtil.runService(compileResult);
    }
}

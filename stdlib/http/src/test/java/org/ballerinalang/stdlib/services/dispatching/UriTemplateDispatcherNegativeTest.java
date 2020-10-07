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

package org.ballerinalang.stdlib.services.dispatching;

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for testing negative cases for path params.
 *
 * i.e: /foo/{abc}/bar and /foo/{xyz}/bar should give an error.
 */
public class UriTemplateDispatcherNegativeTest {

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*Two resources have the same addressable URI, echo1 and echo2.*")
    public void testTwoResourcesWithSamePathWithPathParam() {
        BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/dispatching/uri-template-matching-negative-1.bal").getPath()).getAbsolutePath());
    }
}

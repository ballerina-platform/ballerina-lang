/*
*  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.listener;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

/**
 * Test cases for access listener from another project.
 */
public class ListenerBalaTestExtPackage {

    @Test(description = "Test access listener in different package")
    public void testListenerObjectDefinedInDifferentPackages() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_listener");
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/listener/external_packaged_listener_access.bal");
        BRunUtil.invoke(compileResult, "getStartAndAttachCount");
    }

    @Test(description = "Test listeners defined only in a different package")
    public void testListenerObjectDefinedOnlyInADifferentPackage() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_listener_non_default");
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/listener/external_packaged_listener_access_non_default_module.bal");
        BRunUtil.invoke(compileResult, "main");
    }

}


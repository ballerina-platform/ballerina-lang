/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.psi.vars;

import org.ballerinalang.psi.BallerinaResolveTestBase;

/**
 * Test parameter resolving.
 */
public class BallerinaResolveParameterTest extends BallerinaResolveTestBase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/vars/parameter");
    }

    //    public void testParameterInAction() {
    //        doFileTest();
    //    }

    //    public void testParameterInConnector() {
    //        doFileTest();
    //    }

    public void testParameterInFunction() {
        doFileTest();
    }

    //    public void testParameterInService() {
    //        doFileTest();
    //    }
}

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

public class BallerinaResolveLocalVariableTest extends BallerinaResolveTestBase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/vars/local");
    }

    public void testLocalVariableInAction() {
        doFileTest();
    }

    public void testLocalVariableInConnector() {
        doFileTest();
    }

    public void testLocalVariableInFunction() {
        doFileTest();
    }

    public void testLocalVariableInResource() {
        doFileTest();
    }

    public void testLocalVariableInService() {
        doFileTest();
    }
}

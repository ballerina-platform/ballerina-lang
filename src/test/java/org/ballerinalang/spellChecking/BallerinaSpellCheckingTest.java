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

package org.ballerinalang.spellChecking;

import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;

public class BallerinaSpellCheckingTest extends BallerinaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("spellchecking");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(new SpellCheckingInspection());
    }

    public void testSample() throws Exception {
        doTest();
    }

    private void doTest() {
        myFixture.testHighlighting(false, false, true, getTestName(false) + ".bal");
    }
}

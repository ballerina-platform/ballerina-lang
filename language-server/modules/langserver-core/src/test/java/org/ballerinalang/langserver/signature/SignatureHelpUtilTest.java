/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.signature;

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test Case to verify the Signature help utility functions.
 */
public class SignatureHelpUtilTest {
    private static final ClassLoader CLASS_LOADER = SignatureHelpUtilTest.class.getClassLoader();
    @Test(description = "Test get callable unit name", dataProvider = "positionData")
    public void getCallableItemNameTest(Position position, String funcName) throws URISyntaxException, IOException {
        URI fileLocation = CLASS_LOADER.getResource("signature" + File.separator + "util"
                + File.separator + "testUtil.bal").toURI();
        String stringContent = new String(Files.readAllBytes(Paths.get(fileLocation)));
        LSServiceOperationContext serviceContext = new LSServiceOperationContext();
        SignatureHelpUtil.captureCallableItemInfo(position, stringContent, serviceContext);
        String capturedFunctionName = serviceContext.get(SignatureKeys.CALLABLE_ITEM_NAME);
        Assert.assertEquals(funcName, capturedFunctionName);
    }

    @DataProvider(name = "positionData")
    public Object[][] getPositions() {
        return new Object[][]{
                {new Position(2, 65), "testFunction"},
                {new Position(2, 60), "testFunction"},
                // TODO: Disable Temporarily
//                {new Position(2, 42), "test2"}
        };
    }
}

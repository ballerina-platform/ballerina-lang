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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class CreateVariableTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "create-variable";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"variableAssignmentRequiredCodeAction1.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction2.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction3.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction4.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction5.json", "createVariable2.bal"},
                {"variableAssignmentRequiredCodeAction6.json", "createVariable2.bal"},
                {"variableAssignmentRequiredCodeAction7.json", "createVariable2.bal"},
                {"variableAssignmentRequiredCodeAction8.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction9.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction10.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction11.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction12.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction13.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction14.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction15.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction16.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction17.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction18.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction19.json", "createVariable3.bal"},
                {"variableAssignmentRequiredCodeAction20.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction21.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction22.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction23.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction24.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction25.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction26.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction27.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction28.json", "createVariable4.bal"},
                {"variableAssignmentRequiredCodeAction29.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction30.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction31.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction32.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction33.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction34.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction35.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction36.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction37.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction38.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction39.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction40.json", "createVariable5.bal"},
                {"variableAssignmentRequiredCodeAction41.json", "createVariable6.bal"},
                {"variableAssignmentRequiredCodeAction42.json", "createVariable7.bal"},
                {"variableAssignmentRequiredCodeAction43.json", "createVariable7.bal"},
                {"ignoreReturnValueCodeAction.json", "createVariable.bal"},
                {"projectVariableAssignmentRequiredCodeAction1.json", "testproject/main.bal"},
                {"projectVariableAssignmentRequiredCodeAction2.json", "testproject/main.bal"},
                {"projectVariableAssignmentRequiredCodeAction3.json", "testproject/modules/module1/module1.bal"},
                {"createVariableInClassMethod.json", "createVariableInClassMethod.bal"},
                {"createVariableInServiceMethod.json", "createVariableInServiceMethod.bal"},
                {"createVariableInServiceRemoteMethod.json", "createVariableInServiceMethod.bal"},
                {"createVariableWithUnionType.json", "createVariableWithUnionType.bal"},
                {"createVariableWithIntersectionType.json", "createVariableWithIntersectionType.bal"},
                {"createVariableWithIntersectionType2.json", "createVariableWithIntersectionType.bal"},
                {"createVariableForOptionalFieldAccess1.json", "createVariableForOptionalFieldAccess1.bal"},
                {"createVariableForOptionalFieldAccess2.json", "createVariableForOptionalFieldAccess2.bal"},
                {"createVariableWithTypeDesc.json", "createVariableWithTypeDesc.bal"},

                // Tuple related
                {"createVariableWithTuple1.json", "createVariableWithTuple1.bal"},

                // Create variables of function/invocable type
                {"createVariableWithFunctionType1.json", "createVariableWithFunctionType1.bal"},
                {"createVariableWithFunctionType2.json", "createVariableWithFunctionType1.bal"},
                
                {"createVariableWithFunctionCall1.json", "createVariableWithFunctionCall1.bal"},
                {"createVariableWithFunctionCall2.json", "createVariableWithFunctionCall2.bal"},
        };
    }
}

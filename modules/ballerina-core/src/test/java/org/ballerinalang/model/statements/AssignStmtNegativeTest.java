/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Test class for assignment statement.
 */
public class AssignStmtNegativeTest {

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "incompatible-type-assignment.bal:6: incompatible types: 'int' " +
                    "cannot be converted to 'boolean'")
    public void testIncompatibleTypeAssignment() {
        BTestUtils.parseBalFile("lang/statements/assignment/incompatible-type-assignment.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "repeated-var-multivalue-1.bal:6: 'a' is repeated on the " +
                    "left side of assignment")
    public void testRepeatedVariableInAssignment1() {
        BTestUtils.parseBalFile("lang/statements/assignment/repeated-var-multivalue-1.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "repeated-var-multivalue-2.bal:6: 'name' is repeated on the " +
                    "left side of assignment")
    public void testRepeatedVariableInAssignment2() {
        BTestUtils.parseBalFile("lang/statements/assignment/repeated-var-multivalue-2.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "const-assignment.bal:8: cannot assign a value to constant 'i'")
    public void testConstAssignment() {
        BTestUtils.parseBalFile("lang/statements/assignment/const-assignment.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "assign-count-mismatch-1.bal:6: assignment count mismatch: 2 != 3")
    public void testAssignCountMismatch1() {
        BTestUtils.parseBalFile("lang/statements/assignment/assign-count-mismatch-1.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "assign-count-mismatch-2.bal:7: assignment count mismatch: 4 != 3")
    public void testAssignCountMismatch2() {
        BTestUtils.parseBalFile("lang/statements/assignment/assign-count-mismatch-2.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "assign-types-mismatch-1.bal:6: cannot assign string to " +
                    "'a' \\(type int\\) in multiple assignment")
    public void testAssignTypeMismatch1() {
        BTestUtils.parseBalFile("lang/statements/assignment/assign-types-mismatch-1.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "assign-types-mismatch-2.bal:6: cannot assign string to " +
                    "'name' \\(type int\\) in multiple assignment")
    public void testAssignTypeMismatch2() {
        BTestUtils.parseBalFile("lang/statements/assignment/assign-types-mismatch-2.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "backtick-assign-mismatch-1.bal:6: incompatible types: " +
                    "expected json or xml")
    public void testTemplateJSONAssignTypeMismatch() {
        BTestUtils.parseBalFile("lang/statements/assignment/backtick-assign-mismatch-1.bal");
    }
}

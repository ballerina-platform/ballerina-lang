/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.test.unit;

import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticKind;
import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.test.unit.base.TestReporter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class to test {@link DiagnosticReporter}.
 *
 * @since 2.0.0
 */
public class DiagnosticReporterTest {
    @Test
    public void testConstruction() {
        TestReporter reporter = new TestReporter();
        Assert.assertFalse(reporter.hasErrors());
        Assert.assertEquals(reporter.diagnostics().size(), 0);
    }

    @Test
    public void testAddAndReset() {
        TestReporter reporter = new TestReporter();
        Diagnostic diagnostic = Diagnostic.debug("Debug");
        reporter.addDiagnostic(diagnostic);
        Assert.assertEquals(reporter.diagnostics().size(), 1);
        Assert.assertEquals(reporter.diagnostics(), List.of(diagnostic));
        reporter.resetDiagnostics();
        Assert.assertEquals(reporter.diagnostics().size(), 0);
    }

    @Test
    public void testAddSeveral() {
        TestReporter reporter = new TestReporter();
        Diagnostic diagnostic1 = Diagnostic.debug("Debug");
        Diagnostic diagnostic2 = Diagnostic.debug("Debug");
        reporter.addAllDiagnostics(List.of(diagnostic1, diagnostic2));
        Assert.assertEquals(reporter.diagnostics().size(), 2);
    }

    @Test
    public void testErrorFlag() {
        TestReporter reporter = new TestReporter();
        reporter.addDiagnostic(Diagnostic.debug("Debug"));
        Assert.assertFalse(reporter.hasErrors());
        reporter.addDiagnostic(Diagnostic.error("Error"));
        Assert.assertTrue(reporter.hasErrors());
        reporter.resetDiagnostics();
        Assert.assertFalse(reporter.hasErrors());
    }

    @Test
    public void testDiagnosticTypes() {
        TestReporter reporter = new TestReporter();
        reporter.addDiagnostic(Diagnostic.debug("Debug"));
        reporter.addDiagnostic(Diagnostic.error("Error"));
        reporter.addDiagnostic(Diagnostic.warn("Warn"));
        Assert.assertTrue(reporter.hasErrors());
        List<Diagnostic> diagnostics = new ArrayList<>(reporter.diagnostics());
        Assert.assertEquals(diagnostics.size(), 3);
        Assert.assertEquals(diagnostics.get(0).getKind(), DiagnosticKind.DEBUG);
        Assert.assertEquals(diagnostics.get(1).getKind(), DiagnosticKind.ERROR);
        Assert.assertEquals(diagnostics.get(2).getKind(), DiagnosticKind.WARN);
    }
}

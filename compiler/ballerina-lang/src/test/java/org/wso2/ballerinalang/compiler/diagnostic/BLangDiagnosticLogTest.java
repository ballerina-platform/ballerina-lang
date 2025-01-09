/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;

/**
 * Tests for {@link @BLangDiagnosticLog} class.
 *
 * @since 2.0
 */
public class BLangDiagnosticLogTest {
    private CompilerContext context;
    private DiagnosticLog dlog;

    @BeforeClass
    public void setup() {
        context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(CompilerOptionName.PROJECT_API_INITIATED_COMPILATION, String.valueOf(true));
        dlog = BLangDiagnosticLog.getInstance(context);
    }

    @Test
    public void testLogDiagnosticWithModuleDescriptor() {
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        PackageID packageID = createPackageID("org.diagnostic.log", ".", "1.0.0");

        PackageCache packageCache = PackageCache.getInstance(context);
        packageCache.put(packageID, pkgNode);

        ModuleDescriptor moduleDescriptor = createModuleDescriptor(packageID.orgName,
                packageID.name, packageID.version);

        Location location = new BLangDiagnosticLocation("test.bal", 1, 1, 1, 1);
        dlog.logDiagnostic(DiagnosticSeverity.ERROR, moduleDescriptor, location, "Diagnostic Error Message");
        List<Diagnostic> diagnosticList = pkgNode.getDiagnostics();
        Location expLocation = new BLangDiagnosticLocation("test.bal", 0, 0, 0, 0);
        assertDiagnosticEqual(diagnosticList.get(0), "Diagnostic Error Message", DiagnosticSeverity.ERROR, expLocation);
    }

    @Test
    public void testLogDiagnosticWithPackageID() {
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        PackageID packageID = createPackageID("org.diagnostic.log", ".", "1.0.0");

        PackageCache packageCache = PackageCache.getInstance(context);
        packageCache.put(packageID, pkgNode);

        Location location = new BLangDiagnosticLocation("test.bal", 1, 1, 1, 1);
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(
                new DiagnosticInfo(null, "Diagnostic Message", DiagnosticSeverity.WARNING), location);
        ((BLangDiagnosticLog) dlog).logDiagnostic(packageID, diagnostic);

        List<Diagnostic> diagnosticList = pkgNode.getDiagnostics();
        assertDiagnosticEqual(diagnosticList.get(0), "Diagnostic Message", DiagnosticSeverity.WARNING, location);
    }

    @Test
    public void testDiagnosticHashCollusion() {
        BLangDiagnosticLog dlog = (BLangDiagnosticLog) this.dlog;
    }

    // helpers
    private PackageID createPackageID(String orgName, String name, String version) {
        return new PackageID(new Name(orgName), new Name(name), new Name(version));
    }

    private ModuleDescriptor createModuleDescriptor(Name pkgOrgName,
                                                    Name pkgName,
                                                    Name pkgVersion) {
        PackageOrg packageOrg = PackageOrg.from(pkgOrgName.toString());
        PackageName packageName = PackageName.from(pkgName.toString());
        PackageVersion packageVersion = PackageVersion.from(pkgVersion.toString());
        ModuleName moduleName = ModuleName.from(packageName);
        PackageDescriptor packageDescriptor = PackageDescriptor.from(packageOrg, packageName, packageVersion);
        return ModuleDescriptor.from(moduleName, packageDescriptor);
    }

    private void assertDiagnosticEqual(Diagnostic diag, String expMsg, DiagnosticSeverity expSeverity,
                                       Location expLocation) {
        Diagnostic expDiag =
                DiagnosticFactory.createDiagnostic(new DiagnosticInfo(null, expMsg, expSeverity), expLocation);
        Assert.assertEquals(diag.toString(), expDiag.toString());
    }

}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.packaging;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Project;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.packaging.repo.BinaryRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.NonSysRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ObjRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * Testcase for repo validation.
 */
public class RepoTest {

    @Test
    public void testProjectObjRepo() {
        PackageID pkg = newPackageID("my_org", "my.pkg", "1.2.3");
        ObjRepo subject = new ObjRepo((PathConverter) null);

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/repo/my_org/my.pkg/1.2.3/obj/my.pkg.bala");
    }

    @Test
    public void testProjectSourceRepo() {
        Manifest manifest = new Manifest();
        Project manifestProject = manifest.getProject();
        manifestProject.setOrgName("best_org");
        manifestProject.setVersion("1.8.3");
        
        PackageID pkg = newPackageID("best_org", "this.pkg", "1.8.3");
        ProjectSourceRepo subject = new ProjectSourceRepo((PathConverter) null, manifest, false);

        Patten prospect = subject.calculate(pkg);

        Assert.assertEquals(prospect.toString(), "$/src/this.pkg/**~resources/*.bal");
    }

    @Test
    public void testProjectSourceRepoWithTests() {
        Manifest manifest = new Manifest();
        Project manifestProject = manifest.getProject();
        manifestProject.setOrgName("best_org");
        manifestProject.setVersion("1.8.3");
        
        PackageID pkg = newPackageID("best_org", "this.pkg", "1.8.3");
        ProjectSourceRepo subject = new ProjectSourceRepo((PathConverter) null, manifest, true);

        Patten prospect = subject.calculate(pkg);

        Assert.assertEquals(prospect.toString(), "$/src/this.pkg/**~test~resources/*.bal");
    }

    @Test
    public void testHomeRepo() {
        PackageID pkg = newPackageID("my_org", "my.pkg", "10.2.3");
        HomeRepo subject = new HomeRepo((PathConverter) null);

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/repo/my_org/my.pkg/10.2.3/src/**~resources/*.bal");
    }

    @Test
    public void testBinaryRepo() {
        PackageID pkg = newPackageID("nice_org", "any.pkg", "10.2.3");
        BinaryRepo subject = new BinaryRepo((ZipConverter) null, CompilerPhase.CODE_GEN);

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/nice_org/any.pkg/10.2.3/any.pkg.zip/bir/any.pkg.bir");
    }

    @Test
    public void testSystemOrgIsReserved() {
        PackageID pkg = newPackageID("ballerina", "any.pkg", "10.2.3");
        Repo<String> subject = new NonSysRepo<>(null) {
            @Override
            public Patten calculateNonSysPkg(PackageID pkg) {
                Assert.fail("Tried to calculate path for system packages");
                return null;
            }
        };

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten, Patten.NULL);
    }

    private PackageID newPackageID(String org, String pkg, String version) {
        return new PackageID(new Name(org), new Name(pkg), new Name(version));
    }
}

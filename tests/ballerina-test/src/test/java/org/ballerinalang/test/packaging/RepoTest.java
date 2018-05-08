package org.ballerinalang.test.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.NonSysRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ObjRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;

public class RepoTest {

    @Test
    public void testProjectObjRepo() {
        PackageID pkg = newPackageID("my_org", "my.pkg", "1.2.3");
        ObjRepo subject = new ObjRepo((PathConverter) null);

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/repo/my_org/my.pkg/1.2.3/obj/my.pkg.balo");
    }

    @Test
    public void testProjectSourceRepo() {
        PackageID pkg = newPackageID("best_org", "this.pkg", "1.8.3");
        ProjectSourceRepo subject = new ProjectSourceRepo((PathConverter) null, false);

        Patten prospect = subject.calculate(pkg);

        Assert.assertEquals(prospect.toString(), "$/this.pkg/**~resources/*.bal");
    }

    @Test
    public void testProjectSourceRepoWithTests() {
        PackageID pkg = newPackageID("best_org", "this.pkg", "1.8.3");
        ProjectSourceRepo subject = new ProjectSourceRepo((PathConverter) null, true);

        Patten prospect = subject.calculate(pkg);

        Assert.assertEquals(prospect.toString(), "$/this.pkg/**~test~resources/*.bal");
    }

    @Test
    public void testHomeRepo() {
        PackageID pkg = newPackageID("my_org", "my.pkg", "10.2.3");
        HomeRepo subject = new HomeRepo((PathConverter) null);

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/repo/my_org/my.pkg/10.2.3/src/**~resources/*.bal");
    }

    @Test
    public void testCacheRepo() {
        PackageID pkg = newPackageID("nice_org", "any.pkg", "10.2.3");
        CacheRepo subject = new CacheRepo((PathConverter) null, "test");

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/caches/test/nice_org/any.pkg/10.2.3/any.pkg.zip/src/" +
                "**~resources/*.bal");
    }

    @Test
    public void testSystemOrgIsReserved() {
        PackageID pkg = newPackageID("ballerina", "any.pkg", "10.2.3");
        Repo subject = new NonSysRepo<String>(null) {
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

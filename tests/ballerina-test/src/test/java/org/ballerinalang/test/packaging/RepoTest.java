package org.ballerinalang.test.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectObjRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;
import org.wso2.ballerinalang.compiler.util.Name;

public class RepoTest {

    @Test
    public void testProjectObjRepo() {
        PackageID pkg = newPackageID("my_org", "my.pkg", "1.2.3");
        ProjectObjRepo subject = new ProjectObjRepo((PathResolver) null);

        Patten patten = subject.calculate(pkg);

        Assert.assertEquals(patten.toString(), "$/.ballerina_project/repo/my_org/my.pkg/1.2.3/obj/my.pkg.balo");
    }

    @Test
    public void testProjectSourceRepo() {
        PackageID pkg = newPackageID("best_org", "this.pkg", "1.8.3");
        ProjectSourceRepo subject = new ProjectSourceRepo((PathResolver) null);

        Patten prospect = subject.calculate(pkg);

        Assert.assertEquals(prospect.toString(), "$/this.pkg/**~test~resources/*.bal");
    }

    @Test
    public void testHomeRepo() {
        PackageID pkg = newPackageID("my_org", "my.pkg", "10.2.3");
        HomeRepo subject = new HomeRepo((PathResolver) null);

        Patten prospect = subject.calculate(pkg);

        Assert.assertEquals(prospect.toString(), "$/repo/my_org/my.pkg/10.2.3/src/**~test~resources/*.bal");
    }

    private PackageID newPackageID(String org, String pkg, String version) {
        return new PackageID(new Name(org), new Name(pkg), new Name(version));
    }
}

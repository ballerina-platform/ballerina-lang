package src.test.java.org.ballerinalang.packageloader;

import java.io.IOException;

import org.ballerinalang.packageloader.ModuleLoader;
import org.ballerinalang.packageloader.ModuleLoaderImpl;
import org.ballerinalang.packageloader.Project;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ModuleLoaderTests {




    @Test(description = "")
    public void test() throws IOException {
        Project p = new Project();
        p.parseBallerinaToml("");

        ModuleLoaderImpl ml = new ModuleLoaderImpl(p);
        ml.resolveVersion();

    }
}

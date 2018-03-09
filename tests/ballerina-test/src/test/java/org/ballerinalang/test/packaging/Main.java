package org.ballerinalang.test.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectObjRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path projectRoot = Paths.get("./tests/test-proj");
        PackageID pkg = new PackageID(new Name("my_org"), new Name("my.pkg"), new Name("1.2.3"));

        Repo remote = new RemoteRepo("http://central.io");
        Repo homeCache = new CacheRepo(Paths.get("~/.ballerina", "caches"));
        Repo projectCache = new CacheRepo(projectRoot.resolve(".ballerina").resolve("caches"));
        Repo home = new HomeRepo(Paths.get("~/.ballerina", "repo"));
        Repo projectRepo = new ProjectObjRepo(projectRoot);
        Repo projectSource = new ProjectSourceRepo(projectRoot);

//        RepoHierarchy homeCacheDag =
//                new RepoHierarchy(homeCache,
//                            new RepoHierarchy(remote));
//        RepoHierarchy repos =
//                new RepoHierarchy(projectSource,
//                            new RepoHierarchy(projectRepo,
//                                        new RepoHierarchy(home, homeCacheDag), new RepoHierarchy(projectCache, homeCacheDag)));
//
//
//        Resolution match = repos.resolve(pkg);
//        PrintStream out = System.out;
//        out.println(match.paths);
//        out.println(match.resolvedBy);
    }
}

package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps list of n Repos in lookup order.
 * Also has a list of (n-1) child RepoHierarchy objects.
 * <p>
 * (n-1) because, each Repo is matched with one child RepoHierarchy,
 * except for the fist one, it matches with this object.
 */
public class RepoHierarchy {

    private final Repo[] repos;
    private final RepoHierarchy[] dags;

    RepoHierarchy(Repo[] repos, RepoHierarchy[] dags) {
        this.repos = repos;
        this.dags = dags;
    }

    public Resolution resolve(PackageID pkg) {
//        PrintStream out = System.out;
//        out.println("Searching " + pkg);
        for (int i = 0; i < repos.length; i++) {
            Repo repo = repos[i];
            Patten patten = repo.calculate(pkg);
            if (patten != Patten.NULL) {
                Converter converter = repo.getConverterInstance();
                List<PackageSourceEntry> paths = patten.convertToSources(converter, pkg)
                                                       .collect(Collectors.toList());
//                out.println("\t looking in " + repo + " for patten\n\t\t" +
//                                    patten + " and found \n\t\t\t" +
//                                    sources);
                if (!paths.isEmpty()) {
                    return new Resolution(getChildHierarchyForRepo(i), paths);
                }
            } else {
//                out.println("\t skipping " + repo);
            }
        }
//        out.println("\t could not find");
        return Resolution.NOT_FOUND;
    }

    private RepoHierarchy getChildHierarchyForRepo(int repoIndex) {
        if (repoIndex > 0) {
            return dags[repoIndex - 1];
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "{r:" + Arrays.toString(repos) +
                ", d:" + Arrays.toString(dags) + "}";
    }

}

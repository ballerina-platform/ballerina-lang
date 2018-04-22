package io.ballerina.transactions;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.wso2.ballerinalang.compiler.packaging.repo.JarRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;

/**
 * This represents the standard Ballerina built-in system package repository provider.
 * 
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.spi.SystemPackageRepositoryProvider")
public class TransactionsPackageRepositoryProvider implements SystemPackageRepositoryProvider {

    public Repo loadRepository() {
        return new JarRepo(SystemPackageRepositoryProvider.getClassUri(this));
    }

}

package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

/**
 * Patent of all the non-system repos.
 * Sub classes of this class can't load pakages in the reserved org name ('ballerina')
 *
 * @param <I> Intermediate representation type of the repo. See {@link Repo}
 */
public abstract class NonSysRepo<I> implements Repo<I> {
    private final Converter<I> converter;

    public NonSysRepo(Converter<I> converter) {
        this.converter = converter;
    }

    @Override
    public final Patten calculate(PackageID pkg) {
        // TODO: remove pkg name check, only org should be checked.
        if ("ballerina".equals(pkg.getOrgName().getValue()) ||
                pkg.getName().getValue().startsWith("ballerina.")) {
             return Patten.NULL;
        } else {
        }
        return calculateNonSysPkg(pkg);
    }

    @Override
    public Converter<I> getConverterInstance() {
        return converter;
    }

    public abstract Patten calculateNonSysPkg(PackageID pkg);

    @Override
    public String toString() {
        return "{t:'" + this.getClass().getSimpleName() + "', c:'" + converter + "'}";
    }
}

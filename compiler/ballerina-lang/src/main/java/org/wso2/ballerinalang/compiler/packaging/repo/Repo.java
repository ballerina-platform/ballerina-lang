package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

/**
 * Calculate the path patten of a given package.
 * And also provide a reference to a Converter
 * that can turn that path patten to a usable object such as Path
 * <pre>
 *
 *  Repo ---&gt;  Patten ---&gt; Usable Object (eg: Path)
 *     \         ^
 *      \        |
 *       `---&gt; Converter
 *
 * </pre>
 *
 * @param <I> Intermediate representation type of the repo.
 */
public interface Repo<I> {
    Patten calculate(PackageID pkg);

    Converter<I> getConverterInstance();
}

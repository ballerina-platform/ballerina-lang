/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    private final boolean allowBalOrg = Boolean.parseBoolean(System.getProperty("BALLERINA_DEV_MODE_COMPILE"));

    public NonSysRepo(Converter<I> converter) {
        this.converter = converter;
    }

    @Override
    public final Patten calculate(PackageID pkgId) {
        // TODO: remove pkg name check, only org should be checked.
        String orgName = pkgId.getOrgName().getValue();
        if (!allowBalOrg &&
            "ballerina".equals(orgName) ||
            "ballerinax".equals(orgName) ||
            pkgId.getName().getValue().startsWith("ballerina.")) {
            return Patten.NULL;
        } else {
            return calculateNonSysPkg(pkgId);
        }
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

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.URIConverter;
import org.wso2.ballerinalang.compiler.util.Names;

import java.net.URI;

/**
 * Calculate url pattens of package.
 */
public class RemoteRepo extends NonSysRepo<URI> {

    public RemoteRepo(Converter<URI> converter) {
        super(converter);
    }

    public RemoteRepo(URI base) {
        this(new URIConverter(base));
    }

    public RemoteRepo(URI base, boolean isBuild) {
        this(new URIConverter(base, isBuild));
    }

    @Override
    public Patten calculateNonSysPkg(PackageID pkg) {
        // Central requires an org name.
        if (pkg.getOrgName().equals(Names.ANON_ORG)) {
            return Patten.NULL;
        }
        
        String orgName = pkg.getOrgName().value;
        String pkgName = pkg.getName().value;
        String pkgVersion = pkg.getPackageVersion().value;
        if (pkgVersion.isEmpty()) {
            pkgVersion = "*";
        }

        return new Patten(Patten.path(orgName, pkgName, pkgVersion));
    }

}

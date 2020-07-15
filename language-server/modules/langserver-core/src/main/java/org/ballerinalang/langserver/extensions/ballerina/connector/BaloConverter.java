/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Provide functions need to covert a patten to a balo path.
 */
public class BaloConverter implements Converter<Path> {

    @Override
    public Path combine(Path a, String b) {
        if (!a.toString().endsWith(".balo")) {
            return (a.resolve(b));
        } else {
            return a;
        }
    }

    @Override
    public Stream<Path> getLatestVersion(Path s, PackageID packageID) {
        return Stream.of(s);
    }

    @Override
    public Stream<Path> expandBalWithTest(Path t) {
        return Stream.of(t);
    }

    @Override
    public Stream<Path> expandBal(Path s) {
        return Stream.of(s);
    }

    @Override
    public Path start() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
    }

    @Override
    public Stream<CompilerInput> finalize(Path s, PackageID id) {
        throw new UnsupportedOperationException();
    }
}

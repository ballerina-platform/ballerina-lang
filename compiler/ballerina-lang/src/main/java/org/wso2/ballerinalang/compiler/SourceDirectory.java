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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @since 0.965.0
 */
public interface SourceDirectory {

    boolean canHandle(Path dirPath);

    Path getPath();

    List<String> getSourceFileNames();

    List<String> getSourcePackageNames();

    InputStream getManifestContent();

    InputStream getLockFileContent();

    Path saveCompiledProgram(InputStream source, String fileName);

    void saveCompiledPackage(CompiledPackage compiledPackage, Path dirPath, String fileName) throws IOException;

    Converter<Path> getConverter();
}

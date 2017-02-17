/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.program;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * This class contains a set of static methods to operate on {@code BLangProgram} objects.
 *
 * @since 0.8.0
 */
public class BLangPrograms {
    public static final String BSOURCE_FILE_EXT = ".bal";

    public static GlobalScope populateGlobalScope() {
        // Get the global scope
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BuiltInNativeConstructLoader.loadConstructs(globalScope);
        return globalScope;
    }

    public static Path validateAndResolveArchivePath(Path programArchivePath,
                                                     BLangProgram.Category programCategory) {
        if (programArchivePath == null) {
            throw new IllegalArgumentException("program archive path cannot be null");
        }

        if (!programArchivePath.getFileName().toString().endsWith(programCategory.getExtension())) {
            throw new IllegalArgumentException("invalid file or directory: expected a " +
                    programCategory.getExtension() + " file");
        }

        try {
            Path realProgArchivePath = programArchivePath.toRealPath(LinkOption.NOFOLLOW_LINKS);

            if (!Files.isReadable(realProgArchivePath)) {
                throw new IllegalArgumentException("read access required: " + programArchivePath.toString());
            }

            if (Files.isDirectory(realProgArchivePath, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalStateException("invalid file: expected a " +
                        programCategory.getExtension() + " file");
            }

            return realProgArchivePath;
        } catch (NoSuchFileException x) {
            throw new IllegalArgumentException("no such file or directory: " + programArchivePath.toString());
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + programArchivePath +
                    " reason: " + e.getMessage(), e);
        }
    }

    public static Path validateAndResolveProgramDirPath(Path programDirPath) {
        if (programDirPath == null) {
            throw new IllegalArgumentException("program directory cannot be null");
        }

        try {
            return programDirPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException x) {
            throw new IllegalArgumentException("no such file or directory: " + programDirPath);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + programDirPath +
                    " reason: " + e.getMessage(), e);
        }
    }

    public static Path validateAndResolveSourcePath(Path programDirPath, Path sourcePath,
                                                    BLangProgram.Category programCategory) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        if (sourcePath.toString().endsWith(programCategory.getExtension())) {
            return validateAndResolveArchivePath(sourcePath, programCategory);
        }

        try {
            Path realSourcePath = programDirPath.resolve(sourcePath).toRealPath();

            if (Files.isDirectory(realSourcePath, LinkOption.NOFOLLOW_LINKS)) {
                return realSourcePath;
            }

            if (!realSourcePath.toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
                throw new IllegalArgumentException("invalid file: " + sourcePath);
            }

            return realSourcePath;
        } catch (NoSuchFileException x) {
            throw new IllegalArgumentException("no such file or directory: " + sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + sourcePath +
                    " reason: " + e.getMessage(), e);
        }
    }
}

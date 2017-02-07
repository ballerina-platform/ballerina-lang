/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.docgen.docs;

import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.docgen.docs.html.HtmlDocumentWriter;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main class to generate a ballerina documentation
 */
public class BallerinaDocGeneratorMain {

    private static final PrintStream out = System.out;

    public static void main(String[] args) {

        if ((args == null) || (args.length < 1 || args.length > 2)) {
            out.println("Usage: docerina [ballerina-package-path] [package-filter]");
            return;
        }

        try {
            Map<String, Package> docsMap = generatePackageDocsFromBallerina(args[0],
                    (args.length == 2) ? args[1] : null);
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(docsMap.values());
        } catch (IOException e) {
            out.println("Docerina: Could not read ballerina file(s): " + e.getMessage());
        }
    }

    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link Package} objects.
     * Key - Ballerina package name
     * Value - {@link Package}
     */
    public static Map<String, Package> generatePackageDocsFromBallerina(
            String packagePath) throws IOException {
        return generatePackageDocsFromBallerina(packagePath, null);
    }

    /**
     * Generates {@link Package} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath   should point either to a ballerina file or a folder with ballerina files.
     * @param packageFilter the name of the package or pattern to be excluded
     * @return a map of {@link Package} objects.
     * Key - Ballerina package name
     * Value - {@link Package}
     */
    public static Map<String, Package> generatePackageDocsFromBallerina(
            String packagePath, String packageFilter) throws IOException {

        List<Path> filePaths = new ArrayList<>();

        Files.find(Paths.get(packagePath), Integer.MAX_VALUE,
                (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(".*\\.bal")).
                forEach(path -> filePaths.add(path));

        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();
        for (Path path : filePaths) {
            BallerinaFile balFile = BallerinaDocUtils.buildLangModel(path);
            if (balFile == null) {
                out.println(String.format("Docerina: Invalid Ballerina file: %s", path));
                continue;
            }

            String packageName = balFile.getPackagePath();
            if ((packageFilter != null) && (packageFilter.trim().length() > 0) &&
                    (packageName.startsWith(packageFilter.replace(".*", "")))) {
                out.println("Package " + packageName + " excluded");
                continue;
            }

            Package balPackage = dataHolder.getPackageMap().get(packageName);
            if (balPackage == null) {
                balPackage = new Package(packageName);
                dataHolder.getPackageMap().put(packageName, balPackage);
            }
            balPackage.addFiles(balFile);
        }
        return dataHolder.getPackageMap();
    }
}

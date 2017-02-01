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

import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.docgen.docs.html.HtmlDocumentWriter;
import org.wso2.ballerina.docgen.docs.model.BallerinaPackageDoc;
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

        if ((args == null) || (args.length != 1)) {
            out.println("Usage: docerina [ballerina-package-path]");
            return;
        }

        try {
            Map<String, BallerinaPackageDoc> docsMap = generatePackageDocsFromBallerina(args[0]);
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(docsMap.values());
        } catch (IOException e) {
            out.println("Docerina: Could not read ballerina file(s): " + e.getMessage());
        }
    }

    /**
     * Generates {@link BallerinaPackageDoc} objects for each Ballerina package from the given ballerina files.
     *
     * @param packagePath should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link BallerinaPackageDoc} objects.
     * Key - Ballerina package name
     * Value - {@link BallerinaPackageDoc}
     */
    public static Map<String, BallerinaPackageDoc> generatePackageDocsFromBallerina(String packagePath)
            throws IOException {

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

            BallerinaPackageDoc packageDoc = dataHolder.getBallerinaPackageDocsMap().get(balFile.getPackageName());
            if (packageDoc == null) {
                packageDoc = new BallerinaPackageDoc(balFile.getPackageName());
                dataHolder.getBallerinaPackageDocsMap().put(balFile.getPackageName(), packageDoc);
            }

            for (Function function : balFile.getFunctions()) {
                if (function instanceof BallerinaFunction) {
                    packageDoc.addFunction((BallerinaFunction) function);
                } else {
                    out.println("Warning: An unknown function type found: " + function.getClass().getSimpleName() + ":"
                            + function.getName());
                }
            }

            for (BallerinaConnector connector : balFile.getConnectors()) {
                packageDoc.addConnector(connector);
            }
        }

        return dataHolder.getBallerinaPackageDocsMap();
    }
}

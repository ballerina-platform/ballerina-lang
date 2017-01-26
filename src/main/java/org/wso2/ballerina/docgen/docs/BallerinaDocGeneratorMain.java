/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.docgen.docs.model.BallerinaDoc;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Main class to generate a ballerina documentation
 */
public class BallerinaDocGeneratorMain {
    
    private static final PrintStream out = System.out;

    public static void main(String[] args) {

        Map<String, BallerinaDoc> docsMap = generateDocsFromBallerina(args[0]);
        for (Entry<String, BallerinaDoc> entry : docsMap.entrySet()) {
            out.println(entry.getValue().toString());
        }

    }
    
    /**
     * Generates {@link BallerinaDoc} objects for each Ballerina package from the given ballerina files.
     * @param path should point either to a ballerina file or a folder with ballerina files.
     * @return a map of {@link BallerinaDoc} objects. 
     *  Key - Ballerina package name
     *  Value - {@link BallerinaDoc}
     */
    public static Map<String, BallerinaDoc> generateDocsFromBallerina(String path) {
        String ballerinaFolder = path;
        File[] ballerinaFiles;

        File ballerinaFolderFile = new File(ballerinaFolder);
        if (ballerinaFolderFile.isDirectory()) {
            ballerinaFiles = ballerinaFolderFile.listFiles(new FileFilter() {
                
                @Override
                public boolean accept(File file) {
                    // TODO get .bal from Core constants.
                    if (file.getName().endsWith(".bal")) {
                        return true;
                    }
                    return false;
                }
            });
        } else {
            ballerinaFiles = new File[] { ballerinaFolderFile };
        }

        if (ballerinaFiles == null) {
            return new HashMap<String, BallerinaDoc>();
        }
        BallerinaDocDataHolder dataHolder = BallerinaDocDataHolder.getInstance();

        for (File file : ballerinaFiles) {
            BallerinaFile balFile = BallerinaDocUtils.buildLangModel(file.toPath());
            if (balFile == null) {
                out.println(String.format("Docerina: Invalid Ballerina File: %s.", file.getAbsolutePath()));
                continue;
            }
            SymScope globalScope = GlobalScopeHolder.getInstance().getScope();
            DocumentGenerator docgen = new DocumentGenerator(balFile, globalScope);
            docgen.visit(balFile);
        }

        return dataHolder.getBallerinaDocsMap();
    }

}

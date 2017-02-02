/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.docgen.docs.html;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.docgen.docs.DocumentWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * HTML document writer generates ballerina API documentation in HTML format.
 */
public class HtmlDocumentWriter implements DocumentWriter {

    private static PrintStream out = System.out;

    public static final String PACKAGE_TEMPLATE_FILE_KEY = "package.template.filename";
    public static final String HTML_OUTPUT_PATH_KEY = "html.output.path";
    public static final String TEMPLATES_FOLDER_PATH_KEY = "templates.folder.path";

    private String templatesFolderPath;
    private String outputFilePath;
    private String packageTemplateFileName;

    public HtmlDocumentWriter() {
        String userDir = System.getProperty("user.dir");
        this.outputFilePath = System.getProperty(HTML_OUTPUT_PATH_KEY,
                userDir + File.separator + "api-docs" + File.separator + "html");
        this.templatesFolderPath =  System.getProperty(TEMPLATES_FOLDER_PATH_KEY,
                userDir + File.separator + "templates" + File.separator + "html");
        this.packageTemplateFileName = System.getProperty(PACKAGE_TEMPLATE_FILE_KEY, "package");
    }

    @Override
    public void write(Collection<Package> packages) {
        if (packages == null || packages.size() == 0) {
            out.println("No package definitions found!");
            return;
        }

        out.println("Generating HTML API documentation...");
        for (Package balPackage : packages) {
            writeHtmlDocument(balPackage);
        }
    }

    /**
     * Write HTML document for a given ballerina package
     * @param balPackage Ballerina package object
     */
    private void writeHtmlDocument(Package balPackage) {
        PrintWriter writer = null;
        try {
            TemplateLoader templateLoader = new FileTemplateLoader(templatesFolderPath);
            Handlebars handlebars = new Handlebars(templateLoader);
            Template template = handlebars.compile(packageTemplateFileName);

            String filePath = outputFilePath + File.separator + balPackage.getFullyQualifiedName() + ".html";
            writer = new PrintWriter(filePath, "UTF-8");
            writer.println(template.apply(balPackage));
            out.println("HTML file written: " + filePath);
        } catch (IOException e) {
            out.println("Docerina: Could not write HTML file of package " + balPackage.getFullyQualifiedName() +
                    System.lineSeparator() + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

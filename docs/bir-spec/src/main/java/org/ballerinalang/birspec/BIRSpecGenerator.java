/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.birspec;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Generate BIR spec doc.
 */
public class BIRSpecGenerator {

    private static final String BIR_SPEC_FILE = "/kaitai/bir.ksy";
    private static final Yaml yaml = new Yaml();

    public static void main(String[] args) throws IOException {
        Object birSpecYaml = parseResourceAsYAML();
        String hbs = readResourceAsString("/handlebars/bir-spec.md.hbs");

        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(hbs);
        String result = template.apply(birSpecYaml);

        try (PrintWriter out = new PrintWriter("../compiler/bir-spec.md")) {
            out.println(result);
        }
    }

    private static Object parseResourceAsYAML() throws IOException {
        try {
            String yml = readResourceAsString(BIR_SPEC_FILE);
            return yaml.load(yml);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    private static String readResourceAsString(String uri) throws IOException {
        InputStream inputStream = BIRSpecGenerator.class.getResourceAsStream(uri);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
}

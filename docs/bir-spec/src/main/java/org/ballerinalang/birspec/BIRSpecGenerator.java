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
 * generate BIR spec doc.
 */
public class BIRSpecGenerator {

    private static final Yaml yaml = new Yaml();

    public static void main(String[] args) throws IOException {
        Object birSpecYaml = parseResourceAsYAML("/kaitai/bir.ksy");
        String hbs = readResourceAsString("/handlebars/bir-spec.md.hbs");

        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(hbs);
        String result = template.apply(birSpecYaml);

        try (PrintWriter out = new PrintWriter("../compiler/bir-spec.md")) {
            out.println(result);
        }
    }

    private static Object parseResourceAsYAML(String filename) throws IOException {
        try {
            String yml = readResourceAsString(filename);
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

package org.ballerinalang.docgen;


import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Generates the HTML pages from the Page objects
 */
public class Writer {
    private static PrintStream out = System.out;

    /**
     * Write the HTML document from the Page object for a bal package
     * @param object Page object which is generated from the bal package
     * @param packageTemplateName hbs template file to be used
     * @param filePath path of the file to write the output
     */
    public static void writeHtmlDocument(Object object, String packageTemplateName, String filePath) {
        String templatesFolderPath =
                System.getProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, File.separator
                        + "docerina-templates" + File.separator + "html");
        PrintWriter writer = null;
        try {
            Handlebars handlebars =
                    new Handlebars().with(new ClassPathTemplateLoader(templatesFolderPath), new FileTemplateLoader(
                            templatesFolderPath));
            handlebars.registerHelpers(StringHelpers.class);
            Template template = handlebars.compile(packageTemplateName);

            writer = new PrintWriter(filePath, "UTF-8");

            Context context = Context
                    .newBuilder(object)
                    .resolver(FieldValueResolver.INSTANCE)
                    .build();
            writer.println(template.apply(context));
            out.println("HTML file written: " + filePath);
        } catch (IOException e) {
            out.println("docerina: could not write HTML file " + filePath +
                    System.lineSeparator() + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

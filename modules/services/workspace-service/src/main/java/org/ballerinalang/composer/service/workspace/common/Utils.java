package org.ballerinalang.composer.service.workspace.common;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BallerinaComposerErrorStrategy;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BallerinaComposerModelBuilder;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.ballerinalang.util.program.BLangPrograms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 *  Class with common utility functions used by workspace services
 */
public class Utils {
    /**
     *
     * @param bFile - BallerinaFile
     * @param filePath - file path to parent directory of the .bal file
     * @return  Path to ProgramDirectory
     */
    public static java.nio.file.Path getProgramDirectory(BallerinaFile bFile, String filePath) {
        return getProgramDirectory(bFile, Paths.get(filePath));
    }

    /**
     *
     * @param filePath - file path to parent directory of the .bal file
     * @return BallerinaFile in the given path
     * @throws IOException for IO errors
     */
    public static BallerinaFile getBFile(String filePath) throws IOException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(filePath));

            BallerinaFile bFile = getBFile(stream, Paths.get(filePath));
            return bFile;
        } finally {
            if (null != stream) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     *
     * @param bFile - BallerinaFile
     * @param filePath - file path to parent directory of the .bal file
     * @return parent dir
     */
    public static java.nio.file.Path getProgramDirectory(BallerinaFile bFile, java.nio.file.Path filePath) {
        // find nested directory count using package name
        int directoryCount = (bFile.getPackagePath().contains(".")) ? bFile.getPackagePath().split("\\.").length
                : 1;

        // find program directory
        java.nio.file.Path parentDir = filePath.getParent();
        for (int i = 0; i < directoryCount; ++i) {
            parentDir = parentDir.getParent();
        }
        return parentDir;
    }

    /**
     *
     * @param stream - The input stream.
     * @param filePath - file path to parent directory of the .bal file
     * @return BallerinaFile in the given path
     * @throws IOException for IO errors
     */
    public static BallerinaFile getBFile(InputStream stream, java.nio.file.Path filePath) throws IOException {
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);


        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(true, ballerinaToken, bLangModelBuilder,
                filePath);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();
        return bFile;
    }
}

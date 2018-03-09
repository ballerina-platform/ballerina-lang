package org.ballerinalang.launcher.toml.util;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.ballerinalang.launcher.toml.antlr4.TomlLexer;
import org.ballerinalang.launcher.toml.antlr4.TomlParser;

/**
 * Util methods for toml processor.
 *
 * @since 0.964
 */
public class TomlProcessor {

    /**
     * Generate the proxy object by passing in the toml file.
     *
     * @param stream charstream object containing the content
     * @return proxy object
     */
    public static ParseTree parseTomlContent(CharStream stream) {
        TomlLexer lexer = new TomlLexer(stream);

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        TomlParser parser = new TomlParser(tokens);
        return parser.toml();
    }
}

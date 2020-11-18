package toml.parser.test;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.RootSchema;
import io.ballerina.toml.validator.Schema;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestGson {
    public static void main(String [] args) throws IOException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "test.json");
        RootSchema from = RootSchema.from(resourceDirectory);
        System.out.println(from);
    }
}

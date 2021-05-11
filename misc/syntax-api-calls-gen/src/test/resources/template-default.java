package templatepkg;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.syntaxapicallsgen.test.TemplateCode;

public class TemplateCodeImpl implements TemplateCode {
    @Override
    public Node getNode() {
        return %s;
    }
}

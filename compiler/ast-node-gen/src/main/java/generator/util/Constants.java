package generator.util;

public class Constants {
    public static final String ABSTRACT_CLASS = "abstract class";
    public static final String ABSTRACT_KEYWORD = "abstract";
    public static final String INTERNAL_ATTRIBUTE_VISIBILITY = "public";
    public static final String CASE_KEYWORD = "case";
    public static final String FACADE_ATTRIBUTE_VISIBILITY = "private";
    public static final String FINAL_KEYWORD = "final";
    public static final String CLASS_VISIBILITY = "public";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String EXTENDS_KEYWORD = "extends";
    public static final String OPEN_BRACKETS = "(";
    public static final String CLOSE_BRACKETS = ")";
    public static final String OPEN_PARENTHESIS = "{";
    public static final String NEW_LINE = "\n";
    public static final String THIS_KEYWORD = "this";
    public static final String DOT = ".";
    public static final String SEMI_COLON = ";";
    public static final String INTERFACE = "interface";
    public static final String CLOSE_PARENTHESIS = "}";
    public static final String LESS_THAN_SYMBOL = "<";
    public static final String GREATER_THAN_SYMBOL = ">";
    public static final String ASSIGNMENT_OPERATOR = "=";
    public static final String WHITE_SPACE = " ";
    public static final String PARAMETER_SEPARATOR = ",";
    public static final String NULL_KEYWORD = "null";
    public static final String CLASS_KEYWORD = "class";
    public static final String JAVA_EXT = "java";
    public static final String INT_TYPE = "int";
    public static final String DOUBLE_TYPE = "double";
    public static final String TO_STRING_SIGNATURE = "toString";
    public static final String RETURN_KEYWORD = "return";
    public static final String STRING_KEYWORD = "String";
    public static final String DOUBLE_QUOTES = "\"\"";
    public static final String CONCAT_SYMBOL = "+";
    public static final String COLON = ":";
    public static final String GEN_WHITESPACE = "\" \"";
    public static final String LIST_KEYWORD = "List";
    public static final String LIST_IMPORT_STRING = "import java.util.List;";
    public static final String FACADE_RETURN_STATEMENT = "return new BL$className(this, position, parent)";
    public static final String FACADE_LEAF_RETURN_STATEMENT = "throw new UnsupportedOperationException()";
    public static final String FACADE_FUNCTION = "public BLNode createFacade(int position, " +
            "BLNonTerminalNode parent) {\n$facadeReturnStatementPlaceholder;\n}";
    public static final String FACADE_MEMBER_FUNCTION = "public $type $attribute() {\n" +
            "if ($attribute != null) {\nreturn $attribute;\n}\n$attribute = $functionCall\n" +
            "return $attribute;\n}";
    public static final String CALL_CREATE_FACADE = "node.childInBucket($index).createFacade(getChildPosition" +
            "($index), this);";
    public static final String CALL_CREATE_TOKEN = "createToken($index);";
    public static final String CALL_CREATE_NODE_LIST = "createListNode($index);";
    public static final String TYPE_REGEX = "<Type>";
    public static final String LIST_NAME_REGEX = "<ListName>";
    public static final String BUILDER_REGEX = "<builderName>";
    public static final String BUILDER = "Builder";
    public static final String CHILD_NODE_FUNCTION = "addChildNode";
    public static final String MISSING_TOKEN = "MissingToken";
    public static final String LEADING_TRIVIA = "leadingTrivia";
    public static final String TRAILING_TRIVIA = "trailingTrivia";
    public static final String SYNTAX_TRIVIA = "SyntaxTrivia";
    public static final String SYNTAX_TOKEN = "SyntaxToken";
    public static final String TOKEN = "Token";
    public static final String TRIVIA = "Trivia";
    public static final String NON_TERMINAL_NODE = "BLNonTerminalNode";
    public static final String BL = "BL";
    public static final String PROPERTY = "text";
    public static final String KIND_PROPERTY = "kind.strValue";
    public static final String BL_TOKEN = "BLSyntaxToken";
    public static final String BL_LIST = "BLNodeList<$variable>";
    public static final String BLNode = "BLNode";
    public static final String FACADE_CONSTRUCTOR = "public $className(SyntaxNode node, int position, " +
            "BLNonTerminalNode parent) {\nsuper(node, position, parent);\n}";
    public static final String FACADE_PACKAGE = "package generated.facade;";
    public static final String INTERNAL_PACKAGE = "package generated.internal;";
    public static final String INTERNAL_IMPORTS = "import generated.facade.*;\n";
    public static final String FACADE_IMPORTS = "import generated.internal.SyntaxNode;\n";
    public static final String FACADE_SWITCH ="switch (bucket) {\n$caseStatements\n}";
    public static final String CHILD_FUNCTION = "public BLNode childInBucket(int bucket) {\n" +
            "$switchStatement\nreturn null;\n}";

    public static final String IMPORTS_PLACEHOLDER = "$imports\n";
    public static final String CLASSNAME_PLACEHOLDER = "$className";
    public static final String TYPE_PLACEHOLDER = "$type";
    public static final String RELATIONSHIP_PLACEHOLDER = "$relationship";
    public static final String ABSTRACT_PLACEHOLDER = "$abstract";
    public static final String ATTRIBUTES_PLACEHOLDER = "$attributes";
    public static final String ATTRIBUTE_PLACEHOLDER = "$attribute";
    public static final String CONSTRUCTOR_PLACEHOLDER = "$constructors";
    public static final String PARENT_CLASS_PLACEHOLDER = "$parentClass";
    public static final String PRAM_LIST_PLACEHOLDER = "$paramList";
    public static final String PRAM_INIT_PLACEHOLDER = "$paramInit";
    public static final String BUCKET_COUNT_PLACEHOLDER = "$bucketCount";
    public static final String ADD_CHILD_NODE_PLACEHOLDER = "$addChildNode";
    public static final String IMMEDIATE_PARENT_PLACEHOLDER = "$immediateParent";
    public static final String TO_STRING_FUNCTION_PLACEHOLDER = "$toStringFunction";
    public static final String BUCKET_CODE_PLACEHOLDER = "\nthis.bucketCount = $bucketCount;\n" +
            "this.childBuckets = new SyntaxNode[$bucketCount];";
    public static final String FACADE_RETURN_STATEMENT_PLACEHOLDER = "$facadeReturnStatementPlaceholder";
    public static final String FACADE_FUNCTION_PLACEHOLDER = "$facadeFunction";
    public static final String NODE_VARIABLE_PLACEHOLDER = "$variable";
    public static final String FACADE_MEMBER_PLACEHOLDER = "$facadeMember";
    public static final String CASE_STATEMENTS_PLACEHOLDER = "$caseStatements";
    public static final String FUNCTION_CALL_PLACEHOLDER = "$functionCall";
    public static final String INDEX_PLACEHOLDER = "$index";
    public static final String BL_NODE_PLACEHOLDER = "$BLNode";
    public static final String PACKAGE_PLACEHOLDER = "$package";
    public static final String FACADE_SWITCH_PLACEHOLDER = "$switchStatement";
    public static final String CHILD_FUNCTION_PLACEHOLDER = "$childFunction";
}

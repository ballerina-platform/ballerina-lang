package generator;

import com.google.gson.Gson;
import generator.util.Constants;
import model.Field;
import model.Node;
import model.Tree;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClassGeneratorTemplate {
    private static final String ABSTRACT_CLASS = "abstract class";
    private static final String ABSTRACT_KEYWORD = "abstract";
    private static final String INTERNAL_ATTRIBUTE_VISIBILITY = "public";
    private static final String CASE_KEYWORD = "case";
    private static final String FACADE_ATTRIBUTE_VISIBILITY = "private";
    private static final String FINAL_KEYWORD = "final";
    private static final String CLASS_VISIBILITY = "public";
    private static final String PUBLIC_KEYWORD = "public";
    private static final String EXTENDS_KEYWORD = "extends";
    private static final String OPEN_BRACKETS = "(";
    private static final String CLOSE_BRACKETS = ")";
    private static final String OPEN_PARENTHESIS = "{";
    private static final String NEW_LINE = "\n";
    private static final String THIS_KEYWORD = "this";
    private static final String DOT = ".";
    private static final String SEMI_COLON = ";";
    private static final String INTERFACE = "interface";
    private static final String CLOSE_PARENTHESIS = "}";
    private static final String LESS_THAN_SYMBOL = "<";
    private static final String GREATER_THAN_SYMBOL = ">";
    private static final String ASSIGNMENT_OPERATOR = "=";
    private static final String WHITE_SPACE = " ";
    private static final String PARAMETER_SEPARATOR = ",";
    private static final String NULL_KEYWORD = "null";
    private static final String CLASS_KEYWORD = "class";
    private static final String JAVA_EXT = "java";
    private static final String INT_TYPE = "int";
    private static final String DOUBLE_TYPE = "double";
    private static final String TO_STRING_SIGNATURE = "toString";
    private static final String RETURN_KEYWORD = "return";
    private static final String STRING_KEYWORD = "String";
    private static final String DOUBLE_QUOTES = "\"\"";
    private static final String CONCAT_SYMBOL = "+";
    private static final String COLON = ":";
    private static final String GEN_WHITESPACE = "\" \"";
    private static final String LIST_KEYWORD = "List";
    private static final String LIST_IMPORT_STRING = "import java.util.List;";
    private static final String FACADE_RETURN_STATEMENT = "return new BL$className(this, position, parent)";
    private static final String FACADE_LEAF_RETURN_STATEMENT = "throw new UnsupportedOperationException()";
    private static final String FACADE_FUNCTION = "public BLNode createFacade(int position, " +
            "BLNonTerminalNode parent) {\n$facadeReturnStatementPlaceholder;\n}";
    private static final String FACADE_MEMBER_FUNCTION = "public $type $attribute() {\n" +
            "if ($attribute != null) {\nreturn $attribute;\n}\n$attribute = $functionCall\n" +
            "return $attribute;\n}";
    private static final String CALL_CREATE_FACADE = "node.childInBucket($index).createFacade(getChildPosition" +
            "($index), this);";
    private static final String CALL_CREATE_TOKEN = "createToken($index);";
    private static final String CALL_CREATE_NODE_LIST = "createListNode($index);";
    private static final String TYPE_REGEX = "<Type>";
    private static final String LIST_NAME_REGEX = "<ListName>";
    private static final String BUILDER_REGEX = "<builderName>";
    private static final String BUILDER = "Builder";
    private static final String CHILD_NODE_FUNCTION = "addChildNode";
    private static final String MISSING_TOKEN = "MissingToken";
    private static final String LEADING_TRIVIA = "leadingTrivia";
    private static final String TRAILING_TRIVIA = "trailingTrivia";
    private static final String SYNTAX_TRIVIA = "SyntaxTrivia";
    private static final String SYNTAX_TOKEN = "SyntaxToken";
    private static final String TOKEN = "Token";
    private static final String TRIVIA = "Trivia";
    private static final String NON_TERMINAL_NODE = "BLNonTerminalNode";
    private static final String BL = "BL";
    private static final String PROPERTY = "text";
    private static final String KIND_PROPERTY = "kind.strValue";
    private static final String BL_TOKEN = "BLToken";
    private static final String BL_LIST = "BLNodeList<$variable>";
    private static final String BLNode = "BLNode";
    private static final String FACADE_CONSTRUCTOR = "public $className(SyntaxNode node, int position, " +
            "BLNonTerminalNode parent) {\nsuper(node, position, parent);\n}";

    private static final String IMPORTS_PLACEHOLDER = "$imports\n";
    private static final String CLASSNAME_PLACEHOLDER = "$className";
    private static final String TYPE_PLACEHOLDER = "$type";
    private static final String RELATIONSHIP_PLACEHOLDER = "$relationship";
    private static final String ABSTRACT_PLACEHOLDER = "$abstract";
    private static final String ATTRIBUTES_PLACEHOLDER = "$attributes";
    private static final String ATTRIBUTE_PLACEHOLDER = "$attribute";
    private static final String CONSTRUCTOR_PLACEHOLDER = "$constructors";
    private static final String PARENT_CLASS_PLACEHOLDER = "$parentClass";
    private static final String PRAM_LIST_PLACEHOLDER = "$paramList";
    private static final String PRAM_INIT_PLACEHOLDER = "$paramInit";
    private static final String BUCKET_COUNT_PLACEHOLDER = "$bucketCount";
    private static final String ADD_CHILD_NODE_PLACEHOLDER = "$addChildNode";
    private static final String IMMEDIATE_PARENT_PLACEHOLDER = "$immediateParent";
    private static final String TO_STRING_FUNCTION_PLACEHOLDER = "$toStringFunction";
    private static final String BUCKET_CODE_PLACEHOLDER = "\nthis.bucketCount = $bucketCount;\n" +
            "this.childBuckets = new SyntaxNode[$bucketCount];";
    private static final String FACADE_RETURN_STATEMENT_PLACEHOLDER = "$facadeReturnStatementPlaceholder";
    private static final String FACADE_FUNCTION_PLACEHOLDER = "$facadeFunction";
    private static final String NODE_VARIABLE_PLACEHOLDER = "$variable";
    private static final String FACADE_MEMBER_PLACEHOLDER = "$facadeMember";
    private static final String CASE_STATEMENTS_PLACEHOLDER = "$caseStatements";
    private static final String FUNCTION_CALL_PLACEHOLDER = "$functionCall";
    private static final String INDEX_PLACEHOLDER = "$index";

    private static List<Node> nodes;
    private static List<Node> parentNodes = new ArrayList<>();

    public static void main(String[] args) {
        try {
            nodes = getTreeNodes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateFacade();
    }

    private static void generateFacade() {
        String facadeClassString = "";
        List<Node> nodeList = restructureNodes(nodes);
        for (Node node : nodeList) {
            try {
                facadeClassString = new String(Files.readAllBytes(Paths.get("FacadeTemplateClass.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            facadeClassString = facadeClassString.replace(IMPORTS_PLACEHOLDER, generateImports(node.getFields()));
            facadeClassString = buildClassName(node, facadeClassString);
            facadeClassString = facadeClassString.replace(ATTRIBUTES_PLACEHOLDER,
                    defineFacadeAttributes(node.getFields()));
            facadeClassString = facadeClassString.replace(CONSTRUCTOR_PLACEHOLDER, FACADE_CONSTRUCTOR
                    .replace(CLASSNAME_PLACEHOLDER, node.getName()));
            facadeClassString = facadeClassString.replace(FACADE_MEMBER_PLACEHOLDER,
                    generateFacadeMembers(node.getFields()));
            facadeClassString = facadeClassString.replace(CASE_STATEMENTS_PLACEHOLDER,
                    buildCaseStatements(node.getFields()));
            try {
                writeToFile(facadeClassString, "src/main/java/" + node.getName() + DOT + JAVA_EXT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateFacadeMembers(List<Field> fieldList) {
        StringBuilder facadeMembersString = new StringBuilder();
        if (fieldList == null) {
            return "";
        } else {
            int i = 0;
            for (Field field : fieldList) {
                facadeMembersString.append(FACADE_MEMBER_FUNCTION.replace(TYPE_PLACEHOLDER, field.getType())
                        .replace(ATTRIBUTE_PLACEHOLDER, field.getName()));
                if (field.getType().equals(BLNode)) {
                    facadeMembersString = new StringBuilder(facadeMembersString.toString().replace(FUNCTION_CALL_PLACEHOLDER, CALL_CREATE_FACADE));
                } else if (field.getType().contains(LIST_KEYWORD)) {
                    facadeMembersString = new StringBuilder(facadeMembersString.toString().replace(FUNCTION_CALL_PLACEHOLDER, CALL_CREATE_NODE_LIST));
                } else {
                    facadeMembersString = new StringBuilder(facadeMembersString.toString().replace(FUNCTION_CALL_PLACEHOLDER, CALL_CREATE_TOKEN));
                }
                facadeMembersString = new StringBuilder(facadeMembersString.toString().replace(INDEX_PLACEHOLDER, String.valueOf(i)));
                facadeMembersString.append(NEW_LINE);
                i++;
            }
            return facadeMembersString.toString();
        }
    }

    private static String buildCaseStatements(List<Field> fieldList) {
        StringBuilder caseStatements = new StringBuilder();
        int i = 0;
        for (Field field : fieldList) {
            caseStatements.append(CASE_KEYWORD).append(WHITE_SPACE).append(i).append(COLON).append(NEW_LINE)
                    .append(RETURN_KEYWORD).append(WHITE_SPACE).append(field.getName()).append(OPEN_BRACKETS)
                    .append(CLOSE_BRACKETS).append(SEMI_COLON).append(NEW_LINE);
            i++;
        }
        return caseStatements.toString();
    }

    private static void generateInternalTree() {
        String internalClassString = "";
        for (Node node : nodes) {
            try {
                internalClassString = new String(Files.readAllBytes(Paths.get("InternalTemplateClass.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            internalClassString = internalClassString.replace(IMPORTS_PLACEHOLDER, generateImports(node.getFields()));
            internalClassString = buildClassName(node, internalClassString);
            internalClassString = internalClassString.replace(ATTRIBUTES_PLACEHOLDER,
                    defineAttributes(node.getFields()));
            internalClassString = buildParameterizedConstructor(internalClassString, node.getFields()
            );
            internalClassString = internalClassString.replace(ADD_CHILD_NODE_PLACEHOLDER,
                    getChildNodeCall(node.getFields()));
            if (node.getBase() != null) {
                Node parentNode = getImmediateParentNode(node.getBase());
                if (parentNode != null) {
                    internalClassString = internalClassString.replace(IMMEDIATE_PARENT_PLACEHOLDER,
                            superConstructorParentValues(parentNode.getFields()));
                } else {
                    internalClassString = internalClassString.replace(IMMEDIATE_PARENT_PLACEHOLDER, "");
                }
            }
            if (node.getFields() != null) {
                internalClassString = internalClassString.replace(BUCKET_COUNT_PLACEHOLDER,
                        String.valueOf(node.getFields().size()));
            } else {
                internalClassString = internalClassString.replace(BUCKET_CODE_PLACEHOLDER, "");
            }
            internalClassString = internalClassString.replace(TO_STRING_FUNCTION_PLACEHOLDER, createToString(node.getName(),
                    node.getFields()));
            internalClassString = internalClassString.replace(FACADE_FUNCTION_PLACEHOLDER, getFacadeFunction(node.getName()));
            try {
                writeToFile(internalClassString, "src/main/java/" + node.getName() + DOT + JAVA_EXT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Node> getTreeNodes() throws IOException {
        Gson gson = new Gson();
        Tree ast = gson.fromJson(new FileReader("newTree.json"), Tree.class);
        return ast.getNode();
    }

    private static void writeToFile(String data, String filePath) throws IOException {
        File file = new File(filePath);
        FileWriter fr = new FileWriter(file);
        fr.write(data);
        fr.close();
    }

    private static String buildClassName(Node node, String classString) {
        if (node.getType() == null) {
            classString = classString.replace(ABSTRACT_PLACEHOLDER + WHITE_SPACE, "")
                    .replace(CLASSNAME_PLACEHOLDER, node.getName()).replace(TYPE_PLACEHOLDER, "");
        } else {
            classString = classString.replace(ABSTRACT_PLACEHOLDER, node.getType())
                    .replace(CLASSNAME_PLACEHOLDER, node.getName()).replace(TYPE_PLACEHOLDER, "");
        }
        // Check for parent classes
        if (node.getBase() == null) {
            classString = classString.replace(RELATIONSHIP_PLACEHOLDER + WHITE_SPACE, "")
                    .replace(PARENT_CLASS_PLACEHOLDER, "");
        } else {
            classString = classString.replace(RELATIONSHIP_PLACEHOLDER, EXTENDS_KEYWORD)
                    .replace(PARENT_CLASS_PLACEHOLDER, node.getBase());
        }
        return classString;
    }

    private static String defineAttributes(List<Field> fieldList) {
        if (fieldList != null) {
            StringBuilder attributes = new StringBuilder();
            for (Field field : fieldList) {
                attributes.append(INTERNAL_ATTRIBUTE_VISIBILITY).append(WHITE_SPACE)
                        .append(FINAL_KEYWORD).append(WHITE_SPACE).append(field.getType())
                        .append(WHITE_SPACE).append(field.getName());
                if (field.getDefaultValue() != null) {
                    attributes.append(ASSIGNMENT_OPERATOR).append(field.getDefaultValue());
                }
                attributes.append(SEMI_COLON).append(NEW_LINE);
            }
            return attributes.toString();
        } else {
            return "";
        }
    }

    private static String buildParameterizedConstructor(String classString, List<Field> fieldList) {
        if (fieldList == null) {
            return classString.replace(PRAM_INIT_PLACEHOLDER, "")
                    .replace(PRAM_LIST_PLACEHOLDER, "");
        }
        StringBuilder paramList = new StringBuilder();
        StringBuilder initValues = new StringBuilder();
        if (fieldList.get(0).getDefaultValue() == null) {
            paramList.append(PARAMETER_SEPARATOR);
        }
        int i = 0;
        for (Field field : fieldList) {
            if (field.getDefaultValue() != null) {
                continue;
            }
            initValues.append(THIS_KEYWORD).append(DOT).append(field.getName()).append(ASSIGNMENT_OPERATOR)
                    .append(field.getName()).append(SEMI_COLON);
            paramList.append(field.getType()).append(WHITE_SPACE).append(field.getName());
            i++;
            if (i != fieldList.size()) {
                paramList.append(PARAMETER_SEPARATOR);
                initValues.append(NEW_LINE);
            }
        }
        return classString.replace(PRAM_INIT_PLACEHOLDER, initValues).replace(ClassGeneratorTemplate.PRAM_LIST_PLACEHOLDER, paramList);
    }

    private static String generateImports(List<Field> fieldList) {
        StringBuilder importString = new StringBuilder();
        if (fieldList == null) {
            return "";
        }
        for (Field field : fieldList) {
            if (field.getName().contains(LIST_KEYWORD)) {
                importString.append(LIST_IMPORT_STRING).append(NEW_LINE);
            }
            // add other imports as well
        }
        return importString.toString();
    }

//    private static void getParentNodes(String ext) {
//        for (Node node : nodes) {
//            if (ext == null) {
//                break;
//            }
//            if (ext.equals(node.getName())) {
//                parentNodes.add(node);
//                getParentNodes(node.getExt());
//                return;
//            }
//        }
//    }

    private static Node getImmediateParentNode(String ext) {
        for (Node node : nodes) {
            if (ext == null) {
                break;
            }
            if (ext.equals(node.getName())) {
                return node;
            }
        }
        return null;
    }

    private static String superConstructorParentValues(List<Field> fields) {
        if (fields == null) {
            return "";
        }
        StringBuilder values = new StringBuilder();
        fields.forEach(field -> values.append(PARAMETER_SEPARATOR).append(NULL_KEYWORD));
        return values.toString();
    }

//    private static List<Field> getSuperClassMembers(String nodeName) {
//        parentNodes.clear();
//        getParentNodes(nodeName);
//        List<Field> fieldList = new ArrayList<>();
//        for (Node node : parentNodes) {
//            if (null != node.getFields()) {
//                fieldList.addAll(node.getFields());
//            }
//        }
//        return fieldList;
//    }

    private static String getChildNodeCall(List<Field> fieldList) {
        if (fieldList == null) {
            return "";
        }
        StringBuilder childNodeCall = new StringBuilder();
        int i = 0;
        for (Field field : fieldList) {
            if (!(field.getType().equals(TYPES.INT.getTypeCode()) ||
                    field.getType().equals(TYPES.STRING.getTypeCode()) ||
                    field.getType().equals(TYPES.LONG.getTypeCode()) ||
                    field.getType().equals(TYPES.BOOLEAN.getTypeCode())))
                childNodeCall.append(THIS_KEYWORD).append(DOT).append(CHILD_NODE_FUNCTION).append(OPEN_BRACKETS)
                        .append(field.getName()).append(PARAMETER_SEPARATOR).append(i).append(CLOSE_BRACKETS)
                        .append(SEMI_COLON).append(NEW_LINE);
            i++;
        }
        return childNodeCall.toString();
    }

    private static String getFacadeFunction(String name) {
        if (name.contains(SYNTAX_TRIVIA)) {
            return FACADE_FUNCTION.replace(FACADE_RETURN_STATEMENT_PLACEHOLDER, FACADE_LEAF_RETURN_STATEMENT);
        } else if (!name.contains(TOKEN)) {
            return FACADE_FUNCTION.replace(FACADE_RETURN_STATEMENT_PLACEHOLDER, FACADE_RETURN_STATEMENT)
                    .replace(CLASSNAME_PLACEHOLDER, name);
        } else {
            return "";
        }
    }

    private static String defineFacadeAttributes(List<Field> fieldList) {
        if (fieldList != null) {
            StringBuilder attributes = new StringBuilder();
            for (Field field : fieldList) {
                attributes.append(FACADE_ATTRIBUTE_VISIBILITY).append(WHITE_SPACE).append(field.getType())
                        .append(WHITE_SPACE).append(field.getName());
                if (field.getDefaultValue() != null) {
                    attributes.append(ASSIGNMENT_OPERATOR).append(field.getDefaultValue());
                }
                attributes.append(SEMI_COLON).append(NEW_LINE);
            }
            return attributes.toString();
        } else {
            return "";
        }
    }

    private static List<Node> restructureNodes(List<Node> nodeList) {
        List<Node> modifiedNodeList = new ArrayList<>();
        for (Node node : nodeList) {
            if (!(node.getName().contains(TOKEN) || node.getName().contains(TRIVIA))) {
                Node newNode = new Node();
                newNode.setBase(NON_TERMINAL_NODE);
                newNode.setName(BL + node.getName());
                if (node.getFields() != null) {
                    List<Field> fields = new ArrayList<>();
                    for (Field field : node.getFields()) {
                        Field newField = new Field();
                        newField.setName(field.getName());
                        if (field.getType().contains(TOKEN)) {
                            newField.setType(BL_TOKEN);
                        } else if (field.getName().contains(LIST_KEYWORD)) {
                            newField.setType(BL_LIST.replace(NODE_VARIABLE_PLACEHOLDER, BLNode));
//                            newField.setType(BL_LIST.replace(NODE_VARIABLE_PLACEHOLDER, field.getName().replace(LIST_KEYWORD, "")));
                        } else {
                            newField.setType(BLNode);
                        }
                        fields.add(newField);
                    }
                    newNode.setFields(fields);
                    modifiedNodeList.add(newNode);
                }
            }
        }
        return modifiedNodeList;
    }

    private static String createToString(String nodeName, List<Field> fieldList) {
        String returnStatement;
        if (nodeName.equals(Constants.MISSING_TOKEN)) {
            return "";
        } else if (nodeName.equals(Constants.SYNTAX_TRIVIA)) {
            returnStatement = Constants.RETURN_KEYWORD + Constants.WHITE_SPACE + fieldList.get(0).getName();
        } else if (nodeName.equals(Constants.SYNTAX_TOKEN)) {
            returnStatement = Constants.RETURN_KEYWORD + Constants.WHITE_SPACE + Constants.LEADING_TRIVIA + Constants.CONCAT_SYMBOL
                    + Constants.KIND_PROPERTY + Constants.CONCAT_SYMBOL + Constants.TRAILING_TRIVIA;
        } else if (nodeName.contains(Constants.TOKEN)) {
            returnStatement = Constants.RETURN_KEYWORD + Constants.WHITE_SPACE + Constants.LEADING_TRIVIA + Constants.CONCAT_SYMBOL
                    + Constants.PROPERTY + Constants.CONCAT_SYMBOL + Constants.TRAILING_TRIVIA;
        } else {
            return "";
        }
        return Constants.PUBLIC_KEYWORD + Constants.WHITE_SPACE + Constants.STRING_KEYWORD + Constants.WHITE_SPACE + Constants.TO_STRING_SIGNATURE
                + Constants.OPEN_BRACKETS + Constants.CLOSE_BRACKETS + Constants.OPEN_PARENTHESIS + Constants.NEW_LINE + returnStatement
                + Constants.SEMI_COLON + Constants.NEW_LINE + Constants.CLOSE_PARENTHESIS;
    }

    public enum TYPES {
        INT("int"),
        STRING("String"),
        LONG("long"),
        BOOLEAN("boolean");
        private final String typeCode;

        TYPES(String levelCode) {
            this.typeCode = levelCode;
        }

        public String getTypeCode() {
            return this.typeCode;
        }
    }
}

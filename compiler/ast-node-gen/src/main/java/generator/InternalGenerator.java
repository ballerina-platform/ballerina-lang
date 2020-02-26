package generator;

import generator.util.Common;
import generator.util.Constants;
import model.Field;
import model.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class InternalGenerator {

    private static List<Node> nodes;

    public static void generateInternalTree() throws IOException {
        nodes = Common.getTreeNodes();
        for (Node node : nodes) {
            String internalClassString = new String(Files.readAllBytes(
                    Paths.get("compiler/ast-node-gen/InternalTemplateClass.txt")));
            internalClassString = internalClassString.replace(Constants.PACKAGE_PLACEHOLDER,
                    Constants.INTERNAL_PACKAGE);
            internalClassString = internalClassString.replace(Constants.IMPORTS_PLACEHOLDER,
                    Constants.INTERNAL_IMPORTS);
            internalClassString = Common.buildClassName(node, internalClassString);
            internalClassString = internalClassString.replace(Constants.ATTRIBUTES_PLACEHOLDER,
                    defineInternalAttributes(node.getFields()));
            internalClassString = buildParameterizedConstructor(internalClassString, node.getFields());
            internalClassString = internalClassString.replace(Constants.ADD_CHILD_NODE_PLACEHOLDER,
                    getChildNodeCall(node.getFields()));
            if (node.getBase() != null) {
                Node parentNode = getImmediateParentNode(node.getBase());
                if (parentNode != null) {
                    internalClassString = internalClassString.replace(Constants.IMMEDIATE_PARENT_PLACEHOLDER,
                            superConstructorParentValues(parentNode.getFields()));
                } else {
                    internalClassString = internalClassString.replace(Constants.IMMEDIATE_PARENT_PLACEHOLDER,
                            "");
                }
            }
            if (node.getFields() != null) {
                internalClassString = internalClassString.replace(Constants.BUCKET_COUNT_PLACEHOLDER,
                        String.valueOf(node.getFields().size()));
            } else {
                internalClassString = internalClassString.replace(Constants.BUCKET_CODE_PLACEHOLDER, "");
            }
            internalClassString = internalClassString.replace(Constants.TO_STRING_FUNCTION_PLACEHOLDER,
                    createToString(node, node.getFields()));
            if ((node.getType() != null) && (node.getType().equals(Constants.ABSTRACT_KEYWORD))) {
                internalClassString = internalClassString.replace(Constants.FACADE_FUNCTION_PLACEHOLDER,
                        "");
            } else {
                internalClassString = internalClassString.replace(Constants.FACADE_FUNCTION_PLACEHOLDER,
                        getFacadeFunction(node));
            }
            Common.writeToFile(internalClassString, "compiler/ast-node-gen/src/main/java/generated/internal/"
                    + node.getName() + Constants.DOT + Constants.JAVA_EXT);
        }
    }

//    private static String generateImports(String nodeName) {
//        return Constants.INTERNAL_IMPORTS.replace(Constants.BL_NODE_PLACEHOLDER, Constants.BL + nodeName);
//    }

    public static String defineInternalAttributes(List<Field> fieldList) {
        if (fieldList != null) {
            StringBuilder attributes = new StringBuilder();
            for (Field field : fieldList) {
                attributes.append(Constants.INTERNAL_ATTRIBUTE_VISIBILITY).append(Constants.WHITE_SPACE)
                        .append(Constants.FINAL_KEYWORD).append(Constants.WHITE_SPACE).append(field.getType())
                        .append(Constants.WHITE_SPACE).append(field.getName());
                if (field.getDefaultValue() != null) {
                    attributes.append(Constants.ASSIGNMENT_OPERATOR).append(field.getDefaultValue());
                }
                attributes.append(Constants.SEMI_COLON).append(Constants.NEW_LINE);
            }
            return attributes.toString();
        } else {
            return "";
        }
    }

    private static String buildParameterizedConstructor(String classString, List<Field> fieldList) {
        if (fieldList == null) {
            return classString.replace(Constants.PRAM_INIT_PLACEHOLDER, "")
                    .replace(Constants.PRAM_LIST_PLACEHOLDER, "");
        }
        StringBuilder paramList = new StringBuilder();
        StringBuilder initValues = new StringBuilder();
        if (fieldList.get(0).getDefaultValue() == null) {
            paramList.append(Constants.PARAMETER_SEPARATOR);
        }
        int i = 0;
        for (Field field : fieldList) {
            if (field.getDefaultValue() != null) {
                continue;
            }
            initValues.append(Constants.THIS_KEYWORD).append(Constants.DOT).append(field.getName())
                    .append(Constants.ASSIGNMENT_OPERATOR).append(field.getName()).append(Constants.SEMI_COLON);
            paramList.append(field.getType()).append(Constants.WHITE_SPACE).append(field.getName());
            i++;
            if (i != fieldList.size()) {
                paramList.append(Constants.PARAMETER_SEPARATOR);
                initValues.append(Constants.NEW_LINE);
            }
        }
        return classString.replace(Constants.PRAM_INIT_PLACEHOLDER, initValues)
                .replace(Constants.PRAM_LIST_PLACEHOLDER, paramList);
    }

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
                childNodeCall.append(Constants.THIS_KEYWORD).append(Constants.DOT)
                        .append(Constants.CHILD_NODE_FUNCTION).append(Constants.OPEN_BRACKETS).append(field.getName())
                        .append(Constants.PARAMETER_SEPARATOR).append(i).append(Constants.CLOSE_BRACKETS)
                        .append(Constants.SEMI_COLON).append(Constants.NEW_LINE);
            i++;
        }
        return childNodeCall.toString();
    }

    private static Node getImmediateParentNode(String ext) {
        for (Node node : nodes) {
            if (ext == null) {
                return null;
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
        fields.forEach(field -> values.append(Constants.PARAMETER_SEPARATOR).append(Constants.NULL_KEYWORD));
        return values.toString();
    }

    private static String createToString(Node node, List<Field> fieldList) {
        String returnStatement;
        if (node.getName().equals(Constants.MISSING_TOKEN)) {
            return "";
        } else if (node.getName().equals(Constants.SYNTAX_TOKEN)) {
            returnStatement = Constants.RETURN_KEYWORD + Constants.WHITE_SPACE + Constants.LEADING_TRIVIA
                    + Constants.CONCAT_SYMBOL + Constants.KIND_PROPERTY + Constants.CONCAT_SYMBOL
                    + Constants.TRAILING_TRIVIA;
        } else if (node.getBase().equals(Constants.SYNTAX_TOKEN)) {
            returnStatement = Constants.RETURN_KEYWORD + Constants.WHITE_SPACE + Constants.LEADING_TRIVIA
                    + Constants.CONCAT_SYMBOL + Constants.PROPERTY + Constants.CONCAT_SYMBOL
                    + Constants.TRAILING_TRIVIA;
        } else {
            return "";
        }
        return Constants.PUBLIC_KEYWORD + Constants.WHITE_SPACE + Constants.STRING_KEYWORD + Constants.WHITE_SPACE
                + Constants.TO_STRING_SIGNATURE + Constants.OPEN_BRACKETS + Constants.CLOSE_BRACKETS
                + Constants.OPEN_PARENTHESIS + Constants.NEW_LINE + returnStatement + Constants.SEMI_COLON
                + Constants.NEW_LINE + Constants.CLOSE_PARENTHESIS;
    }

    private static String getFacadeFunction(Node node) {
        if (!node.getBase().equals(Constants.SYNTAX_TOKEN) || node.getName().contains(Constants.SYNTAX_TOKEN)) {
//            public BLNode createFacade(int position, BLNonTerminalNode parent) {
//                  return new BL$className(this, position, parent);
//            }
            return Constants.FACADE_FUNCTION.replace(Constants.FACADE_RETURN_STATEMENT_PLACEHOLDER,
                    Constants.FACADE_RETURN_STATEMENT).replace(Constants.CLASSNAME_PLACEHOLDER, node.getName());
        } else {
            return "";
        }
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

import org.bytedeco.llvm.global.LLVM;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

/**
 * test.
 */
public class BindingGenerator {

    private static final String KEYWORDS[] = {
            "abstract", "as", "assert", "boolean", "break", "byte", "case",
            "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof",
            "int", "interface", "long", "native", "new",
            "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "void", "volatile",
            "while"
    };

    private static final String LICENSES = "// Copyright (c) 2018 WSO2 Inc. " +
                                           "(http://www.wso2.org) All Rights Reserved.\n" +
                                           "//\n" +
                                           "// WSO2 Inc. licenses this file to you under the Apache License,\n" +
                                           "// Version 2.0 (the \"License\"); you may not use this file except\n" +
                                           "// in compliance with the License.\n" +
                                           "// You may obtain a copy of the License at\n" +
                                           "//\n" +
                                           "// http://www.apache.org/licenses/LICENSE-2.0\n" +
                                           "//\n" +
                                           "// Unless required by applicable law or agreed to in writing,\n" +
                                           "// software distributed under the License is distributed on an\n" +
                                           "// \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                                           "// KIND, either express or implied.  See the License for the\n" +
                                           "// specific language governing permissions and limitations\n" +
                                           "// under the License.\n" +
                                           "\n";

    private static final String CLASS1 = LICENSES +
                                         "package org.ballerinalang.nativeimpl.llvm.gen;\n" +
                                         "\n" +
                                         "import org.ballerinalang.bre.Context;\n" +
                                         "import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;\n" +
                                         "import org.ballerinalang.model.values.BInteger;\n" +
                                         "import org.ballerinalang.model.values.BMap;\n" +
                                         "import org.ballerinalang.model.values.BValue;\n" +
                                         "import org.ballerinalang.nativeimpl.llvm.FFIUtil;\n" +
                                         "import org.ballerinalang.natives.annotations.Argument;\n" +
                                         "import org.ballerinalang.natives.annotations.BallerinaFunction;\n" +
                                         "import org.ballerinalang.natives.annotations.ReturnType;\n" +
                                         "import org.bytedeco.javacpp.BytePointer;\n" +
                                         "import org.bytedeco.javacpp.IntPointer;\n" +
                                         "import org.bytedeco.LLVM*;\n" +
                                         "import org.bytedeco.llvm.LLVM;\n" +
                                         "import org.bytedeco.javacpp.Pointer;\n" +
                                         "import org.bytedeco.javacpp.PointerPointer;\n" +
                                         "import org.bytedeco.javacpp.SizeTPointer;\n" +
                                         "\n" +
                                         "import java.nio.ByteBuffer;\n" +
                                         "\n" +
                                         "import static org.ballerinalang.model.types.TypeKind.*;\n" +
                                         "import static org.bytedeco.LLVM%s;\n" +
                                         "\n" +
                                         "/**\n" +
                                         " * Auto generated class.\n" +
                                         " */\n" +
                                         "@BallerinaFunction(\n" +
                                         "        orgName = \"ballerina\", packageName = \"llvm\",\n" +
                                         "        functionName = \"%s\"";

    private static final String CLASS2 = ")\n" +
                                         "public class %s extends BlockingNativeCallableUnit {\n" +
                                         "\n" +
                                         "    @Override\n" +
                                         "    public void execute(Context context) {\n";

    private static final String CLASS3 = "    }\n" +
                                         "}\n";
    private static final String RECODE_BODY = "%s %s = FFIUtil.getRecodeArgumentNative(context, %d);\n";
    private static final String RETURN_RECODE = "BMap<String, BValue> rerunWrapperRecode =" +
                                                " FFIUtil.newRecord(context, \"%s\");\n" +
                                                "FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);\n" +
                                                "context.setReturnValues(rerunWrapperRecode);\n";
    private static final String GEN_DIR = "stdlib/llvm/src/main/java/org/ballerinalang/nativeimpl/llvm/gen/";
    private static final String[] UNSUPPORTED_ARGS = new String[]{
            "ByteBuffer",
            "BytePointer",
            "IntBuffer",
            "IntPointer",
            "LongBuffer",
            "LongPointer"};
    private static final int STRING_REG = 0;
    private static final int INT_REG = 2;
    private static final int REG_REG = 3;

    private static final Set<String> types = new TreeSet<>();

    public static void main(String[] args) throws IOException {
        Class<LLVM> c = LLVM.class;
        StringBuilder declarations = new StringBuilder();
        Map<String, Integer> names = new HashMap<>();
        Map<String, Boolean> isOverloaded = new HashMap<>();
        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods) {
            if (hasUnusableArgs(method)) {
                continue;
            }
            String name = method.getName();
            isOverloaded.compute(name, (key, val) -> val != null);
        }
        for (Method method : methods) {
            String name = method.getName();
            if (!method.getName().startsWith("LLVM") || hasUnusableArgs(method)) {
                writeCommentedSig(method, name, declarations);
                continue;
            }


            if (isOverloaded.get(name)) {
                Integer id = names.compute(name, (key, val) -> val == null ? 0 : val + 1);
                name = name + id;
            }
            try {
                genBody(method, name);
                writeSig(method, name, declarations);
            } catch (Exception e) {
                writeCommentedSig(method, name, declarations);
            }

        }
        PrintWriter declarationsFile = new PrintWriter("stdlib/llvm/src/main/ballerina/llvm/staticfunc.bal");
        declarationsFile.write(LICENSES);
        declarationsFile.write(declarations.toString());
        declarationsFile.close();

        PrintWriter typesFile = new PrintWriter("stdlib/llvm/src/main/ballerina/llvm/types.bal");
        typesFile.write(LICENSES);
        for (String type : types) {
            typesFile.write("public type " + type + " record {};\n");
        }
        typesFile.close();
    }

    private static boolean hasUnusableArgs(Method method) {
        return Arrays.stream(method.getParameterTypes())
                .anyMatch(p -> Arrays.binarySearch(UNSUPPORTED_ARGS, p.getSimpleName()) >= 0);
    }

    private static void genBody(Method method, String name) throws IOException {
        Files.createDirectories(Paths.get(GEN_DIR));
        String fileName = GEN_DIR + name + ".java";
        try (PrintWriter writer = new PrintWriter(fileName)) {

            writer.write(String.format(CLASS1, method.getName(), name));
            StringBuilder body = new StringBuilder();
            Parameter[] parameters = method.getParameters();
            if (parameters.length > 0) {
                writer.write(",\n        args = {\n");
                int[] regs = new int[8];
                for (Parameter parameter : parameters) {
                    Class<?> paramType = parameter.getType();
                    String paramName = toVarName(parameter.getName());
                    writeArgs(writer, paramType, paramName);
                    createBody(body, regs, paramType, paramName);
                }
                writer.write("        }");
            }
            if (method.getReturnType() != Void.TYPE) {
                writeReturn(writer, method.getReturnType());
            }

            writer.write(String.format(CLASS2, name));
            writer.write(body.toString());
            writeMethodCall(method, writer);
            writer.write(CLASS3);
        } catch (Exception e) {
            Files.deleteIfExists(Paths.get(fileName));
            throw e;
        }
    }

    private static void writeMethodCall(Method method, PrintWriter writer) {
        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE) {
            writer.write(returnType.getSimpleName());
            writer.write(" returnValue = ");
        }

        writer.write(method.getName());
        writer.write("(");
        StringJoiner callParams = new StringJoiner(", ");
        for (Parameter parameter : method.getParameters()) {
            callParams.add(toVarName(parameter.getName()));
        }
        writer.write(callParams.toString());
        writer.write(");\n");

        if (returnType == Integer.TYPE) {
            writer.write("context.setReturnValues(new BInteger(returnValue));");
        } else if (returnType == Long.TYPE) {
            writer.write("context.setReturnValues(new BInteger(returnValue));");
        } else if (!returnType.isPrimitive()) {
            writer.write(String.format(RETURN_RECODE, returnType.getSimpleName()));

        }
    }

    private static void writeReturn(PrintWriter writer, Class<?> parameter) {

        writer.write(",\n        returnType = {\n" +
                     "                @ReturnType(");

        writeTypeAnnotation(writer, parameter);
        writer.write(", structPackage = \"ballerina/llvm\"),\n" +
                     "        }\n");

    }

    private static void writeArgs(PrintWriter writer, Class<?> paramType, String paramName) {
        writer.write("                @Argument(name = \"");
        writer.write(paramName);
        writer.write("\", ");
        writeTypeAnnotation(writer, paramType);
        writer.write("),\n");

    }

    private static void writeTypeAnnotation(PrintWriter writer, Class<?> type) {
        writer.write("type = ");
        if (type == String.class) {
            writer.write("STRING");
        } else if (type == Integer.TYPE) {
            writer.write("INT");
        } else if (type == Long.TYPE) {
            writer.write("INT");
        } else if (type == byte[].class || type == int[].class || type == long[].class) {
            throw new RuntimeException("array arg type not supported yet");
        } else if (!type.isPrimitive()) {
            types.add(type.getSimpleName());
            writer.write("RECORD, structType = \"");
            String typeName = type.getSimpleName();
            writer.write(typeName);
            writer.write("\"");
        } else {
            throw new RuntimeException("can't convert arg of type " + type.getSimpleName());
        }
    }

    private static void createBody(StringBuilder body, int[] regs, Class<?> paramType, String paramName) {
        if (paramType == String.class) {
            body.append(String.format("String %s = context.getStringArgument(%d);\n", paramName, regs[STRING_REG]++));
        } else if (paramType == Integer.TYPE) {
            body.append(String.format("int %s = (int) context.getIntArgument(%d);\n", paramName, regs[INT_REG]++));
        } else if (paramType == Long.TYPE) {
            body.append(String.format("long %s = context.getIntArgument(%d);\n", paramName, regs[INT_REG]++));
        } else if (paramType == byte[].class || paramType == int[].class || paramType == long[].class) {
            throw new RuntimeException("array arg type not supported yet");
        } else if (!paramType.isPrimitive()) {
            String typeName = paramType.getSimpleName();
            if (paramType.getName().startsWith("org.bytedeco.llvm.LLVM")) {
                typeName = "LLVM." + typeName;
            }
            body.append(String.format(RECODE_BODY, typeName, paramName, regs[REG_REG]++));
        } else {
            throw new RuntimeException("can't convert arg of type " + paramType.getSimpleName());
        }
    }

    private static void writeCommentedSig(Method method, String javaName, StringBuilder out) {
        out.append("//");
        writeSig(method, javaName, out);
    }

    private static void writeSig(Method method, String javaName, StringBuilder out) {
        out.append("public extern function ");
        out.append(javaName);
        out.append("(");
        out.append(genParmList(method));
        out.append(")");


        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            out.append(" returns ");
            out.append(toBalType(returnType));
        }

        out.append(";\n");
    }

    private static String genParmList(Method method) {
        StringJoiner sj = new StringJoiner(", ");
        for (Parameter parameter : method.getParameters()) {
            sj.add(toBalType(parameter.getType()) + " " + toVarName(parameter.getName()));
        }
        return sj.toString();
    }

    private static String toBalType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Long.TYPE) {
            return "int";
        }

        return type.getSimpleName();
    }


    private static String toVarName(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        char chars[] = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c) && (i == 0 || i == chars.length - 1 || Character.isUpperCase(chars[i + 1]))) {
                chars[i] = Character.toLowerCase(c);
            } else {
                break;
            }
        }
        String deCapitalized = new String(chars);
        if (Arrays.binarySearch(KEYWORDS, deCapitalized) >= 0) {
            return deCapitalized + "Value";
        }
        return deCapitalized;
    }
}

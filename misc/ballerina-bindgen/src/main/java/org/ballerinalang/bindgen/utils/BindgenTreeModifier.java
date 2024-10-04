package org.ballerinalang.bindgen.utils;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.BFunction;
import org.ballerinalang.bindgen.model.JClass;
import org.ballerinalang.bindgen.model.JConstructor;
import org.ballerinalang.bindgen.model.JField;
import org.ballerinalang.bindgen.model.JMethod;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_RESERVED_WORDS;
import static org.ballerinalang.bindgen.utils.BindgenNodeFactory.createFunctionDefinitionNode;
import static org.ballerinalang.bindgen.utils.BindgenNodeFactory.createTypeReferenceNode;

/**
 * Class for traversing the Ballerina syntax tree used for creating the bindings.
 *
 * @since 2.0.0
 */
public class BindgenTreeModifier {

    private final JClass jClass;
    public final BindgenEnv env;
    private static final String CLASS_DEF_ERROR = "error: unable to locate the class definition in the syntax tree";

    BindgenTreeModifier(JClass jClass, BindgenEnv env) {
        this.jClass = jClass;
        this.env = env;
    }

    ModulePartNode transform(ModulePartNode modulePartNode) throws BindgenException {
        NodeList<ImportDeclarationNode> imports = modifyImportDeclarationNodes(modulePartNode.imports());
        NodeList<ModuleMemberDeclarationNode> members = modifyModuleMemberDeclarationNode(modulePartNode.members());
        Token eofToken = modulePartNode.eofToken();
        return modulePartNode.modify(imports, members, eofToken);
    }

    private NodeList<ImportDeclarationNode> modifyImportDeclarationNodes(NodeList<ImportDeclarationNode> imports) {
        if (imports == null) {
            return AbstractNodeFactory.createNodeList();
        }
        if (jClass.isImportJavaArraysModule()) {
            ImportDeclarationNode jArraysImport = BindgenNodeFactory.createImportDeclarationNode("ballerina",
                    "jarrays", new LinkedList<>(Arrays.asList("jballerina", ".", "java", ".", "arrays")));
            imports = imports.add(jArraysImport);
        }
        if (env.getModulesFlag() && env.getPackageName() != null) {
            for (String packageName : jClass.getImportedPackages()) {
                ImportDeclarationNode packageImport = BindgenNodeFactory
                        .createImportDeclarationNode(null,
                                packageName.replace(".", ""),
                                new LinkedList<>(Collections.singletonList(escapeName(env.getPackageName()) + "."
                                        + processModuleName(packageName))));
                imports = imports.add(packageImport);
            }
        }
        return imports;
    }

    private NodeList<ModuleMemberDeclarationNode> modifyModuleMemberDeclarationNode(
            NodeList<ModuleMemberDeclarationNode> members) throws BindgenException {
        members = updateTypeReferences(members);
        members = updateInstanceMethods(members);
        members = updateInstanceFields(members);
        members = updateConstructors(members);
        members = updateStaticMethods(members);
        members = updateStaticFields(members);
        members = updateExternalMethods(members);

        return members;

    }

    private NodeList<ModuleMemberDeclarationNode> updateTypeReferences(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<TypeReferenceNode> typeReferences = new LinkedList<>();

        AbstractMap.SimpleEntry<Integer, ClassDefinitionNode> classDefinitionDetails = retrieveClassDefinition(members);
        if (classDefinitionDetails == null) {
            throw new BindgenException(CLASS_DEF_ERROR);
        }
        NodeList<Node> classDefinitionMembers = classDefinitionDetails.getValue().members();

        // Retrieve the existing function definition nodes inside the class definition
        for (Node node : classDefinitionMembers) {
            if (node.kind() == SyntaxKind.TYPE_REFERENCE) {
                typeReferences.add((TypeReferenceNode) node);
            }
        }

        ClassDefinitionNode modifiedClassDefinition = classDefinitionDetails.getValue();
        NodeList<Node> memberList = modifiedClassDefinition.members();
        NodeList<Node> newMemberList = AbstractNodeFactory.createEmptyNodeList();
        List<Node> newTypeReferences = new LinkedList<>();
        for (Map.Entry<String, String> superClass : jClass.getSuperClassPackage().entrySet()) {
            String completeSuperClassName = superClass.getKey();
            if (env.getModulesFlag() && (!superClass.getValue().equals(jClass.getPackageName().replace(".", "")))) {
                completeSuperClassName = superClass.getValue() + ":" + superClass.getKey();
            }
            if (!isFieldExists(completeSuperClassName, typeReferences)) {
                TypeReferenceNode typeReferenceNode = generateTypeReference(completeSuperClassName);
                if (typeReferenceNode != null) {
                    newTypeReferences.add(typeReferenceNode);
                }
            }
        }
        for (Node node : memberList) {
            newMemberList = newMemberList.add(node);
            if (node.kind() == SyntaxKind.TYPE_REFERENCE) {
                newMemberList = newMemberList.addAll(newTypeReferences);
            }
        }
        modifiedClassDefinition = modifiedClassDefinition.modify().withMembers(newMemberList).apply();
        return members.set(classDefinitionDetails.getKey(), modifiedClassDefinition);
    }

    private NodeList<ModuleMemberDeclarationNode> updateConstructors(NodeList<ModuleMemberDeclarationNode> members) {
        List<FunctionDefinitionNode> constructorFunctions = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                constructorFunctions.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JConstructor jConstructor : jClass.getConstructorList()) {
            if (!isFunctionExists(jConstructor.getFunctionName(), constructorFunctions)) {
                FunctionDefinitionNode staticMethod = generateBalFunction(jConstructor, false);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        return memberList;
    }

    private NodeList<ModuleMemberDeclarationNode> updateStaticMethods(NodeList<ModuleMemberDeclarationNode> members) {
        List<FunctionDefinitionNode> staticFunctions = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                staticFunctions.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JMethod jMethod : jClass.getMethodList()) {
            if (jMethod.isStatic() && !isFunctionExists(jMethod.getBalFunctionName(), staticFunctions)) {
                FunctionDefinitionNode staticMethod = generateBalFunction(jMethod, false);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        return memberList;
    }

    private NodeList<ModuleMemberDeclarationNode> updateInstanceMethods(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<FunctionDefinitionNode> instanceFunctions = new LinkedList<>();
        AbstractMap.SimpleEntry<Integer, ClassDefinitionNode> classDefinitionDetails = retrieveClassDefinition(members);
        if (classDefinitionDetails == null) {
            throw new BindgenException(CLASS_DEF_ERROR);
        }
        NodeList<Node> classDefinitionMembers = classDefinitionDetails.getValue().members();

        // Retrieve the existing function definition nodes inside the class definition
        for (Node node : classDefinitionMembers) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                instanceFunctions.add((FunctionDefinitionNode) node);
            }
        }

        ClassDefinitionNode modifiedClassDefinition = classDefinitionDetails.getValue();
        NodeList<Node> memberList = modifiedClassDefinition.members();
        for (JMethod jMethod : jClass.getMethodList()) {
            if (!jMethod.isStatic() && !isFunctionExists(jMethod.getBalFunctionName(), instanceFunctions)) {
                FunctionDefinitionNode instanceMethod = generateBalFunction(jMethod, false);
                if (instanceMethod != null) {
                    memberList = memberList.add(instanceMethod);
                }
            }
        }
        modifiedClassDefinition = modifiedClassDefinition.modify().withMembers(memberList).apply();
        return members.set(classDefinitionDetails.getKey(), modifiedClassDefinition);
    }

    private NodeList<ModuleMemberDeclarationNode> updateStaticFields(NodeList<ModuleMemberDeclarationNode> members) {
        List<FunctionDefinitionNode> staticFields = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                staticFields.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JField jField : jClass.getFieldList()) {
            if (jField.isStatic() && !isFunctionExists(jField.getFunctionName(), staticFields)) {
                FunctionDefinitionNode staticMethod = generateBalFunction(jField, false);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        return memberList;
    }

    private NodeList<ModuleMemberDeclarationNode> updateInstanceFields(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<FunctionDefinitionNode> instanceFields = new LinkedList<>();
        AbstractMap.SimpleEntry<Integer, ClassDefinitionNode> classDefinitionDetails = retrieveClassDefinition(members);
        if (classDefinitionDetails == null) {
            throw new BindgenException(CLASS_DEF_ERROR);
        }
        NodeList<Node> classDefinitionMembers = classDefinitionDetails.getValue().members();

        // Retrieve the existing function definition nodes inside the class definition
        for (Node node : classDefinitionMembers) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                instanceFields.add((FunctionDefinitionNode) node);
            }
        }

        ClassDefinitionNode modifiedClassDefinition = classDefinitionDetails.getValue();
        NodeList<Node> memberList = modifiedClassDefinition.members();
        for (JField jField : jClass.getFieldList()) {
            if (!jField.isStatic() && !isFunctionExists(jField.getFunctionName(), instanceFields)) {
                FunctionDefinitionNode instanceMethod = generateBalFunction(jField, false);
                if (instanceMethod != null) {
                    memberList = memberList.add(instanceMethod);
                }
            }
        }
        modifiedClassDefinition = modifiedClassDefinition.modify().withMembers(memberList).apply();
        return members.set(classDefinitionDetails.getKey(), modifiedClassDefinition);
    }

    private NodeList<ModuleMemberDeclarationNode> updateExternalMethods(NodeList<ModuleMemberDeclarationNode> members) {
        List<FunctionDefinitionNode> externalFunctions = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                externalFunctions.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JMethod jMethod : jClass.getMethodList()) {
            if (!isFunctionExists(jMethod.getBalFunctionName(), externalFunctions)) {
                FunctionDefinitionNode generatedFunction = generateBalFunction(jMethod, true);
                if (generatedFunction != null) {
                    memberList = memberList.add(generatedFunction);
                }
            }
        }
        for (JField jField : jClass.getFieldList()) {
            if (!isFunctionExists(jField.getFunctionName(), externalFunctions)) {
                FunctionDefinitionNode generatedFunction = generateBalFunction(jField, true);
                if (generatedFunction != null) {
                    memberList = memberList.add(generatedFunction);
                }
            }
        }
        for (JConstructor jConstructor : jClass.getConstructorList()) {
            if (!isFunctionExists(jConstructor.getExternalFunctionName(),
                    externalFunctions)) {
                FunctionDefinitionNode generatedFunction = generateBalFunction(jConstructor, true);
                if (generatedFunction != null) {
                    memberList = memberList.add(generatedFunction);
                }
            }
        }
        return memberList;
    }

    @Nullable
    private AbstractMap.SimpleEntry<Integer, ClassDefinitionNode> retrieveClassDefinition(
            NodeList<ModuleMemberDeclarationNode> members) {
        AbstractMap.SimpleEntry<Integer, ClassDefinitionNode> entry = null;
        for (int i = 0; i < members.size(); i++) {
            ModuleMemberDeclarationNode moduleMember = members.get(i);
            if (moduleMember.kind() == SyntaxKind.CLASS_DEFINITION) {
                ClassDefinitionNode classDefinition = (ClassDefinitionNode) moduleMember;
                if (classDefinition.className().text().equals(jClass.getShortClassName())) {
                    entry =  new AbstractMap.SimpleEntry<>(i, classDefinition);
                }
            }
        }
        return entry;
    }

    @Nullable
    private FunctionDefinitionNode generateBalFunction(BFunction bFunction, boolean isExternal) {
        try {
            return createFunctionDefinitionNode(bFunction, isExternal);
        } catch (BindgenException e) {
            env.setFailedMethodGens("error: unable to generate the binding function '" + bFunction.getFunctionName()
                    + "' of '" + bFunction.getDeclaringClass().getName() + "': "  + e.getMessage());
            return null;
        }
    }

    private TypeReferenceNode generateTypeReference(String type) {
        return createTypeReferenceNode(type);
    }

    private boolean isFieldExists(String fieldName, List<TypeReferenceNode> fieldList) {
        for (TypeReferenceNode memberNode : fieldList) {
            if (memberNode.typeName().toString().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFunctionExists(String functionName, List<FunctionDefinitionNode> functionList) {
        for (FunctionDefinitionNode memberNode : functionList) {
            if (memberNode.functionName().text().equals(functionName)) {
                return true;
            }
        }
        return false;
    }

    private String processModuleName(String packageName) {
        List<String> reservedWords = Arrays.asList(BALLERINA_RESERVED_WORDS);
        List<String> moduleName = new LinkedList<>();
        String[] components = packageName.split("\\.");
        for (String component : components) {
            if (reservedWords.contains(component)) {
                moduleName.add("'" + component);
            } else {
                moduleName.add(component);
            }
        }
        return String.join(".", moduleName);
    }

    private String escapeName(String name) {
        List<String> reservedWords = Arrays.asList(BALLERINA_RESERVED_WORDS);
        if (reservedWords.contains(name)) {
            name = "'" + name;
        }
        return name;
    }
}

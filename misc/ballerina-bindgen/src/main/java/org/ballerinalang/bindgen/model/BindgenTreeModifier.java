package org.ballerinalang.bindgen.model;

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
import org.ballerinalang.bindgen.utils.BindgenEnv;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.bindgen.model.BindgenNodeFactory.createFunctionDefinitionNode;
import static org.ballerinalang.bindgen.model.BindgenNodeFactory.createTypeReferenceNode;

/**
 * Class for traversing the Ballerina syntax tree used for creating the bindings.
 *
 * @since 2.0.0
 */
public class BindgenTreeModifier {

    public final JClass jClass;
    public final BindgenEnv env;

    public BindgenTreeModifier(JClass jClass, BindgenEnv env) {
        this.jClass = jClass;
        this.env = env;
    }

    public ModulePartNode transform(ModulePartNode modulePartNode) throws BindgenException {
        NodeList<ImportDeclarationNode> imports = modifyImportDeclarationNodes(modulePartNode.imports());
        NodeList<ModuleMemberDeclarationNode> members = modifyModuleMemberDeclarationNode(modulePartNode.members());
        Token eofToken = modulePartNode.eofToken();
        return modulePartNode.modify(imports, members, eofToken);
    }

    private NodeList<ImportDeclarationNode> modifyImportDeclarationNodes(NodeList<ImportDeclarationNode> imports) {
        if (jClass.isImportJavaArraysModule()) {
            ImportDeclarationNode jArraysImport = BindgenNodeFactory.createImportDeclarationNode("ballerina",
                    "jarrays", new LinkedList<>(Arrays.asList("java", ".", "arrays")));
            imports = imports.add(jArraysImport);
        }
        if (env.getModulesFlag() && env.getPackageName() != null) {
            for (String packageName : jClass.getImportedPackages()) {
                ImportDeclarationNode jArraysImport = BindgenNodeFactory
                        .createImportDeclarationNode(env.getPackageName(),
                                packageName.replace(".", ""),
                                new LinkedList<>(Collections.singletonList(packageName)));
                imports = imports.add(jArraysImport);
            }
        }
        return imports;
    }

    private NodeList<ModuleMemberDeclarationNode> modifyModuleMemberDeclarationNode(
            NodeList<ModuleMemberDeclarationNode> members) throws BindgenException {
        members = updateInstanceMethods(members);
        members = updateInstanceFields(members);
        members = updateConstructors(members);
        members = updateStaticMethods(members);
        members = updateStaticFields(members);
        members = updateExternalMethods(members);
        members = updateTypeReferences(members);

        return members;

    }

    private NodeList<ModuleMemberDeclarationNode> updateTypeReferences(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<TypeReferenceNode> typeReferences = new LinkedList<>();

        AbstractMap.SimpleEntry<Integer, ClassDefinitionNode> classDefinitionDetails = retrieveClassDefinition(members);
        if (classDefinitionDetails == null) {
            throw new BindgenException("error: unable to locate the class definition in the syntax tree");
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
        for (Map.Entry<String, String> superClass : jClass.getSuperClassPackage().entrySet()) {
            String completeSuperClassName = superClass.getKey();
            if (env.getModulesFlag()) {
                completeSuperClassName = superClass.getValue() + ":" + superClass.getKey();
            }
            if (!isFieldExists(completeSuperClassName, typeReferences)) {
                TypeReferenceNode typeReferenceNode = generateTypeReference(completeSuperClassName);
                if (typeReferenceNode != null) {
                    memberList = memberList.add(typeReferenceNode);
                }
            }
        }
        modifiedClassDefinition = modifiedClassDefinition.modify().withMembers(memberList).apply();
        return members.set(classDefinitionDetails.getKey(), modifiedClassDefinition);
    }

    private NodeList<ModuleMemberDeclarationNode> updateConstructors(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<FunctionDefinitionNode> constructorFunctions = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                constructorFunctions.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JConstructor jConstructor : jClass.getConstructorList()) {
            if (!isFunctionExists(jConstructor.getFunctionName(), constructorFunctions)) {
                FunctionDefinitionNode staticMethod = generateInstanceMethods(jConstructor, false);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        return memberList;
    }

    private NodeList<ModuleMemberDeclarationNode> updateStaticMethods(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<FunctionDefinitionNode> staticFunctions = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                staticFunctions.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JMethod jMethod : jClass.getMethodList()) {
            if (jMethod.isStatic() && !isFunctionExists(jMethod.getBalFunctionName(), staticFunctions)) {
                FunctionDefinitionNode staticMethod = generateInstanceMethods(jMethod, false);
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
            throw new BindgenException("error: unable to locate the class definition in the syntax tree");
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
                FunctionDefinitionNode instanceMethod = generateInstanceMethods(jMethod, false);
                if (instanceMethod != null) {
                    memberList = memberList.add(instanceMethod);
                }
            }
        }
        modifiedClassDefinition = modifiedClassDefinition.modify().withMembers(memberList).apply();
        return members.set(classDefinitionDetails.getKey(), modifiedClassDefinition);
    }

    private NodeList<ModuleMemberDeclarationNode> updateStaticFields(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<FunctionDefinitionNode> staticFields = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                staticFields.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JField jField : jClass.getFieldList()) {
            if (jField.isStatic() && !isFunctionExists(jField.getFunctionName(), staticFields)) {
                FunctionDefinitionNode staticMethod = generateInstanceMethods(jField, false);
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
            throw new BindgenException("error: unable to locate the class definition in the syntax tree");
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
                FunctionDefinitionNode instanceMethod = generateInstanceMethods(jField, false);
                if (instanceMethod != null) {
                    memberList = memberList.add(instanceMethod);
                }
            }
        }
        modifiedClassDefinition = modifiedClassDefinition.modify().withMembers(memberList).apply();
        return members.set(classDefinitionDetails.getKey(), modifiedClassDefinition);
    }

    private NodeList<ModuleMemberDeclarationNode> updateExternalMethods(NodeList<ModuleMemberDeclarationNode> members)
            throws BindgenException {
        List<FunctionDefinitionNode> externalFunctions = new LinkedList<>();

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                externalFunctions.add((FunctionDefinitionNode) node);
            }
        }

        NodeList<ModuleMemberDeclarationNode> memberList = members;
        for (JMethod jMethod : jClass.getMethodList()) {
            if (jMethod.isStatic() && !isFunctionExists(jMethod.getBalFunctionName(), externalFunctions)) {
                FunctionDefinitionNode staticMethod = generateInstanceMethods(jMethod, true);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        for (JField jField : jClass.getFieldList()) {
            if (jField.isStatic() && !isFunctionExists(jField.getFunctionName(), externalFunctions)) {
                FunctionDefinitionNode staticMethod = generateInstanceMethods(jField, true);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        for (JConstructor jConstructor : jClass.getConstructorList()) {
            if (jConstructor.isStatic() && !isFunctionExists(jConstructor.getFunctionName(), externalFunctions)) {
                FunctionDefinitionNode staticMethod = generateInstanceMethods(jConstructor, true);
                if (staticMethod != null) {
                    memberList = memberList.add(staticMethod);
                }
            }
        }
        return memberList;
    }

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

    private FunctionDefinitionNode generateInstanceMethods(BFunction bFunction, boolean isExternal)
            throws BindgenException {
        return createFunctionDefinitionNode(bFunction, isExternal);
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
}

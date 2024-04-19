/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.semtype.port.test;

import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.types.Context;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.testng.Assert;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Extract semtype assertions in the below form.
 * // @type A < B
 * // @type B = C
 * // @type c <> D
 *
 * @since 3.0.0
 */
public class SemTypeAssertionTransformer extends NodeVisitor {
    private final String fileName;
    private final SyntaxTree syntaxTree;
    private final Env semtypeEnv;
    private final Context context;
    private final List<String> list;

    private SemTypeAssertionTransformer(String fileName, SyntaxTree syntaxTree, Env semtypeEnv) {
        this.fileName = fileName;
        this.syntaxTree = syntaxTree;
        this.semtypeEnv = semtypeEnv;
        this.context = Context.from(semtypeEnv);
        list = new ArrayList<>();
    }

    public static List<TypeAssertion> getTypeAssertionsFrom(String fileName, SyntaxTree syntaxTree, Env semtypeEnv) {
        final SemTypeAssertionTransformer t = new SemTypeAssertionTransformer(fileName, syntaxTree, semtypeEnv);
        return t.getTypeAssertions();
    }

    private List<TypeAssertion> getTypeAssertions() {
        syntaxTree.rootNode().accept(this);
        List<TypeAssertion> assertions = new ArrayList<>();
        for (String str : list) {
            String[] parts = splitAssertion(str);
            if (parts == null) {
                continue;
            }
            SemType lhs = toSemType(parts[0]);
            RelKind kind = RelKind.fromString(parts[1], str);
            SemType rhs = toSemType(parts[2]);
            String text = parts[0] + " " + parts[1] + " " + parts[2];
            assertions.add(new TypeAssertion(this.context, this.fileName, lhs, rhs, kind, text));
        }
        return assertions;
    }

    private SemType toSemType(String typeExpr) {
        // Simple type reference
        int leftBracketPos = typeExpr.indexOf('[');
        final Map<String, SemType> typeNameSemTypeMap = semtypeEnv.getTypeNameSemTypeMap();
        if (leftBracketPos == -1) {
            return typeNameSemTypeMap.get(typeExpr);
        }
        int rightBracketPos = typeExpr.indexOf(']');
        String typeRef = typeExpr.substring(0, leftBracketPos);
        String memberAccessExpr = typeExpr.substring(leftBracketPos + 1, rightBracketPos);

        SemType type = typeNameSemTypeMap.get(typeRef);
        if (SemTypes.isSubtypeSimple(type, PredefinedType.LIST)) {
            SemType m;
            try {
                long l = Long.parseLong(memberAccessExpr);
                m = SemTypes.intConst(l);
            } catch (Exception e) {
                // parsing number failed, access must be a type-reference
                m = typeNameSemTypeMap.get(memberAccessExpr);
            }
            return listProj(context, type, m);
        } else if (SemTypes.isSubtypeSimple(type, PredefinedType.MAPPING)) {
            SemType m = typeNameSemTypeMap.get(memberAccessExpr);
            return SemTypes.mappingMemberTypeInnerVal(context, type, m);
        }
        throw new IllegalStateException("Unsupported type test: " + typeExpr);
    }

    private SemType listProj(Context context, SemType t, SemType m) {
        SemType s1 = SemTypes.listProj(context, t, m);
        SemType s2 = SemTypes.listMemberType(context, t, m);
        if (!SemTypes.isSubtype(context, s1, s2)) {
            Assert.fail("listProj result is not a subtype of listMemberType");
        }
        return s1;
    }

    private String[] splitAssertion(String str) {
        if (ignoredCommentSet().contains(str)) {
            return null;
        }
        String[] parts = str.split(" ");

        if (parts[1].equals("-@type")) {
            // TODO: remove this check once diff operator is supported
            return null;
        }

        // Only accept the form: `//` `@type` T1 REL T2
        if (!parts[1].equals("@type") || parts.length != 5) {
            throw new IllegalStateException("Invalid type assertion '" + str +
                    "', expected in form: '// @type T1 REL T2'");
        }
        return Arrays.copyOfRange(parts, 2, 5);
    }

    /**
     * Returns a set of comments that are to be ignored during the processing.
     * These comments include non-essential information or comments that do not conform to the expected format.
     *
     * @return Set of comments to be ignored.
     */
    public final HashSet<String> ignoredCommentSet() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("// the order of type defns are intentional");
        return hashSet;
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        addComments(node.leadingMinutiae());
        addComments(node.trailingMinutiae());
    }

    @Override
    public void visit(Token token) {
        addComments(token.leadingMinutiae());
        addComments(token.trailingMinutiae());
    }

    private void addComments(MinutiaeList minutiaes) {
        for (Minutiae minutiae : minutiaes) {
            if (minutiae.kind() == SyntaxKind.COMMENT_MINUTIAE) {
                String text = minutiae.text();
                if (text.length() > 2) {
                    list.add(text);
                }
            }
        }
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {
        for (ModuleMemberDeclarationNode member : modulePartNode.members()) {
            member.accept(this);
        }
        modulePartNode.eofToken().accept(this);
    }

    /**
     * Subtype test.
     *
     * @param context  Type context under which {@code SemTypes} were defined.
     * @param fileName Name of the file in which types were defined in.
     * @param lhs      Resolved {@code SemType} for the Left-hand side of the subtype test.
     * @param rhs      Resolved {@code SemType} for the Right-hand side of the subtype test.
     * @param kind     Relationship between the two types.
     * @param text     Text that will be shown in case of assertion failure.
     * @since 3.0.0
     */
    record TypeAssertion(Context context, String fileName, SemType lhs, SemType rhs, RelKind kind, String text) {

        TypeAssertion {
            assert lhs != null;
            assert rhs != null;
        }

        @Override
        public String toString() {
            return Paths.get(fileName).getFileName().toString() + ": " + text;
        }
    }

    /**
     * Relationship to be asserted.
     *
     * @since 3.0.0
     */
    public enum RelKind {
        SUB, SAME, NON;

        static RelKind fromString(String rel, String assertion) {
            switch (rel) {
                case "<":
                    return SUB;
                case "=":
                    return SAME;
                case "<>":
                    return NON;
                default:
                    throw new AssertionError("Unknown type relationship in assertion: " + rel + "in: " + assertion);
            }
        }
    }
}

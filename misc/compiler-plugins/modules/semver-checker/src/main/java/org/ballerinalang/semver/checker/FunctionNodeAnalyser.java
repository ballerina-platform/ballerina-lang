package org.ballerinalang.semver.checker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import org.ballerinalang.semver.checker.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class FunctionNodeAnalyser  implements AnalysisTask<SyntaxNodeAnalysisContext> {

    static JsonArray array = new JsonArray();
    JsonObject object = new JsonObject();
    String string;
    JsonArray arr = new JsonArray();
    static ArrayList<SyntaxTree> syn = new ArrayList<SyntaxTree>();
    public FunctionNodeAnalyser(ArrayList<SyntaxTree> syn) {
        this.syn = syn;

    }

    public static JsonArray getArray() {
        return array;
    }
    public static ArrayList<SyntaxTree> getList() {
        return syn;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        ModulePartNode node = (ModulePartNode) syntaxNodeAnalysisContext.node();
        NodeList<ModuleMemberDeclarationNode> members = node.members();
        String s2 = null;
        File file = new File("filename.json");
        if (!file.exists()) {
        for (ModuleMemberDeclarationNode item : members
        ) {
            if (item.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                    FunctionDefinitionNode item1 = (FunctionDefinitionNode) item;
                    FunctionBodyNode nod = item1.functionBody();
                    syn.add(item1.syntaxTree());
                    String s11 = item1.toSourceCode();
                    String s22 = nod.toSourceCode();
                    String st = s11.replace(s22, "");
                    FunctionBodyBlockNode nodd = (FunctionBodyBlockNode) item1.functionBody();
                    NodeList<StatementNode> statements = nodd.statements();
                    NodeList<StatementNode> emptyNodeList = AbstractNodeFactory.createEmptyNodeList();
                    FunctionBodyBlockNode modify = nodd.modify(nodd.openBraceToken(), nodd.namedWorkerDeclarator().orElse(null), emptyNodeList, nodd.closeBraceToken());
                    MetadataNode meta = item1.metadata().orElse(null);
                    FunctionDefinitionNode modify1 = item1.modify(item1.kind(), meta, item1.qualifierList(), item1.functionKeyword(), item1.functionName(), item1.relativeResourcePath(), item1.functionSignature(), modify);
                    SyntaxTree syntaxTree = modify1.syntaxTree();
                    String identifierToken = String.valueOf(item1.functionName());
                    object.addProperty(identifierToken,String.valueOf(syntaxTree));
                    arr.add(String.valueOf(syntaxTree));
                    syn.add(syntaxTree);
                    String functionNode = modify1.toSourceCode();

                    String s111 = modify1.toSourceCode();
                    String[] lines = functionNode.split("\\{");
                    String line = lines[0];
                    String signature = line.replaceAll("\n", "");
                    String[] line1 = line.split("\n");
                    array.add(signature);
                }
            }
            String ss = syntaxNodeAnalysisContext.currentPackage().packageVersion().toString();
            FileUtils.createFile(arr, ss);
        }
        else{
            for (ModuleMemberDeclarationNode item : members
            ) {
                if (item.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                    FunctionDefinitionNode no = (FunctionDefinitionNode) item;
                    IdentifierToken identifierToken = no.functionName();
                    SyntaxTree syntaxTree = no.syntaxTree();

                    JsonParser jsonP = new JsonParser();
                    Object obj = null;
                    JsonObject object = null;
                    try (FileReader reader = new FileReader("filename.json")) {
                        obj = jsonP.parse(reader);
                        String s = obj.toString();
                        object = (JsonObject) obj;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    JsonArray signature = (JsonArray) object.get("Signature");
                    JsonElement sy = signature.get(0);
                    System.out.println(sy);
                }

                }

        }


    }
    }


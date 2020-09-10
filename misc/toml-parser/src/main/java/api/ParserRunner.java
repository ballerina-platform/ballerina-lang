package api;

import internal.parser.ParserFactory;
import internal.parser.TomlParser;
import internal.parser.tree.STNode;
import sementic.analyser.TomlNodeTransformer;
import syntax.tree.KeyValue;
import syntax.tree.ModuleMemberDeclarationNode;
import syntax.tree.ModulePartNode;
import syntax.tree.Node;
import syntax.tree.NodeList;
import syntax.tree.SyntaxKind;
import syntax.tree.TableNode;
import syntax.tree.Token;

import java.util.HashMap;
import java.util.Map;

public class ParserRunner {

    private final ModulePartNode rootNode;

    public ParserRunner (String content){
        TomlParser parser = ParserFactory.getParser(content);
        STNode node = parser.parse();
        Node externalNode = node.createUnlinkedFacade();
        TomlNodeTransformer nodeTransformer = new TomlNodeTransformer();
         rootNode = (ModulePartNode) nodeTransformer.transform((ModulePartNode) externalNode);
    }

    public Map<String,Object> parse() {
        HashMap<String,Object> output = new HashMap<>();
        for (ModuleMemberDeclarationNode member:rootNode.members()){
            if (member.kind() == SyntaxKind.KEY_VALUE){
                Pair<String, Object> keyValuePair = parseKeyValue((KeyValue) member);
                output.put(keyValuePair.getFirst(),keyValuePair.getSecond());
            } else if (member.kind() == SyntaxKind.TABLE) {
                TableNode childNode = (TableNode) member;
                String identifier = childNode.identifier().text();
                HashMap<String, Object> childTableNode = parseTable(childNode);
                output.put(identifier,childTableNode);
            } else {

            }
        }
        return output;
    }

    private HashMap<String,Object> parseTable (TableNode tableNode){
        HashMap<String,Object> output = new HashMap<>();
        NodeList<Node> childs = tableNode.fields();
        for (Node child : childs) {
            SyntaxKind kind = child.kind();
            if (kind == SyntaxKind.KEY_VALUE){
                Pair<String, Object> keyValuePair = parseKeyValue((KeyValue) child);
                output.put(keyValuePair.getFirst(),keyValuePair.getSecond());
            } else if (kind == SyntaxKind.TABLE){
                TableNode childNode = (TableNode) child;
                String identifier = childNode.identifier().text();
                HashMap<String, Object> childTableNode = parseTable(childNode);
                output.put(identifier,childTableNode);
            } else {

            }
        }
        return output;
    }

    private Pair<String,Object> parseKeyValue (KeyValue keyValue){
        String key = keyValue.identifier().text();
        if (key.startsWith("\"") && key.endsWith("\"")){
            key = key.substring(1,key.length()-1);
        }
        Token value = keyValue.value();
        Object targetObject;
        SyntaxKind kind = value.kind();
        if (kind == SyntaxKind.DEC_INT){
            targetObject = Integer.parseInt(value.text());
        } else if (kind == SyntaxKind.FLOAT){
            targetObject = Double.parseDouble(value.text());
        } else if (kind == SyntaxKind.STRING_LITERAL){
            targetObject = value.text();
        } else {
            targetObject = null;
        }

        return new Pair<>(key,targetObject);
    }

}

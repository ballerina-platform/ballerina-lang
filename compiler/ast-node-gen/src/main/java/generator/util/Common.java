package generator.util;

import com.google.gson.Gson;
import model.Node;
import model.Tree;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Common {

    public static Map<String, String> buildClassName(Node node) throws IOException {
        Map<String, String > classMap = new HashMap<>();
        classMap.put(Constants.CLASSNAME_PLACEHOLDER, node.getName());
        if (node.getType()!= null) {
            classMap.put(Constants.ABSTRACT_PLACEHOLDER, node.getType());
        }
        // Check for parent classes
        if (node.getBase() != null) {
            classMap.put(Constants.RELATIONSHIP_PLACEHOLDER, Constants.EXTENDS_KEYWORD);
            classMap.put(Constants.PARENT_CLASS_PLACEHOLDER, node.getBase());
        }
        return classMap;
    }

    public static List<Node> getTreeNodes() throws IOException {
        Gson gson = new Gson();
        Tree ast = gson.fromJson(new FileReader("compiler/ast-node-gen/src/main/resources/newTree.json"),
                Tree.class);
        return ast.getNode();
    }

    public static void writeToFile(String data, String filePath) throws IOException {
        File file = new File(filePath);
        FileWriter fr = new FileWriter(file);
        fr.write(data);
        fr.close();
    }

    public static Node getImmediateParentNode(String ext, List<Node> nodes) {
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
}

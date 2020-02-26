package generator.util;

import com.google.gson.Gson;
import model.Node;
import model.Tree;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Common {

    public static String buildClassName(Node node, String classString) {
        if (node.getType() == null) {
            classString = classString.replace(Constants.ABSTRACT_PLACEHOLDER + Constants.WHITE_SPACE,
                    "").replace(Constants.CLASSNAME_PLACEHOLDER, node.getName())
                    .replace(Constants.TYPE_PLACEHOLDER, "");
        } else {
            classString = classString.replace(Constants.ABSTRACT_PLACEHOLDER, node.getType())
                    .replace(Constants.CLASSNAME_PLACEHOLDER, node.getName())
                    .replace(Constants.TYPE_PLACEHOLDER, "");
        }
        // Check for parent classes
        if (node.getExt() == null) {
            classString = classString.replace(Constants.RELATIONSHIP_PLACEHOLDER + Constants.WHITE_SPACE,
                    "").replace(Constants.PARENT_CLASS_PLACEHOLDER, "");
        } else {
            classString = classString.replace(Constants.RELATIONSHIP_PLACEHOLDER, Constants.EXTENDS_KEYWORD)
                    .replace(Constants.PARENT_CLASS_PLACEHOLDER, node.getExt());
        }
        System.out.println(classString);
        return classString;
    }

    public static List<Node> getTreeNodes() throws IOException {
        Gson gson = new Gson();
        Tree ast = gson.fromJson(new FileReader("compiler/ast-node-gen/newTree.json"), Tree.class);
        return ast.getNode();
    }

    public static void writeToFile(String data, String filePath) throws IOException {
        File file = new File(filePath);
        FileWriter fr = new FileWriter(file);
        fr.write(data);
        fr.close();
    }
}

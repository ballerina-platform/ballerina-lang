/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    public static List<Node> getTreeNodes(String nodeJsonPath) throws IOException {
        Gson gson = new Gson();
        Tree ast = gson.fromJson(new FileReader(nodeJsonPath),
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

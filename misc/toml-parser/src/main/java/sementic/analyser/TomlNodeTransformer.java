package sementic.analyser;

import syntax.tree.IdentifierToken;
import syntax.tree.KeyValue;
import syntax.tree.ModuleMemberDeclarationNode;
import syntax.tree.ModulePartNode;
import syntax.tree.Node;
import syntax.tree.NodeFactory;
import syntax.tree.NodeList;
import syntax.tree.NodeTransformer;
import syntax.tree.SyntaxKind;
import syntax.tree.TableArrayNode;
import syntax.tree.TableNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class TomlNodeTransformer extends NodeTransformer<Node> {

    @Override
    public Node transform(ModulePartNode modulePartNode) {
        modulePartNode = transformTopLevelNodes(modulePartNode);
        modulePartNode = fixChildNodes(modulePartNode);

        return modulePartNode;
    }

    private ModulePartNode transformTopLevelNodes(ModulePartNode modulePartNode) {
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (int i=0;i<members.size();i++){
            ModuleMemberDeclarationNode member = members.get(i);
            ModuleMemberDeclarationNode node = (ModuleMemberDeclarationNode) member.apply(this);
            members = members.set(i,node);
        }
        return modulePartNode.modify().withMembers(members).apply();
    }

    private ModulePartNode fixChildNodes (ModulePartNode modulePartNode){

        TreeSet<RelationIndex> relationship = new TreeSet<>();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (int i=0;i<members.size();i++){
            ModuleMemberDeclarationNode parentNode = members.get(i);
            if (parentNode.kind() == SyntaxKind.TABLE) {
                String parentKey = ((TableNode) parentNode).identifier().text();
                for (int j=0;j<members.size();j++){
                    if (i==j){
                        continue;
                    }
                    ModuleMemberDeclarationNode childNode = members.get(j);
                    if (childNode.kind() == SyntaxKind.TABLE) {
                        String childKey = ((TableNode) childNode).identifier().text();
                        if (isChildTable(parentKey, childKey)) {
                            relationship.add(new RelationIndex(i, j, childKey.split("\\.").length));
                        }
                    }
                }
            }
        }

        for (RelationIndex index:relationship){
            ModuleMemberDeclarationNode parentNode = members.get(index.getParentIndex());
            ModuleMemberDeclarationNode childNode = members.get(index.getChildIndex());
            NodeList<Node> modifiedParentNodeList = ((TableNode) parentNode).fields().add(childNode);
            TableNode modifiedParentTable = ((TableNode) parentNode).modify().withFields(modifiedParentNodeList).apply();
            members = members.set(index.getParentIndex(),modifiedParentTable);
        }

        PriorityQueue<Integer> removalQueue = new PriorityQueue<>(Collections.reverseOrder());
        for (RelationIndex index:relationship){
            removalQueue.add(index.getChildIndex());
        }

        for (int index:removalQueue){
            members = members.remove(index);
        }

        return modulePartNode.modify().withMembers(members).apply();
    }

    private boolean isChildTable(String parentKey,String childKey){
        String[] splitResult = childKey.split("\\.");
        if (splitResult.length > 1){
            return parentKey.equals(getParentKey(splitResult));
        } else {
            return false;
        }
    }

    private String getParentKey(String[] key){
        return String.join(".",Arrays.copyOf(key, key.length-1));
    }

    @Override
    public Node transform(TableNode tableNode) {

        String identifierName = tableNode.identifier().text();


        NodeList<Node> fields = tableNode.fields();
        for (Node node : fields){
            Node childNode = node.apply(this);

        }
        return tableNode;
    }

    @Override
    public Node transform(TableArrayNode tableArrayNode) {

        return super.transform(tableArrayNode);
    }

    @Override
    public Node transform(KeyValue keyValue) {
        String key = keyValue.identifier().text();
//        String key = keyValue.identifier().text();
//        if (key.equals("\"\"")){
//            System.out.println("test");
//        }
        return keyValue;
    }

    @Override
    protected Node transformSyntaxNode(Node node) {
        throw new RuntimeException("Node not supported: " + node.getClass().getSimpleName());
    }
}

class RelationIndex implements Comparable<RelationIndex>{
    private int parentIndex;
    private int childIndex;
    private int stepCount;

    public RelationIndex(int parentIndex, int childIndex, int stepCount) {

        this.parentIndex = parentIndex;
        this.childIndex = childIndex;
        this.stepCount = stepCount;
    }

    public int getParentIndex() {

        return parentIndex;
    }

    public int getChildIndex() {

        return childIndex;
    }

    public int getStepCount() {

        return stepCount;
    }

    @Override
    public int compareTo(RelationIndex o) {
        return o.getStepCount()-stepCount;
    }
}

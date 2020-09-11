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

package org.wso2.ballerinalang.compiler.bir.optimizer;

import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The control flow graph that gets created from the bir. In this implementation each instruction is reflected as a node
 * in the Graph including the terminator instruction.
 */
public class ControlFlowGraph {
    private final BIRNode.BIRFunction function;
    private final Map<BIRNode.BIRBasicBlock, Node> funcBasicBlockFirstNodeMap;
    private final Map<BIRNode.BIRBasicBlock, Node> funcBasicBlockLastNodeMap;
    private final List<Node> nodes;

    public ControlFlowGraph(BIRNode.BIRFunction function) {
        this.function = function;
        funcBasicBlockFirstNodeMap = new HashMap<>();
        funcBasicBlockLastNodeMap = new HashMap<>();
        nodes = new ArrayList<>();
        generate();
    }

    /**
     * Traverses the BIR and generates a graph. Each instruction is represented as a Node and linked with an edge to the
     * next instruction within the same basic block. The nodes across the basic blocks are linked based on the basic
     * blocks' terminator nodes.
     */
    public void generate() {
        for (BIRNode.BIRBasicBlock basicBlock : function.basicBlocks) {
            Node prev = null;
            for (int i = 0; i < basicBlock.instructions.size(); i++) {
                Node node = new Node(basicBlock.instructions.get(i));
                nodes.add(node);
                if (i == 0) {
                    funcBasicBlockFirstNodeMap.put(basicBlock, node);
                }
                if (prev != null) {
                    addEdge(prev, node);
                }
                prev = node;
            }
            Node terminatorNode = new Node(basicBlock.terminator);
            nodes.add(terminatorNode);
            if (prev != null) {
                addEdge(prev, terminatorNode);
            } else {
                funcBasicBlockFirstNodeMap.put(basicBlock, terminatorNode);
            }
            funcBasicBlockLastNodeMap.put(basicBlock, terminatorNode);
        }

        connectNodesAcrossBasicBlocks();
    }

    private void connectNodesAcrossBasicBlocks() {
        funcBasicBlockLastNodeMap.forEach((birBasicBlock, node) -> {
            BIRTerminator terminator = birBasicBlock.terminator;
            for (BIRNode.BIRBasicBlock basicBlock : terminator.getNextBasicBlocks()) {
                Node target = funcBasicBlockFirstNodeMap.get(basicBlock);
                if (target != null) {
                    addEdge(node, target);
                }
            }
        });
    }

    private void addEdge(Node first, Node second) {
        first.successors.add(second);
        second.predecessors.add(first);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    static class Node {
        List<Node> successors;
        List<Node> predecessors;
        BIRAbstractInstruction instruction;

        public Node(BIRAbstractInstruction instruction) {
            this.instruction = instruction;
            predecessors = new ArrayList<>();
            successors = new ArrayList<>();
        }
    }
}

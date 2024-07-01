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
package org.ballerinalang.langserver.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filter words using Trie data structure.
 *
 * @since 2201.1.1
 * */
public class Trie {
    /**
     * Get completion items by prefix.
     */
    public TrieNode root;

    public Trie(List<String> words) {
        root = new TrieNode();
        for (String word : words) {
            root.insert(word);
        }
    }

    public boolean find(String prefix, boolean exact) {
        TrieNode lastNode = root;
        for (char character : prefix.toCharArray()) {
            lastNode = lastNode.children.get(character);
            if (lastNode == null) {
                return false;
            }
        }
        return !exact || lastNode.isWord;
    }

    public boolean find(String prefix) {
        return find(prefix, false);
    }

    public void suggestHelper(TrieNode root, List<String> list, StringBuilder stringBuilder) {
        if (root.isWord) {
            list.add(stringBuilder.toString());
        }

        if (root.children == null || root.children.isEmpty()) {
            return;
        }

        for (TrieNode child : root.children.values()) {
            stringBuilder.append(child.character);
            suggestHelper(child, list, stringBuilder);
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
    }

    public List<String> suggest(String prefix) {
        List<String> list = new ArrayList<>();
        TrieNode lastNode = root;
        StringBuilder stringBuilder = new StringBuilder();
        for (char character : prefix.toCharArray()) {
            lastNode = lastNode.children.get(character);
            if (lastNode == null) {
                return list;
            }
            stringBuilder.append(character);
        }
        suggestHelper(lastNode, list, stringBuilder);
        return list;
    }
    /**
     * Insert words into the trie.
     */
    public static class TrieNode {
        Map<Character, TrieNode> children;
        char character;
        boolean isWord;

        public TrieNode(char character) {
            this.character = character;
            children = new HashMap<>();
        }

        public TrieNode() {
            children = new HashMap<>();
        }

        public void insert(String word) {
            if (word == null || word.isEmpty()) {
                return;
            }
            char firstCharacter = word.charAt(0);
            TrieNode child = children.get(firstCharacter);
            if (child == null) {
                child = new TrieNode(firstCharacter);
                children.put(firstCharacter, child);
            }

            if (word.length() > 1) {
                child.insert(word.substring(1));
            } else {
                child.isWord = true;
            }
        }
    }
}

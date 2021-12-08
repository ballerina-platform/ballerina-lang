/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.simulator;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.simulator.generators.Generators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main class to simulate the behavior of language server. Similarly to how vscode client use LSP to send different
 * updates, this sends similar messages via JSON RPC to the language server.
 *
 * @since 2.0.0
 */
public class EditorSimulator {

    private static final Logger logger = LoggerFactory.getLogger(EditorSimulator.class);

    private static final String PROP_DURATION = "ls.simulation.duration";
    private static final String PROP_SOURCE_DIR = "ls.simulation.src";

    private static final Random random = new Random();

    public static void main(String[] args) throws IOException {
        try {
            run();
        } catch (Exception e) {
            logger.error("Error occurred while running the simulator", e);
            throw e;
        }
    }

    public static void run() throws IOException {
        int durationSeconds = Integer.parseInt(System.getProperty(PROP_DURATION, "60")) * 60;
        String projectPath = System.getProperty(PROP_SOURCE_DIR);
        if (projectPath == null) {
            throw new IllegalArgumentException("No ballerina project path provided");
        }

        Path path = Paths.get(projectPath);
        logger.info("Using project: {}, path: {}", path.toString(), projectPath);

        List<Path> balFiles = Files.list(path)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName() != null)
                .filter(p -> p.getFileName().toString().endsWith(".bal"))
                .collect(Collectors.toList());

        if (balFiles.isEmpty()) {
            throw new IllegalArgumentException("No bal files found in the provided directory");
        }

        Path modulesPath = path.resolve("modules");
        if (Files.exists(modulesPath)) {
            Files.list(modulesPath)
                    .filter(Files::isDirectory)
                    .flatMap(modPath -> {
                        try {
                            return Files.list(modPath)
                                    .filter(Files::isRegularFile)
                                    .filter(p -> p.getFileName() != null)
                                    .filter(p -> p.getFileName().toString().endsWith(".bal"));
                        } catch (IOException e) {
                            logger.error("Unable to read path: {}", modPath);
                            return Stream.empty();
                        }
                    })
                    .forEach(balFiles::add);
        }

        logger.info("Found bal files in project: {}", balFiles.stream()
                .map(Path::toString).collect(Collectors.joining("\n")));

        Editor editor = Editor.open();
        Runtime.getRuntime().addShutdownHook(new Thread(editor::close));

        long endTime = Instant.now().getEpochSecond() + durationSeconds;
        while (Instant.now().getEpochSecond() < endTime) {
            int i = random.nextInt(balFiles.size());
            Path balFile = balFiles.get(i);
            EditorTab editorTab = editor.openFile(balFile);

            // Select a random place to type random code
            ModulePartNode modulePartNode = editorTab.syntaxTree().rootNode();
            NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
            ModuleMemberDeclarationNode moduleMemberDeclarationNode = members.get(random.nextInt(members.size()));
            LinePosition linePosition = moduleMemberDeclarationNode.location().lineRange().startLine();
            // Set cursor to start of random node
            editorTab.cursor(linePosition.line(), linePosition.offset());

            logger.info("Generating random code snippet");
            // Get random content to type
            String content = getRandomNode();
            logger.info("Typing in editor tab: {} -> {}", editorTab, content);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                editorTab.type(content);
                editorTab.completions();
            });
            
            // While the snippet is being typed, check if we have reached a timeout
            while (!future.isDone() && Instant.now().getEpochSecond() < endTime) {
                logger.info("Remaining time: {}", endTime - Instant.now().getEpochSecond());
                try {
                    Thread.sleep(60 * 1000L);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted editing", e);
                    break;
                }
            }
            
            try {
                int sleepSecs = 1 + random.nextInt(5);
                Thread.sleep(sleepSecs * 1000L);
            } catch (InterruptedException e) {
                logger.warn("Interrupted simulation", e);
                break;
            }
        }

        System.out.println("Exiting...");
        editor.close();
        System.exit(0);
    }

    /**
     * Generate a random syntax tree node (top level) to be inserted to the source document.
     *
     * @return Source for a random top level node.
     */
    public static String getRandomNode() {
        List<Generators.Type> types = Arrays.stream(Generators.Type.values())
                .filter(Generators.Type::isTopLevelNode)
                .collect(Collectors.toList());

        // Get random generator
        Generators.Type type = types.get(random.nextInt(types.size()));
        logger.info("Generating snippet of type: {}", type);
        return Generators.generate(type);
    }
}

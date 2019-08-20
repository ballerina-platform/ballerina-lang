/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Responsible for creating a Ballerina module.
 */
public class BallerinaModuleBuilder extends JavaModuleBuilder implements SourcePathsBuilder, ModuleBuilderListener {

    private static final Logger LOG = Logger.getInstance(BallerinaModuleBuilder.class);
    private ModifiableRootModel rootModel;

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        this.rootModel = modifiableRootModel;
        addListener(this);
        super.setupRootModel(modifiableRootModel);
    }

    @Override
    public List<Pair<String, String>> getSourcePaths() {
        try {
            // Todo - Revamp with "balerina init ." command, when its available.
            initProject(Paths.get(Objects.requireNonNull(rootModel.getProject().getBasePath())));
        } catch (Exception e) {
            LOG.warn("Ballerina project artifacts creation is failed dues to: ", e);
        }
        return ContainerUtil.emptyList();
    }

    /**
     * Initialize a new ballerina project in the given path.
     *
     * @param path Project path
     * @throws IOException If any IO exception occurred
     */
    private static void initProject(Path path) throws IOException {
        // We will be creating following in the project directory
        // - Ballerina.toml
        // - src/
        // - tests/
        // -- resources/      <- integration test resources
        // - .gitignore       <- git ignore file

        Path manifest = path.resolve("Ballerina.toml");
        Path src = path.resolve("src");
        Path test = path.resolve("tests");
        Path testResources = test.resolve("resources");
        Path gitignore = path.resolve(".gitignore");


        Files.createFile(manifest);
        Files.createFile(gitignore);
        Files.createDirectory(src);
        Files.createDirectory(test);
        Files.createDirectory(testResources);

        String defaultManifest = "[project]\n" +
                "org-name= \"ORG_NAME\"\n" +
                "version= \"0.1.0\"\n" +
                "\n" +
                "[dependencies]\n";

        String defaultGitignore = "target\n";

        // replace manifest org with a guessed value.
        defaultManifest = defaultManifest.replaceAll("ORG_NAME", guessOrgName());

        Files.write(manifest, defaultManifest.replace("\n", System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        Files.write(gitignore, defaultGitignore.replace("\n", System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
    }

    private static String guessOrgName() {
        String guessOrgName = System.getProperty("user.name");
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
    }

    private void addBallerinaProjectArtifacts() {
        try {
            LOG.info("Initiating ballerina project by adding required project artifacts manually");

            // Creates "src" dir.
            String ballerinaSrcDir = getContentEntryPath() + File.separator + "src";
            new File(ballerinaSrcDir).mkdirs();

            // Creates .toml file.
            String ballerinaTomlFile = getContentEntryPath() + File.separator +
                    BallerinaConstants.BALLERINA_CONFIG_FILE_NAME;
            File file = new File(ballerinaTomlFile);
            file.createNewFile();
        } catch (Exception e) {
            LOG.warn("Error occurred when initiating ballerina project due to:", e);
        }
    }

    @NotNull
    @Override
    public ModuleType getModuleType() {
        return BallerinaModuleType.getInstance();
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType == BallerinaSdkType.getInstance();
    }

    @Override
    public void moduleCreated(@NotNull Module module) {
        CompilerWorkspaceConfiguration.getInstance(module.getProject()).CLEAR_OUTPUT_DIRECTORY = false;
    }
}

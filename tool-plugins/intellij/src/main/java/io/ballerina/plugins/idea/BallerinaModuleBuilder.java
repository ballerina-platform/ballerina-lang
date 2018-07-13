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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Responsible for creating a Ballerina module.
 */
public class BallerinaModuleBuilder extends JavaModuleBuilder implements SourcePathsBuilder, ModuleBuilderListener {

    private static final Logger LOG = Logger.getInstance(BallerinaModuleBuilder.class);
    private static final String SERVICE_CONTENT = "// A system package containing protocol access constructs\n"
            + "// Package objects referenced with 'http:' in code\n" + "import ballerina/http;\n" + "\n"
            + "documentation {\n" + "   A service endpoint represents a listener.\n" + "}\n"
            + "endpoint http:Listener listener {\n" + "    port:9090\n" + "};\n" + "\n" + "documentation {\n"
            + "   A service is a network-accessible API\n"
            + "   Advertised on '/hello', port comes from listener endpoint\n" + "}\n"
            + "service<http:Service> hello bind listener {\n" + "\n" + "    documentation {\n"
            + "       A resource is an invokable API method\n" + "       Accessible at '/hello/sayHello\n"
            + "       'caller' is the client invoking this resource \n" + "\n" + "       P{{caller}} Server Connector\n"
            + "       P{{request}} Request\n" + "    }\n" + "    sayHello (endpoint caller, http:Request request) {\n"
            + "\n" + "        // Create object to carry data back to caller\n"
            + "        http:Response response = new;\n" + "\n"
            + "        // Objects and structs can have function calls\n"
            + "        response.setTextPayload(\"Hello Ballerina!\\n\");\n" + "\n"
            + "        // Send a response back to caller\n" + "        // Errors are ignored with '_'\n"
            + "        // -> indicates a synchronous network-bound call\n"
            + "        _ = caller -> respond(response);\n" + "    }\n" + "}";

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        addListener(this);
        super.setupRootModel(modifiableRootModel);
    }

    // Note - Removing this override will create src directory in the project root.
    public List<Pair<String, String>> getSourcePaths() {
        String ballerinaCacheRoot = getContentEntryPath() + File.separator + ".ballerina";
        new File(ballerinaCacheRoot).mkdirs();
        String ballerinaTomlFile = getContentEntryPath() + File.separator + "Ballerina.toml";
        File tomlFile = new File(ballerinaTomlFile);
        String helloWorldServiceFile = getContentEntryPath() + File.separator + "hello_service.bal";
        File serviceFile = new File(helloWorldServiceFile);
        try {
            tomlFile.createNewFile();
            serviceFile.createNewFile();
            writeContent(serviceFile.toPath(), SERVICE_CONTENT);
            // Todo - Add some content to the toml file?
        } catch (IOException e) {
            LOG.debug(e);
        }
        return ContainerUtil.emptyList();
    }

    private static void writeContent(Path file, String content) throws IOException {
        byte data[] = content.getBytes(Charset.defaultCharset());
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, CREATE, APPEND))) {
            out.write(data, 0, data.length);
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

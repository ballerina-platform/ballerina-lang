/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;

import org.ballerinalang.config.ConfigRegistry;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class PackagingTest {
    private Path tmpRepo;

    @BeforeClass
    public void setup() throws IOException {
        ConfigRegistry instance = ConfigRegistry.getInstance();
        Map<String, String> runtimeConfigs = new HashMap<>();
        tmpRepo = Files.createTempDirectory("bal_unit_tmp_repo_");
        runtimeConfigs.put("ballerina.repo", tmpRepo.toAbsolutePath().toString());
        instance.initRegistry(runtimeConfigs);
        instance.loadConfigurations();
    }

    @AfterClass
    public void teardown() throws IOException {
        Files.walk(tmpRepo)
                .sorted(Comparator.reverseOrder())
                .filter(Files::isDirectory)
                .map(Path::toFile)
                .forEach(f -> Assert.assertTrue(f.delete(), "Can't delete file " + f));
    }
}

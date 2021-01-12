/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.allreferences;

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getModule;

/**
 * Test cases for the finding all references of a symbol across multiple files in the module.
 *
 * @since 2.0.0
 */
@Test
public class FindRefsAcrossFilesTest extends FindAllReferencesTest {

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject(getTestSourcePath());
        Module baz = getModule(project, "baz");
        model = baz.getCompilation().getSemanticModel();
        srcFile = getDocument(baz);
    }

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {22, 42, List.of(location(18, 6, 10, "constants.bal"),
                                 location(22, 42, 46, getFileName()))
                },
                {22, 56, List.of(location(16, 12, 18, "type_defs.bal"),
                                 location(22, 56, 62, getFileName()))
                },
                {16, 16, List.of(location(16, 16, 19, getFileName()),
                                 location(25, 14, 17, "tests/test1.bal"))
                },
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/test-project";
    }

    private Document getDocument(Module module) {
        for (DocumentId id : module.documentIds()) {
            Document doc = module.document(id);
            if ("functions.bal".equals(doc.name())) {
                return doc;
            }
        }

        throw new IllegalStateException("Source file 'functions.bal' not found");
    }
}

/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.suggetions;

import org.ballerinalang.composer.service.workspace.langserver.CompletionItemAccumulator;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BFile;
import org.ballerinalang.model.BallerinaFile;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Tests for the auto complete suggester
 */
public class AutoCompleteSuggesterTest {

    public static void main(String[] args) throws IOException {
        //TODO Fix this test and enable
//        BFile bFile = new BFile();
//        bFile.setContent("import \n" +
//                "@http:BasePath { value: \"/healthcare\"}\n" +
//                "service Service1 {\n" +
//                "\n" +
//                "    @http:POST {}\n" +
//                "    @http:Path { value: \"/reserve\"}\n" +
//                "    resource Resource1 (message m) {\n" +
//                "    \tint a = 12;\n" +
//                "    \tint b = 12\n" +
//                "    }\n" +
//                "}\n");
//        bFile.setFilePath("/temp");
//        bFile.setFileName("temp.bal");
//        bFile.setPackageName(".");
//
//        Position position = new Position();
//        position.setLine(1);
//        position.setCharacter(7);
//        AutoCompleteSuggester autoCompleteSuggester = new AutoCompleteSuggesterImpl();
//        CapturePossibleTokenStrategy capturePossibleTokenStrategy = new CapturePossibleTokenStrategy(position);
//        BallerinaFile ballerinaFile = autoCompleteSuggester.getBallerinaFile(bFile, position,
//                capturePossibleTokenStrategy);
//        capturePossibleTokenStrategy.getSuggestionsFilterDataModel().setBallerinaFile(ballerinaFile);
//        SuggestionsFilterDataModel dm = capturePossibleTokenStrategy.getSuggestionsFilterDataModel();
//
//        ArrayList completionItem = new ArrayList<>();
//        CompletionItemAccumulator jsonModelBuilder = new CompletionItemAccumulator(completionItem, position);
//        dm.getBallerinaFile().accept(jsonModelBuilder);
//        dm.setClosestScope(jsonModelBuilder.getClosestScope());
//
//        SuggestionsFilter suggestionsFilter = new SuggestionsFilter();
//        suggestionsFilter.getCompletionItems(dm, completionItem);
    }
}

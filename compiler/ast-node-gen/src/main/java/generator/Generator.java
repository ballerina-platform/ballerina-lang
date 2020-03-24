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
package generator;

import exception.GeneratorException;

import java.io.IOException;

import static generator.FacadeGenerator.generateFacade;
import static generator.InternalGenerator.generateInternalTree;

public class Generator {
    private static String CLASS_PATH = "src/main/java/generated";
    private static String FACADE_TEMPLATE_PATH = "src/main/resources/facadeTemplate.mustache";
    private static String INTERNAL_TEMPLATE_PATH = "src/main/resources/internalTemplate.mustache";
    private static String NODE_JSON_PATH = "src/main/resources/newTree.json";

    public static void main(String[] args) {
        try {
            generateInternalTree(INTERNAL_TEMPLATE_PATH, CLASS_PATH, NODE_JSON_PATH);
            generateFacade(FACADE_TEMPLATE_PATH, CLASS_PATH, NODE_JSON_PATH);
        } catch (IOException | GeneratorException e) {
            e.printStackTrace();
        }
    }
}

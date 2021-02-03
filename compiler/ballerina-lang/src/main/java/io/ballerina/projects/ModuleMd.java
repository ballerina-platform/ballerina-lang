/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects;

/**
 * Represents the 'Module.md' file of a module.
 *
 * @since 2.0.0
 */
public class ModuleMd {

    private final MdDocumentContext mdDocumentContext;
    private final Module module;

    ModuleMd(MdDocumentContext documentContext, Module module) {
        this.mdDocumentContext = documentContext;
        this.module = module;
    }

    public static ModuleMd from(DocumentConfig documentConfig, Module module) {
        MdDocumentContext documentContext = MdDocumentContext.from(documentConfig);
        return new ModuleMd(documentContext, module);
    }

    public static ModuleMd from(MdDocumentContext documentContext, Module module) {
        return new ModuleMd(documentContext, module);
    }

    MdDocumentContext mdDocumentContext() {
        return mdDocumentContext;
    }

    public Module module() {
        return module;
    }

    public String content() {
        return mdDocumentContext.content();
    }
}

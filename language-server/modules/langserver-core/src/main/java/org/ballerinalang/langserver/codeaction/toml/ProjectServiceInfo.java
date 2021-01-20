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
package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents service related data of a Project after parsed from Syntax trees.
 *
 * @since 2.0.0
 */
public class ProjectServiceInfo {
    private final Map<String, List<ServiceInfo>> serviceMap;
    private final Map<String, List<ListenerInfo>> listenerMap;

    public ProjectServiceInfo(Project project) {
        Package currentPackage = project.currentPackage();

        Iterable<Module> modules = currentPackage.modules();
        Map<String, List<ServiceInfo>> serviceMap = new HashMap<>();
        Map<String, List<ListenerInfo>> listenerMap = new HashMap<>();
        for (Module module : modules) {
            Collection<DocumentId> documentIds = module.documentIds();
            for (DocumentId doc : documentIds) {
                Document document = module.document(doc);
                String name = document.name();
                Node node = document.syntaxTree().rootNode();

                C2CVisitor visitor = new C2CVisitor();
                node.accept(visitor);
                serviceMap.put(name, visitor.getServices());
                listenerMap.put(name, visitor.getListeners());
            }
        }

        //When service use a listener in another bal file
        for (Map.Entry<String, List<ServiceInfo>> entry : serviceMap.entrySet()) {
            String fileName = entry.getKey();
            List<ServiceInfo> value = entry.getValue();
            for (ServiceInfo serviceInfo : value) {
                ListenerInfo listener = serviceInfo.getListener();
                if (listener.getPort() == 0) {
                    String name = listener.getName();
                    List<ListenerInfo> listenerInfos = listenerMap.get(fileName);
                    for (ListenerInfo listenerInfo : listenerInfos) {
                        if (name.equals(listenerInfo.getName())) {
                            listener.setPort(listenerInfo.getPort());
                        }
                    }
                }
            }
        }
        this.serviceMap = serviceMap;
        this.listenerMap = listenerMap;
    }

    public Map<String, List<ServiceInfo>> getServiceMap() {
        return serviceMap;
    }

    public Map<String, List<ListenerInfo>> getListenerMap() {
        return listenerMap;
    }
}

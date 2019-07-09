/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines the manifest object which is created using the toml file configs.
 *
 * @since 0.964
 */
public class Manifest {
    private Project project = new Project();
    private Map<String, Object> dependencies = new LinkedHashMap<>();
    private Platform platform = new Platform();
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public Map<String, Object> getDependenciesAsObjectMap() {
        return this.dependencies.entrySet().stream()
                .collect(Collectors.toMap(d -> d.getKey().replaceAll("^\"|\"$", ""), Map.Entry::getValue));
    }
    
    public List<Dependency> getDependencies() {
        return this.dependencies.entrySet().stream()
                .map(entry -> {
                    Dependency dependency = new Dependency();
                    // get rid of the double quotes
                    dependency.setModuleName(entry.getKey());
                    dependency.setMetadata(convertObjectToDependencyMetadata(entry.getValue()));
                    return dependency;
                })
                .collect(Collectors.toList());
    }
    
    private DependencyMetadata convertObjectToDependencyMetadata(Object obj) {
        DependencyMetadata metadata = new DependencyMetadata();
        if (obj instanceof String) {
            metadata.setVersion((String) obj);
        } else if (obj instanceof Map) {
            Map metadataMap = (Map) obj;
            if (metadataMap.keySet().contains("version") && metadataMap.get("version") instanceof String) {
                metadata.setVersion((String) metadataMap.get("version"));
            }
    
            if (metadataMap.keySet().contains("path") &&  metadataMap.get("path") instanceof String) {
                metadata.setPath((String) metadataMap.get("path"));
            }
        }
        return metadata;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     * Project definition.
     */
    public static class Project {
        private String orgName = "";
        private String version = "";
        private String license = "";
        private List<String> authors = new LinkedList<>();
        private String repository = "";
        private List<String> keywords = new LinkedList<>();
        
        public String getOrgName() {
            return orgName;
        }
        
        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public String getLicense() {
            return license;
        }
        
        public void setLicense(String license) {
            this.license = license;
        }
        
        public List<String> getAuthors() {
            return authors;
        }
        
        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }
        
        public String getRepository() {
            return repository;
        }
        
        public void setRepository(String repository) {
            this.repository = repository;
        }
        
        public List<String> getKeywords() {
            return keywords;
        }
        
        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.maven.bala.client.model;

/**
 * Data class representing a single symbol entry in symbol search results from maven-metadata.xml.
 *
 * @since 2201.13.3
 */
public class SymbolSearchEntry {
    private String id;
    private String packageID;
    private String name;
    private String org;
    private String version;
    private long createdDate;
    private String icon;
    private String symbolType;
    private String symbolParent;
    private String symbolName;
    private String description;
    private String symbolSignature;
    private boolean isIsolated;
    private boolean isRemote;
    private boolean isResource;
    private boolean isClosed;
    private boolean isDistinct;
    private boolean isReadOnly;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(String symbolType) {
        this.symbolType = symbolType;
    }

    public String getSymbolParent() {
        return symbolParent;
    }

    public void setSymbolParent(String symbolParent) {
        this.symbolParent = symbolParent;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSymbolSignature() {
        return symbolSignature;
    }

    public void setSymbolSignature(String symbolSignature) {
        this.symbolSignature = symbolSignature;
    }

    public boolean isIsolated() {
        return isIsolated;
    }

    public void setIsolated(boolean isolated) {
        isIsolated = isolated;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public boolean isResource() {
        return isResource;
    }

    public void setResource(boolean resource) {
        isResource = resource;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    @Override
    public String toString() {
        return "SymbolSearchEntry{" +
                "id='" + id + '\'' +
                ", packageID='" + packageID + '\'' +
                ", name='" + name + '\'' +
                ", org='" + org + '\'' +
                ", version='" + version + '\'' +
                ", createdDate=" + createdDate +
                ", symbolType='" + symbolType + '\'' +
                ", symbolParent='" + symbolParent + '\'' +
                ", symbolName='" + symbolName + '\'' +
                ", description='" + description + '\'' +
                ", symbolSignature='" + symbolSignature + '\'' +
                '}';
    }
}

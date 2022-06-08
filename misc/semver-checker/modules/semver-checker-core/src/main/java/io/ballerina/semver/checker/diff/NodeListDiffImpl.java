/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semver.checker.diff;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.semver.checker.util.DiffUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_KIND;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_MESSAGE;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_TYPE;
import static io.ballerina.semver.checker.util.DiffUtils.DIFF_ATTR_VERSION_IMPACT;
import static io.ballerina.semver.checker.util.DiffUtils.stringifyDiff;

/**
 * Base implementation of changes in Ballerina syntax tree node lists.
 *
 * @param <T> node type
 * @since 2201.2.0
 */
public class NodeListDiffImpl<T extends Node> implements NodeListDiff<List<T>> {

    protected final List<T> newNodes;
    protected final List<T> oldNodes;
    protected DiffType diffType;
    protected SemverImpact versionImpact;
    protected final List<Diff> childDiffs;
    private String message;

    private NodeListDiffImpl(List<T> newNodes, List<T> oldNodes) {
        this(newNodes, oldNodes, DiffType.UNKNOWN, SemverImpact.UNKNOWN);
    }

    private NodeListDiffImpl(List<T> newNodes, List<T> oldNodes, DiffType diffType,
                             SemverImpact versionImpact) {
        this.newNodes = newNodes;
        this.oldNodes = oldNodes;
        this.diffType = diffType;
        this.versionImpact = versionImpact;
        this.childDiffs = new ArrayList<>();
        this.message = null;
    }

    @Override
    public Optional<List<T>> getNewNodes() {
        return Optional.ofNullable(newNodes);
    }

    @Override
    public Optional<List<T>> getOldNodes() {
        return Optional.ofNullable(oldNodes);
    }

    @Override
    public DiffType getType() {
        return diffType;
    }

    private void setType(DiffType diffType) {
        this.diffType = diffType;
    }

    @Override
    public SemverImpact getVersionImpact() {
        return versionImpact;
    }

    private void setVersionImpact(SemverImpact versionImpact) {
        this.versionImpact = versionImpact;
    }

    @Override
    public void computeVersionImpact() {
        if (versionImpact == SemverImpact.UNKNOWN) {
            versionImpact = childDiffs.stream()
                    .map(Diff::getVersionImpact)
                    .max(Comparator.comparingInt(SemverImpact::getRank))
                    .orElse(SemverImpact.UNKNOWN);
        }
    }

    @Override
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public List<Diff> getChildDiffs() {
        return childDiffs;
    }

    @Override
    public List<Diff> getChildDiffs(SemverImpact versionImpact) {
        List<Diff> filteredDiffs = new ArrayList<>();
        for (Diff diff : childDiffs) {
            if (diff.getChildDiffs().isEmpty()) {
                if (diff.getVersionImpact() == versionImpact) {
                    filteredDiffs.add(diff);
                }
            } else {
                for (Diff childDiff : diff.getChildDiffs()) {
                    filteredDiffs.addAll(childDiff.getChildDiffs(versionImpact));
                }
            }
        }
        return filteredDiffs;
    }

    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        if (childDiffs == null || childDiffs.isEmpty()) {
            sb.append(stringifyDiff(this));
        } else {
            childDiffs.forEach(diff -> sb.append(diff.getAsString()));
        }

        return sb.toString();
    }

    @Override
    public JsonArray getAsJson() {
        JsonArray childArray = new JsonArray();
        if (childDiffs == null || childDiffs.isEmpty()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(DIFF_ATTR_KIND, new JsonPrimitive(DiffUtils.getDiffTypeName(this)));
            jsonObject.add(DIFF_ATTR_TYPE, new JsonPrimitive(this.getType().name().toLowerCase(Locale.getDefault())));
            jsonObject.add(DIFF_ATTR_VERSION_IMPACT, new JsonPrimitive(this.getVersionImpact().name()
                    .toLowerCase(Locale.getDefault())));
            if (this.getMessage().isPresent()) {
                jsonObject.add(DIFF_ATTR_MESSAGE, new JsonPrimitive(this.getVersionImpact().name()));
            }
            childArray.add(jsonObject);
        } else {
            for (Diff diff : childDiffs) {
                JsonElement jsonDiff = diff.getAsJson();
                if (jsonDiff.isJsonArray()) {
                    childArray.addAll((JsonArray) jsonDiff);
                } else {
                    childArray.add(diff.getAsJson());
                }
            }
        }

        return childArray;
    }

    /**
     * Node list diff builder implementation.
     *
     * @param <T> Node type
     */
    public static class Builder<T extends Node> implements NodeDiffBuilder {

        private final NodeListDiffImpl<T> nodeListDiff;

        public Builder(List<T> newNodes, List<T> oldNodes) {
            nodeListDiff = new NodeListDiffImpl<>(newNodes, oldNodes);
        }

        @Override
        public Optional<NodeListDiffImpl<?>> build() {
            if (!nodeListDiff.getChildDiffs().isEmpty()) {
                nodeListDiff.computeVersionImpact();
                nodeListDiff.setType(DiffType.MODIFIED);
                return Optional.of(nodeListDiff);
            } else if (nodeListDiff.getType() == DiffType.NEW || nodeListDiff.getType() == DiffType.REMOVED
                    || nodeListDiff.getMessage().isPresent()) {
                return Optional.of(nodeListDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            nodeListDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            nodeListDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            nodeListDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            nodeListDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            nodeListDiff.childDiffs.addAll(childDiffs);
            return null;
        }
    }
}

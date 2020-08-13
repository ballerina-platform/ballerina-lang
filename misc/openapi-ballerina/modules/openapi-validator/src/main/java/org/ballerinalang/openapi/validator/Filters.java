package org.ballerinalang.openapi.validator;

import org.ballerinalang.util.diagnostic.Diagnostic;

import java.util.List;

/**
 * This for model the all tag, operations, excludeTags and excludeOperations filters.
 */
public class Filters {
    private List<String> tag;
    private List<String> excludeTag;
    private List<String> operation;
    private List<String> excludeOperation;
    private Diagnostic.Kind kind;

    public Filters(List<String> tag, List<String> excludeTag, List<String> operation,
                   List<String> excludeOperation, Diagnostic.Kind kind) {

        this.tag = tag;
        this.excludeTag = excludeTag;
        this.operation = operation;
        this.excludeOperation = excludeOperation;
        this.kind = kind;
    }

    public List<String> getTag() {

        return tag;
    }

    public void setTag(List<String> tag) {

        this.tag = tag;
    }

    public List<String> getExcludeTag() {

        return excludeTag;
    }

    public void setExcludeTag(List<String> excludeTag) {

        this.excludeTag = excludeTag;
    }

    public List<String> getOperation() {

        return operation;
    }

    public void setOperation(List<String> operation) {

        this.operation = operation;
    }

    public List<String> getExcludeOperation() {

        return excludeOperation;
    }

    public Diagnostic.Kind getKind() {

        return kind;
    }

    public void setKind(Diagnostic.Kind kind) {

        this.kind = kind;
    }

    public void setExcludeOperation(List<String> excludeOperation) {

        this.excludeOperation = excludeOperation;
    }

}

package io.ballerina.projects;

import io.ballerina.projects.plugins.completion.CompletionException;
import io.ballerina.projects.plugins.completion.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of the completion operation.
 *
 * @since 2201.7.0
 */
public class CompletionResult {

    private final List<CompletionItem> completionItems = new ArrayList<>();
    private final List<CompletionException> errors = new ArrayList<>();

    public void addCompletionItems(List<CompletionItem> completionItems) {
        this.completionItems.addAll(completionItems);
    }

    public void addError(CompletionException ex) {
        errors.add(ex);
    }

    /**
     * Get completion items provided by compiler plugins.
     *
     * @return List of completion items
     */
    public List<CompletionItem> getCompletionItems() {
        return completionItems;
    }

    /**
     * Get errors catch while processing compiler plugin completion providers.
     *
     * @return List of errors
     */
    public List<CompletionException> getErrors() {
        return errors;
    }
}

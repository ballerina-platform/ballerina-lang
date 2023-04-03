package io.ballerina.projects.plugins.completion;

import io.ballerina.tools.text.LinePosition;

import java.util.List;

/**
 * Represents a completion item.
 *
 * @since 2201.6.0
 */
public class CompletionItem {
    /**
     * The label of this completion item. By default, also the text that is inserted when selecting
     * this completion.
     */
    private String label;
    
    /**
     * Indicates the priority(sorted position) of the completion item.
     */
    private Priority priority;

    /**
     * An optional array of additional text edits that are applied when selecting this completion. 
     * Edits must not overlap (including the same insert position) with the main edit nor with themselves.
     * Additional text edits should be used to change text unrelated to the 
     * current cursor position (for example adding an import statement at the top of the file if the completion
     * item will insert a qualified type).
     */
    private List<TextEdit> additionalTextEdits;

    /**
     * A string that should be inserted a document when selecting this completion. 
     * When omitted or empty, the label is used as the insert text for this item.
     */
    private String insertText;

    /**
     * A string that should be used when filtering a set of completion items. 
     * When omitted or empty, the label is used as the filter text for this item.
     */
    private String filterText;
    
    public CompletionItem(String label, String insertText, Priority priority) {
        this.label = label;
        this.insertText = insertText;
        this.priority = priority;
    }

    public String getInsertText() {
        return insertText;
    }

    public String getLabel() {
        return label;
    }
    
    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setAdditionalTextEdits(List<TextEdit> additionalTextEdits) {
        this.additionalTextEdits = additionalTextEdits;
    }

    public List<TextEdit> getAdditionalTextEdits() {
        return additionalTextEdits;
    }

    /**
     * Represents the priority of the completion item. If priority is high the completion item
     * will be sorted to the top of the completion item list. If low a default priority based on
     * the completion item kind (Snippet) will be assigned.
     */
    public enum Priority {
        HIGH,
        LOW
    }
    /**
     * Represents a text edit that is applied along with the completion item.
     */
    static class TextEdit {
        private String newText;
        private LinePosition start;
        private LinePosition end;

        public TextEdit(String newText, LinePosition start, LinePosition end) {
            this.newText = newText;
            this.start = start;
            this.end = end;
        }

        public String getNewText() {
            return newText;
        }

        public LinePosition getStart() {
            return start;
        }

        public LinePosition getEnd() {
            return end;
        }
    }

}

package org.ballerinalang.langserver.util.rename;

import io.ballerina.projects.Module;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility to handle renaming related operations
 */
public class RenameUtil {

    private RenameUtil() {
    }

    /**
     * Process a rename request and returns the text edits required to be made to complete the rename
     *
     * @param context Context
     * @param newName Assigned identifier after renaming
     * @return Text edits for that rename operation
     */
    public static Map<String, List<TextEdit>> rename(ReferencesContext context, String newName) {
        if (!CommonUtil.isIdentifierValid(newName)) {
            throw new UserErrorException("Invalid identifier provided");
        }

        Map<Module, List<Location>> locationMap = ReferencesUtil.getReferences(context);

        Path projectRoot = context.workspace().projectRoot(context.filePath());

        Map<String, List<TextEdit>> changes = new HashMap<>();
        locationMap.forEach((module, locations) ->
                locations.forEach(location -> {
                    String uri = ReferencesUtil.getUriFromLocation(module, location, projectRoot);
                    List<TextEdit> textEdits = changes.computeIfAbsent(uri, k -> new ArrayList<>());
                    textEdits.add(new TextEdit(ReferencesUtil.getRange(location), newName));
                }));
        return changes;
    }
}

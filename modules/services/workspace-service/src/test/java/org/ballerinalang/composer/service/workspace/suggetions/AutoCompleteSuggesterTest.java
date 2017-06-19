package org.ballerinalang.composer.service.workspace.suggetions;

import org.ballerinalang.composer.service.workspace.rest.datamodel.BFile;
import org.ballerinalang.composer.service.workspace.rest.datamodel.SourcePosition;

import java.io.IOException;
import java.util.List;

/**
 * Tests for the auto complete suggester
 */
public class AutoCompleteSuggesterTest {

    public static void main(String[] args) throws IOException {
        BFile bFile = new BFile();
        bFile.setContent("import   ");
        bFile.setFilePath("/temp");
        bFile.setFileName("temp.bal");
        bFile.setPackageName(".");

        SourcePosition position = new SourcePosition(1, 8);
        AutoCompleteSuggester autoCompleteSuggester = new AutoCompleteSuggesterImpl();
        List<PossibleToken> possibleTokenTypes = autoCompleteSuggester.getPossibleTokenTypes(bFile, position);
    }
}

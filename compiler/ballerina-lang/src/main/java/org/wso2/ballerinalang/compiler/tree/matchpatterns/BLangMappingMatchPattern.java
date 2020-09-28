package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.matchpatterns.FieldMatchPatternNode;
import org.ballerinalang.model.tree.matchpatterns.MappingMatchPatternNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

public class BLangMappingMatchPattern extends BLangMatchPattern implements MappingMatchPatternNode {

    public List<BLangFieldMatchPattern> fieldMatchPatterns = new ArrayList<>();
    // TODO : add rest-match-pattern

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.MAPPING_MATCH_PATTERN;
    }

    @Override
    public List<? extends FieldMatchPatternNode> getFieldMatchPatterns() {

        return fieldMatchPatterns;
    }
}

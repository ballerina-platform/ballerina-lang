package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Completion provider for {@link ComputedResourceAccessSegmentNode} context.
 *
 * @since 2201.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ComputedResourceAccessSegmentNodeContext extends
        AbstractCompletionProvider<ComputedResourceAccessSegmentNode> {

    public ComputedResourceAccessSegmentNodeContext() {
        super(ComputedResourceAccessSegmentNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ComputedResourceAccessSegmentNode node) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, node.expression())) {
            /*
               Captures the following
               /[module:a<cursor>]
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.expression();
            Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                    || symbol.kind() == SymbolKind.FUNCTION;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, filter);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            /*
            Captures the following cases
            /[<cursor>]
             */
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }
}

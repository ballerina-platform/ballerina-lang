package io.ballerina.plugins.idea.debugger.evaluator;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.LightVirtualFile;
import io.ballerina.plugins.idea.BallerinaFileType;
import io.ballerina.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Ballerina code fragment representation for debugger expression evaluation.
 */
public class BallerinaExpressionCodeFragment extends PsiFileBase {

    public BallerinaExpressionCodeFragment(Project project, CharSequence text) {
        super(new SingleRootFileViewProvider(PsiManager.getInstance(project),
                        new LightVirtualFile("DebugExpression.bal", BallerinaFileType.INSTANCE, text), true),
                BallerinaLanguage.INSTANCE);
        ((SingleRootFileViewProvider) getViewProvider()).forceCachedPsi(this);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return BallerinaFileType.INSTANCE;
    }
}

package org.ballerinalang.mime.nativeimpl.contentdisposition;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.Constants.ASSIGNMENT;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILENAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_FILE_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME_INDEX;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_PARA_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;
import static org.ballerinalang.mime.util.Constants.SEMICOLON;

@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "toString",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ContentDisposition", structPackage = "ballerina.mime"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToString extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct contentDispositionStruct = (BStruct) context.getRefArgument(FIRST_PARAMETER_INDEX);
        StringBuilder dispositionBuilder = new StringBuilder();
        if (contentDispositionStruct != null) {
            String disposition = contentDispositionStruct.getStringField(DISPOSITION_INDEX);
            if (disposition != null && !disposition.isEmpty()) {
                dispositionBuilder.append(disposition);
                String name = contentDispositionStruct.getStringField(CONTENT_DISPOSITION_NAME_INDEX);
                String fileName = contentDispositionStruct.getStringField(CONTENT_DISPOSITION_FILENAME_INDEX);
                if (MimeUtil.isNotNullAndEmpty(name)) {
                    MimeUtil.appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_NAME).append(ASSIGNMENT).append(
                            MimeUtil.includeQuotes(name)).append(SEMICOLON);
                }
                if (MimeUtil.isNotNullAndEmpty(fileName)) {
                    MimeUtil.appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_FILE_NAME).append(ASSIGNMENT)
                            .append(MimeUtil.includeQuotes(fileName)).append(SEMICOLON);
                }
                if (contentDispositionStruct.getRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX) != null) {
                    BMap map = (BMap) contentDispositionStruct.getRefField(CONTENT_DISPOSITION_PARA_MAP_INDEX);
                    HeaderUtil.appendHeaderParams(MimeUtil.appendSemiColon(dispositionBuilder), map);
                }
                if (dispositionBuilder.toString().endsWith(SEMICOLON)) {
                    dispositionBuilder.setLength(dispositionBuilder.length() - 1);
                }
            }
        }
        context.setReturnValues(new BString(dispositionBuilder.toString()));
    }
}

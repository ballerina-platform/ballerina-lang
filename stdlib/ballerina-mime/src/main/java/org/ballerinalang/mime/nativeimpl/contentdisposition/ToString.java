package org.ballerinalang.mime.nativeimpl.contentdisposition;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.Constants.DISPOSITION_INDEX;
import static org.ballerinalang.mime.util.Constants.FIRST_PARAMETER_INDEX;

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
                dispositionBuilder = MimeUtil.convertDispositionObjectToString(dispositionBuilder, contentDispositionStruct);
            }
        }
        context.setReturnValues(new BString(dispositionBuilder.toString()));
    }
}

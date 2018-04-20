package org.ballerinalang.mime.nativeimpl.contentdisposition;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.mime.util.Constants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.Constants.STRING_INDEX;

@BallerinaFunction(orgName = "ballerina", packageName = "mime",
        functionName = "getContentDispositionObject",
        args = {@Argument(name = "contentDisposition",
                type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT)},
        isPublic = true)
public class GetContentDispositionObject extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        String contentDisposition = context.getStringArgument(STRING_INDEX);
        BStruct contentDispositionObj = ConnectorUtils.createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_MIME,
                Constants.CONTENT_DISPOSITION_STRUCT);
        MimeUtil.populateContentDispositionObject(contentDispositionObj, contentDisposition);
        context.setReturnValues(contentDispositionObj);
    }
}

package org.ballerinalang.nativeimpl.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Native function ballerina.runtime:exec.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "runtime",
        functionName = "execBallerina",
        args = {@Argument(name = "command", type = TypeKind.STRING),
                @Argument(name = "packageName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ExecBallerina extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        Properties property = System.getProperties();
        String path = (String) property.get("ballerina.home");

        String command = context.getStringArgument(0);
        String packageName = context.getStringArgument(1);
        String balCommand = path + "/bin/ballerina " + command + " " + packageName;
        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(balCommand);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader readerEr =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String lineEr = "";
            while ((lineEr = readerEr.readLine()) != null) {
                output.append(lineEr + "\n");
            }
            readerEr.close();

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        String adjusted = output.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
        context.setReturnValues(new BString(adjusted));
    }
}
package org.ballerinalang.nativeimpl.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.nativeimpl.io.channels.base.Buffer;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
                @Argument(name = "data", type = TypeKind.STRING)},
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

        Process process;
        BufferedReader reader = null;
        BufferedReader readerEr = null;
        try {
            StringBuilder output = new StringBuilder();
            process = Runtime.getRuntime().exec(balCommand);
            process.waitFor();
            reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            readerEr =
                    new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String lineEr = "";
            while ((lineEr = readerEr.readLine()) != null) {
                output.append(lineEr).append("\n");
            }

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            String adjusted = output.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
            context.setReturnValues(new BString(adjusted));
        } catch (InterruptedException e) {
            throw new BallerinaException(e.getMessage());
        } catch (IOException e) {
            throw new BallerinaException(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (readerEr != null) {
                    readerEr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



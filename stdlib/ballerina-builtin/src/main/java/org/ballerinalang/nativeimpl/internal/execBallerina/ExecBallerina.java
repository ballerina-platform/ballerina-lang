package org.ballerinalang.nativeimpl.internal.execBallerina;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Native function ballerina.runtime:exec.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "execBallerina",
        args = {@Argument(name = "command", type = TypeKind.STRING),
                @Argument(name = "packagePath", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ExecBallerina extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {

        Properties property = System.getProperties();
        String path = (String) property.get("ballerina.home");

        String command = context.getRefArgument(0).stringValue();
        String packageName = context.getStringArgument(0);

        if (!packageName.contains(";") && !packageName.contains("|") && !packageName.contains("\"")) {
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
        } else {
            String message = "The arguments to the ballerina command: " + command + " contains invalid characters";
            context.setReturnValues(new BString(message));
        }
    }
}

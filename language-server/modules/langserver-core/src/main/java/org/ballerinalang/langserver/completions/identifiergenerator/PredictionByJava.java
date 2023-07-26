package org.ballerinalang.langserver.completions.identifiergenerator;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.LongBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.io.Resources.getResource;

public class PredictionByJava {

    public static void main(String[] args) {
    }

    private static int[] getTopKIndices(float[] array, int k) {
        int[] indices = new int[k];
        for (int i = 0; i < k; i++) {
            int maxIndex = -1;
            float maxValue = Float.NEGATIVE_INFINITY;
            for (int j = 0; j < array.length; j++) {
                if (array[j] > maxValue) {
                    maxValue = array[j];
                    maxIndex = j;
                }
            }
            indices[i] = maxIndex;
            array[maxIndex] = Float.NEGATIVE_INFINITY;
        }
        return indices;
    }

    public static String[] getPredictedToken(String sentence) {
        try {
//            ClassLoader classLoader = PredictionByJava.class.getClassLoader();
//            String path  = classLoader.getResource("/home/vinoth/Documents/ballerina-lang/language-server/modules/langserver-core/src/main/resources/fine_tuned_albert/fine_tuned_model.onnx").getPath();

            HuggingFaceTokenizer tokenizer =
                    HuggingFaceTokenizer.newInstance(Paths.get(
                    "/home/vinoth/Documents/ballerina-lang/language-server/modules/langserver-core/src/" +
                            "main/resources/fine_tuned_albert/tokenizer.json"));

            Encoding encoding = tokenizer.encode(sentence);

            int maskTokenIndex = -1;  // Initialize the mask token index

            String[] tokens = encoding.getTokens();
            int numTokens = encoding.getTokens().length;
            String[] cleanTokens = new String[numTokens];
            int cleanTokensIndex = 0;
            for (int j = 0; j < numTokens; j++) {
                String token = tokens[j].replace(" ", "")
                        .replace("Ġ", "").toLowerCase();
                if (!token.equals("[pad]")) {
                    cleanTokens[cleanTokensIndex++] = token;
                }
                if (token.equals("<mask>")) {
                    maskTokenIndex = cleanTokensIndex - 1;
                }
            }

            if (maskTokenIndex == -1) {
                return null;  // Exit if no masked token is found
            }

            String[] actualTokens = new String[cleanTokensIndex];
            System.arraycopy(cleanTokens, 0, actualTokens, 0, cleanTokensIndex);

            // Update the inputIds and attentionMask arrays
            long[] inputIds = new long[cleanTokensIndex];
            long[] attentionMask = new long[cleanTokensIndex];
            for (int i = 0; i < cleanTokensIndex; i++) {
                inputIds[i] = encoding.getIds()[i];
                attentionMask[i] = encoding.getAttentionMask()[i];
            }

            OrtEnvironment environment = OrtEnvironment.getEnvironment();

            OrtSession session = environment.createSession("/home/vinoth/Documents/ballerina-lang/" +
                    "language-server/modules/langserver-core/src/main/resources/" +
                    "fine_tuned_albert/fine_tuned_model.onnx");
            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(
                    environment, LongBuffer.wrap(inputIds) , new long[]{1, inputIds.length}
            );
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(
                    environment,LongBuffer.wrap(attentionMask) , new long[]{1, attentionMask.length}
            );

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            // Run the model
            OrtSession.Result outputs = session.run(inputs);

            // Get the predictions for the masked token
            Optional<OnnxValue> optionalValue = outputs.get("output");
            OnnxTensor predictionsTensor = (OnnxTensor) optionalValue.get();
            float[][][] predictions = (float[][][]) predictionsTensor.getValue();

            int[] predictedTokenIndices = getTopKIndices(predictions[0][maskTokenIndex], 5);

            String[] topPredictedTokens = new String[5];
            int actualLength = 0; // Variable to keep track of the actual number of predicted tokens
            for (int i = 0; i < Math.min(predictedTokenIndices.length, 5); i++) {
                int predictedTokenId = predictedTokenIndices[i];
                String predictedToken = tokenizer.decode(new long[]{predictedTokenId});

                // Clean the predicted token
                StringBuilder cleanTokenBuilder = new StringBuilder(predictedToken.length());
                for (char c : predictedToken.toCharArray()) {
                    if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                        cleanTokenBuilder.append(c);
                    }
                }
                String cleanedToken = cleanTokenBuilder.toString().trim();

                if (!cleanedToken.isEmpty()) {
                    topPredictedTokens[actualLength] = cleanedToken;
                    actualLength++;
                }
            }

            // If the actual number of predicted tokens is less than 5, fill the remaining slots with an empty string
            for (int i = actualLength; i < topPredictedTokens.length; i++) {
                topPredictedTokens[i] = "";
            }

            // Return the top predicted tokens with a fixed length of 5
            return topPredictedTokens;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }
}

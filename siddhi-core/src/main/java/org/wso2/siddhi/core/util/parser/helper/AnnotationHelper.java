package org.wso2.siddhi.core.util.parser.helper;

import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for annotation
 */
public class AnnotationHelper {

    private static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    public static List<String> generateIncludedMetrics(Element metrics) {
        List<String> regexs = new ArrayList<String>();
        if (metrics != null) {
            String[] metricStrings = metrics.getValue().split(",");
            for (String metricString : metricStrings) {
                String metricStringTrim = metricString.trim();
                if (!metricStringTrim.isEmpty()) {
                    regexs.add(createRegexFromGlob(metricStringTrim));
                }

            }
        }
        if (regexs.size() == 0) {
            regexs.add(createRegexFromGlob("*.*"));
        }
        return regexs;

    }
}

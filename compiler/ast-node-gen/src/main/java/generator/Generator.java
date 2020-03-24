package generator;

import exception.GeneratorException;

import java.io.IOException;

import static generator.FacadeGenerator.generateFacade;
import static generator.InternalGenerator.generateInternalTree;

public class Generator {
    private static String CLASS_PATH = "src/main/java/generated";
    private static String FACADE_TEMPLATE_PATH = "src/main/resources/facadeTemplate.mustache";
    private static String INTERNAL_TEMPLATE_PATH = "src/main/resources/internalTemplate.mustache";
    private static String NODE_JSON_PATH = "src/main/resources/newTree.json";

    public static void main(String[] args) {
        try {
            generateInternalTree(INTERNAL_TEMPLATE_PATH, CLASS_PATH, NODE_JSON_PATH);
            generateFacade(FACADE_TEMPLATE_PATH, CLASS_PATH, NODE_JSON_PATH);
        } catch (IOException | GeneratorException e) {
            e.printStackTrace();
        }
    }
}

package generator;

import exception.GeneratorException;

import java.io.IOException;

import static generator.FacadeGenerator.generateFacade;
import static generator.InternalGenerator.generateInternalTree;

public class Generator {
    private static String CLASS_PATH = "compiler/ast-node-gen/src/main/java/generated";
    private static String FACADE_TEMPLATE_PATH = "compiler/ast-node-gen/src/main/resources/facadeTemplate.mustache";
    private static String INTERNAL_TEMPLATE_PATH = "compiler/ast-node-gen/src/main/resources/internalTemplate.mustache";

    public static void main(String[] args) {
        try {
            generateInternalTree(INTERNAL_TEMPLATE_PATH, CLASS_PATH);
            generateFacade(FACADE_TEMPLATE_PATH, CLASS_PATH);
        } catch (IOException | GeneratorException e) {
            e.printStackTrace();
        }
    }
}

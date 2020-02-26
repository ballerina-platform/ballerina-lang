package generator;

import java.io.IOException;

import static generator.FacadeGenerator.generateFacade;
import static generator.InternalGenerator.generateInternalTree;

public class Generator {

    public static void main(String[] args) {
        try {
            generateInternalTree();
            generateFacade();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

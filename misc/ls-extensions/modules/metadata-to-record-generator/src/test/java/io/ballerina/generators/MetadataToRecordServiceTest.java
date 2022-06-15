package io.ballerina.generators;

import io.ballerina.generators.exceptions.MetadataToRecordGeneratorException;
import io.ballerina.generators.platform.MetadataConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Tests for MetadataToRecordConverter.
 **/
public class MetadataToRecordServiceTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String MetadataToRecordService = "customRecordGenerator/generate";
    private final Path accountMetadataJson = RES_DIR.resolve("json")
            .resolve("account.json");
    private final Path accountMetadataBal = RES_DIR.resolve("ballerina")
            .resolve("account.bal");

    private final Path contactMetadataJson = RES_DIR.resolve("json")
            .resolve("contact.json");
    private final Path contactMetadataBal = RES_DIR.resolve("ballerina")
            .resolve("contact.bal");

    private final Path opportunityMetadataJson = RES_DIR.resolve("json")
            .resolve("opportunity.json");
    private final Path opportunityMetadataBal = RES_DIR.resolve("ballerina")
            .resolve("opportunity.bal");

    @Test(description = "Test with metadata related to account sObject")
    public void testAccountMetadata() throws MetadataToRecordGeneratorException, IOException {
        String jsonFileContent = Files.readString(accountMetadataJson);

        MetadataToRecordConverterFactory factory = new MetadataToRecordConverterFactory();
        MetadataConverter source = factory.getConvert("Salesforce Schema");

        String generatedCodeBlock = source.convert(jsonFileContent).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(accountMetadataBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with metadata related to contact sObject")
    public void testContactMetadata() throws MetadataToRecordGeneratorException, IOException {
        String jsonFileContent = Files.readString(contactMetadataJson);

        MetadataToRecordConverterFactory factory = new MetadataToRecordConverterFactory();
        MetadataConverter source = factory.getConvert("Salesforce Schema");

        String generatedCodeBlock = source.convert(jsonFileContent).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(contactMetadataBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with metadata related to contact sObject")
    public void testOpportunityMetadata() throws MetadataToRecordGeneratorException, IOException {
        String jsonFileContent = Files.readString(opportunityMetadataJson);

        MetadataToRecordConverterFactory factory = new MetadataToRecordConverterFactory();
        MetadataConverter source = factory.getConvert("Salesforce Schema");

        String generatedCodeBlock = source.convert(jsonFileContent).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(opportunityMetadataBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

//    @Test
//    public void testSalesforceRecordTypeGeneration() throws IOException, MetadataToRecordGeneratorException {
//        String file = "src/test/resources/json/opportunity.json";
//        String json = new String(Files.readAllBytes(Paths.get(file)));
//        MetadataToRecordConverterFactory factory = new MetadataToRecordConverterFactory();
//        MetadataConverter source = factory.getConvert("Salesforce Schema");
//        MetadataToRecordSchemaResponse response = source.convert(json);
//    }
}

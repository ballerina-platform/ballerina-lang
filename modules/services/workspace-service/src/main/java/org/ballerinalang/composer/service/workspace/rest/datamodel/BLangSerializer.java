package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
//import org.ballerinalang.model.tree.FunctionNode;

import java.io.IOException;

/**
 * BLangSerializer
 */
public class BLangSerializer extends JsonSerializer {

    public void serialize(Object o, JsonGenerator jGen, SerializerProvider serializerProvider) throws IOException {
        //((FunctionNode)o).getPosition();
        jGen.writeStartObject();
        //jGen.writeStringField("start", ((FunctionNode)o).getPosition().toString());
        jGen.writeStringField("end", "Sumuditha");
        jGen.writeEndObject();
    }
}

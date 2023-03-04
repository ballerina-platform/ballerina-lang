package org.ballerinalang.bindgen;

import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class BindgenUnitTest extends BindgenUnitTestBase {

    @Override
    @Test
    public void constructorMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.constructorMapping();
    }

    @Override
    @Test
    public void methodMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.methodMapping();
    }

    @Override
    @Test
    public void fieldMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.fieldMapping();
    }

    @Override
    @Test
    public void errorMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.errorMapping();
    }

    @Override
    @Test
    public void innerClassMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.innerClassMapping();
    }

    @Override
    @Test
    public void moduleLevelMapping1() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.moduleLevelMapping1();
    }

    @Override
    @Test
    public void moduleLevelMapping2() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.moduleLevelMapping2();
    }

    @Override
    @Test
    public void dependentClassMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.dependentClassMapping();
    }

    @Override
    @Test
    public void directThrowableMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.directThrowableMapping();
    }

    @Override
    @Test
    public void dependentInnerClassMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.dependentInnerClassMapping();
    }

    @Override
    @Test
    public void interfaceMapping() throws FormatterException, ClassNotFoundException, BindgenException, IOException {
        super.interfaceMapping();
    }
}

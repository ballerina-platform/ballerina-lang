package org.ballerinalang.launcher.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * 
 * Validates archive type parameter
 *
 */
public class ArchiveTypeValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!value.equals("lib") && !value.equals("main") && !value.equals("service")) {
            throw new ParameterException("Parameter " + name + " should be one of 'lib' 'main' or 'service' ");
        }
    }

}

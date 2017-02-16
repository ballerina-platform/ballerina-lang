package org.ballerinalang.containers.docker.cmd.validator;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * 
 * Validates Docker Host URL pattern
 *
 */
public class DockerHostValidator implements IParameterValidator{

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!isURL(value)) {
            throw new ParameterException("Parameter " + name + " should be a valid URL");
        }        
    }
    
    private boolean isURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

}

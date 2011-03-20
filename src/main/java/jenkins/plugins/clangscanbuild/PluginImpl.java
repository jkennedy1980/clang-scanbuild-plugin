package jenkins.plugins.clangscanbuild;

import hudson.Plugin;

import java.io.IOException;

import org.xml.sax.SAXException;

public class PluginImpl extends Plugin{
	
	public static final String SHORTNAME = "clangscanbuild";
	
    @Override
    public void start() throws IOException, SAXException {
    	// For future use
    }
    
}

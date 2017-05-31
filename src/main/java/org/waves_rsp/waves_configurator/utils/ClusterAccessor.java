package org.waves_rsp.waves_configurator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.waves_rsp.waves_configurator.exceptions.ProjectNotExistException;

public class ClusterAccessor {
	
    private final static Logger log = LogManager.getLogger();
    private static Properties properties = new Properties();
    private TripleStoreAccessor tsAccessor = new TripleStoreAccessor();

    public ClusterAccessor() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("configurator.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void submitProject(String projectName) throws ProjectNotExistException {
        if (projectName == null) {
            throw new NullPointerException("The project you want to submit to cluster doesn't exist");
        }
        if (!tsAccessor.exists(projectName)) {
            throw new ProjectNotExistException();
        }
        
		log.info("Submitting project: " + projectName);
		String location = tsAccessor.getProjectLocation(projectName);
		
	}
	
	String sshCmd(){
		String command;
		// Get the properties
		String localSsh = properties.getProperty("LocalSsh");
		String uname = properties.getProperty("ClusterMasterUname");
		String ip = properties.getProperty("ClusterMasterIP");
    	String system = System.getProperty("os.name");
    	// Create the ssh command
    	command = localSsh + ' ' + uname + "@" + ip;
		return command;
	}
	
	String runCmd(String trigLocation, String filterName){
		return "bash run-waves.sh " + trigLocation + filterName;
	}
	
	String stopCmd(String trigLocation, String filterName){
		return "bash stop-waves.sh " + trigLocation + filterName;
	}
}

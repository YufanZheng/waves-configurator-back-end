package org.waves_rsp.waves_configurator.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ProjectNotExistException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ClusterAccessor {
	
    private final static Logger log = LogManager.getLogger();
    private static Properties properties = new Properties();
    private TripleStoreAccessor tsAccessor = new TripleStoreAccessor();
    private RDFHandler trigHandler = new RDFHandler();
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    
    public ClusterAccessor() {
        InputStream is = getClass().getClassLoader()
        		.getResourceAsStream("configurator.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ------------------------------------------------------------------------
    // Execute remote command to run / stop Waves program
    // ------------------------------------------------------------------------
    
    /**
     * Execute remote command via SSH 
     * 
     * @param hostname	
     * @param username
     * @param password
     * @param cmd
     * @return
     */
    public ArrayList<String> cmdViaSSH(String hostname, String username, String password, String cmd) {
        ArrayList<String> stdout = new ArrayList<String>();
        try
        {
            Connection conn = new Connection(hostname);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false) {
                return null;
            }
            Session sess = conn.openSession();
            sess.execCommand(cmd);
            
            InputStream is = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true)
            {
                String line = br.readLine();
                if (line == null)
                    break;
                stdout.add(line);
            }
            sess.close();
            conn.close();
        }
        catch (Exception e)
        {
            return null;
        }
        
        return stdout;
    }
    
    // ------------------------------------------------------------------------
    // Run and stop waves project
    // ------------------------------------------------------------------------
	
    /**
     * Run project in cluster
     * @param projectName
     * @throws MultiGraphsException 
     * @throws ZeroGraphException 
     * @throws Exception
     */
	public ArrayList<String> runProject(String projectName) throws ZeroGraphException, MultiGraphsException, Exception {
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!tsAccessor.exists(projectName)) {
            throw new ProjectNotExistException();
        }
        
		String trigLocation = tsAccessor.getProjectLocation(projectName);
		ArrayList< String > filterNames = trigHandler.extractFilterNames( tsAccessor.getProjectDataset(projectName) );
		
		if( filterNames == null || filterNames.get(0) == null ) {
			log.error("No filter name in the TriG configuration.");
			return null;	
		}
		
		log.info("Submitting project: " + projectName);
		String cmd = getRunCmd(trigLocation, filterNames.get(0));
		log.info("Execute command: " + cmd );
		ArrayList<String> stdout = cmdViaSSH(
					properties.getProperty("remote.host"),
					properties.getProperty("remote.username"),
					properties.getProperty("remote.password"),
					cmd
				);
		
		return stdout;
	}

    // ------------------------------------------------------------------------
    // Get the commands to execute Waves project 
    // ------------------------------------------------------------------------
	
	/**
	 * Command to run waves project in the cluster
	 * @param trigLocation	The location where project configuration is stored
	 * @param filterName	The filter's name
	 * @return
	 */
	String getRunCmd(String trigLocation, String filterName){
		return "bash run-waves.sh " + 
				properties.getProperty("remote.jarLocation") + " " +
				properties.getProperty("remote.mainClass") + " " +
				trigLocation + " " +
				filterName;
	}
	
}

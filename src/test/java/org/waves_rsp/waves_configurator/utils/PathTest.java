package org.waves_rsp.waves_configurator.utils;

import static org.waves_rsp.fwk.Configuration.APP_CONFIG_PATH_PROP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.jena.atlas.logging.Log;
import org.junit.Test;
import org.waves_rsp.fwk.Configuration;
import org.waves_rsp.sesame.RdfConfiguration;

public class PathTest {

	@Test public void PathTest() throws IOException{
		String path = "http://localhost:3030/wavesRepo/data?graph=http://localhost:9091/waves/first-tests";

		RdfConfiguration rcfg = new RdfConfiguration();
		rcfg.init(path, null);
		System.out.println(rcfg);
		
        URL url = new URL(path);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String location = "";
        //create a temp file
	    File temp = new File("temp.trig");
	    location = temp.getPath();
    	try{
    	    System.out.println("File path: " + temp.getPath());
    	    BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
    	    //write it
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                bw.write(inputLine);
            }
            in.close();
            bw.close();
    	    System.out.println("Write into file Done");
    	}catch(IOException e){
    	    e.printStackTrace();
    	}
    	
    	System.out.println(location);
	    System.setProperty(APP_CONFIG_PATH_PROP, location);
	    Configuration cfg = Configuration.getDefault();
	    
	    // Comment tu iterate le configuration? println
	    System.out.println("Printing Configuration: " + cfg);
	    temp.delete();
	}
}

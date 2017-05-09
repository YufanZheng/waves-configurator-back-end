package org.waves_rsp.waves_configurator;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "project-data" path)
 * Handle all the request about Waves project data 
 */
@Path("project-data")
public class ProjectDataResource {
	
    /**
     * Method handling HTTP POST requests. 
     * Client POST a TriG Configuration in String format.
     * The received string will be converted into RDF and saved in triple store.
     */
    @POST
    @Path("load-trig")
    @Consumes(MediaType.TEXT_PLAIN)
    public void loadTriG(String trig) {
    	System.out.println(trig);
    }

}

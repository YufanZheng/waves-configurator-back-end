package org.waves_rsp.waves_configurator;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.waves_rsp.waves_configurator.utils.ClusterAccessor;

@Path("project-execution")
public class ProjectExecutionResource {

	private final static Logger log = LogManager.getLogger();
	private ClusterAccessor clAccessor = new ClusterAccessor();
	
    @POST
    @Path("run")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response run(String projectName){
    	log.info("Running project " + projectName);
    	Boolean success;
    	String logs = "";
    	String errorMessage = "";
    	
    	try {
			ArrayList<String> stdout = clAccessor.runProject(projectName);
			success = true;
			for( String s : stdout ){
				logs += s + "\n"; 
			}
		} catch (Exception e) {
			success = false;
			errorMessage = "ERROR: Failed submitting project into cluster. Check the server side and cluster for debugging.";
			log.error(errorMessage);
			// Possible reasons that cause the connections not work
			// 1. The cluster is not set up (The Storm cluster doesn't exists)
			// 2. The configuration of the cluster is not correct, see src/main/resources/configurator.properties
			e.printStackTrace();
		}
    	
        // Step 2: Create response entity to send back to client side
        JSONObject entity = new JSONObject();
        try {
            entity.put("success", success);
            if (success) {
                entity.put("logs", logs);
            } else {
                entity.put("errorMessage", errorMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity.toString()).build();
    	
    }
    
}

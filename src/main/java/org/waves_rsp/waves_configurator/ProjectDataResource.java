package org.waves_rsp.waves_configurator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ProjectNotExistException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;
import org.waves_rsp.waves_configurator.utils.RDFHandler;
import org.waves_rsp.waves_configurator.utils.TripleStoreAccessor;

/**
 * Root resource (exposed at "project-data" path)
 * Handle all the request about Waves project data 
 */
@Path("project-data")
public class ProjectDataResource {

    private final static Logger log = LogManager.getLogger();
    private TripleStoreAccessor tsAccessor = new TripleStoreAccessor();
    private RDFHandler trigHandler = new RDFHandler();

    /**
     * Method handling HTTP POST requests. 
     * Client POST a TriG Configuration in String format.
     * The received string will be converted into RDF and saved in triple store.
     * @throws InterruptedException 
     */
    @POST
    @Path("load-trig")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadTriG(String trig) {
        log.info("Recieve the Trig Configuration from Client :\n\n" + trig);
        
        // Step 1: Convert TriG String to Jena RDF Model and Put it into Triple Store
        Boolean success = false;
        String graphLocation = "";
        String projectName = ""; 
        String errorMessage = "";
        
        try {
            // Step 1.1 : Convert TriG String to Jena RDF Dataset 
            Dataset dataset = trigHandler.parseToDataset(trig);
            String graphUri = trigHandler.extractBaseUri(dataset);
            Model graphModel = trigHandler.extractGraphModel(dataset);
            projectName = trigHandler.extractProjectnName(dataset);
            
            // Step 1.2 : Put data into Triple Store
            if( tsAccessor.exists(projectName) ){
            	tsAccessor.removeProject(projectName);
            }
            graphLocation = tsAccessor.addNewProject(graphUri, graphModel);
            
            // Step 1.3 : If all previous steps throw no exceptions, the loading process is successed.
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = getErrorMessage(e);
        }
        
        // Step 2: Create response entity to send back to client side
        JSONObject entity = new JSONObject();
        try {
            entity.put("success", success);
            if (success) {
                entity.put("graphLocation", graphLocation);
                entity.put("projectName", projectName);
                log.info("The loading processus is successed.");
            } else {
                entity.put("errorMessage", errorMessage);
                log.error("The loading processus failed. " + errorMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity.toString()).build();
    }

    /**
     * Method handling HTTP GET requests. 
     * Client Send a GET Request to get all the project names
     * Return a json object contains list of project names as a String array
     * @throws InterruptedException 
     */
    @GET
    @Path("project-list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectList() {
        // Step 1: Fetch all the project names from Triple Store
        ArrayList < String > listProjectNames = tsAccessor.getListProjectNames();
        // Step 2: Send the project name list to client
        JSONObject entity = new JSONObject();
        JSONArray projects = new JSONArray(listProjectNames);
        try {
            entity.put("projects", projects);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log.info("Sending Response with project list to the Client side");
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity.toString()).build();
    }

    /**
     * Get project information
     * @param projectName
     * @return
     * @throws Exception
     */
    @POST
    @Path("project-info")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectInfo(String projectName) {
        JSONObject entity = new JSONObject();
        Boolean success = false; /* Flag to tell if the conversion step is success or not */
        String errorMessage = ""; /* If conversion is failed, send to client the error message */
        log.info("Retriving project information.");
        try {
        	log.info("Get the project location for project: " + projectName);
        	String location = tsAccessor.getProjectLocation(projectName);
        	log.info("Get configuration properties at path: " + location);
        	Properties cfg = tsAccessor.getProjectConfig(projectName);
        	log.info("Printing Configuration: " + cfg);
        	success = true;
        	// Put project properties into entity response
        	entity.put("location", location);
        	entity.put("properties", toJsonArray(cfg));
        } catch (JSONException | ProjectNotExistException | IOException e) {
            e.printStackTrace();
            errorMessage = getErrorMessage(e);
        }
        try {
            entity.put("success", success);
            if (!success) {
                entity.put("errorMessage", errorMessage);
                log.error("Failed to get project information");
                log.error(errorMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity.toString()).build();
    }
    
    private JSONArray toJsonArray(Properties properties) {
    	ArrayList<JSONObject> array = new ArrayList<JSONObject>();
    	Enumeration<?> en = properties.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            JSONObject o = new JSONObject();
            try {
				o.put(key, properties.getProperty(key));
			} catch (JSONException e) { e.printStackTrace(); }
            array.add(o);
        }
    	return new JSONArray(array);
    }
    
    private String getErrorMessage(Exception e){
    	String errorMessage = "";
    	if (e instanceof JenaTransactionException) {
            errorMessage = "ERROR: Cannot parse invalid TriG";
        } else if (e instanceof ZeroGraphException) {
            errorMessage = "ERROR: This TriG contains no graph";
        } else if (e instanceof MultiGraphsException) {
            errorMessage = "ERROR: This TriG contains more than one graph";
        } else if (e instanceof HttpException) {
            errorMessage = "ERROR: Can not connect to Triple Store, please check if the triple store is well started or there is a proxy in network";
        }  else if (e instanceof ProjectNotExistException) {
            errorMessage = "ERROR: Project doesn't exist";
        } else {
            errorMessage = "Unknown Error: Check the server to debug";
        }
    	return errorMessage;
    }

}
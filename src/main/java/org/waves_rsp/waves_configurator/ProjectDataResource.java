package org.waves_rsp.waves_configurator;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;

/**
 * Root resource (exposed at "project-data" path)
 * Handle all the request about Waves project data 
 */
@Path("project-data")
public class ProjectDataResource {
	
	private final static Logger log = LogManager.getLogger();
	private TripleStoreAccessor tsAccessor = new TripleStoreAccessor();
	
    /**
     * Method handling HTTP POST requests. 
     * Client POST a TriG Configuration in String format.
     * The received string will be converted into RDF and saved in triple store.
     */
    @POST
    @Path("load-trig")
    @Consumes(MediaType.TEXT_PLAIN)
    public void loadTriG(String trig) {
    	log.info("Recieve the Trig Configuration from Client :\n\n" + trig);
		try {
			// Convert TriG String to Jena RDF Model and Put it into Triple Store
			DatasetGraph graph = tsAccessor.convert(trig);
			String graphUri = tsAccessor.getGraphUri(graph);
			Model graphModel = tsAccessor.getGraphModel(graph);
			String graphLocation = tsAccessor.put(graphUri, graphModel);
		} catch (Exception e) {
			e.printStackTrace();
			if( e instanceof ZeroGraphException ){}
			if( e instanceof MultiGraphsException ){}
			if( e instanceof ZeroGraphException ){}
			if( e instanceof ZeroGraphException ){}
		}
    	System.out.println(trig);
    }

}

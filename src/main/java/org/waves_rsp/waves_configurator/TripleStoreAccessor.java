package org.waves_rsp.waves_configurator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;

/**
 * Triple Store Accessor is the only entry point 
 *  to the triple store where we store the configurations
 *  
 * @author yufan
 *
 */
public class TripleStoreAccessor {
	
	private final static Logger log = LogManager.getLogger();
	private static Properties properties = new Properties();
	
	TripleStoreAccessor(){
		InputStream is = getClass().getClassLoader().getResourceAsStream("configurator.properties");
		try {
			properties.load(is);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * Convert TriG in String Format into JENA Model for the use of pushing into Triple Store
	 * 
	 * @param trig
	 * @return model
	 * @throws ZeroGraphException 
	 * @throws MultiGraphsException 
	 */
	public DatasetGraph convert(String trig) throws ZeroGraphException, MultiGraphsException{
		// Step 1:  Read the String into a Dataset Graph
		StringReader reader = new StringReader(trig);
		log.info("Converting the TriG String into RDF Graph");
		Dataset dataset = DatasetFactory.create();
		dataset.begin(ReadWrite.WRITE) ; 
		try { 
            RDFDataMgr.read(dataset.asDatasetGraph(), reader, "", Lang.TRIG); 
            dataset.commit(); 
        } 
        finally { 
            dataset.end(); 
        }
		DatasetGraph datasetGraph = dataset.asDatasetGraph();
		// Step 2: Exception handling
		if( datasetGraph.size() == 0 ){
			log.error("Invalid TriG: it contains only zero graph");
			throw new ZeroGraphException();
		}
		if( datasetGraph.size() >= 2 ){
			log.error("Invalid TriG: it contains more than one graphs");
			throw new MultiGraphsException();
		}
		return datasetGraph;
	}
	
	public String getGraphUri(DatasetGraph singleGraphDataset) throws Exception{
		if( singleGraphDataset.size() != 1 ){
			throw new Exception("This function could only get Graph URI of single dataset graph.");
		}
		log.info("Getting the base URI of TriG graph...");
		Node graphNode = singleGraphDataset.listGraphNodes().next();
		String graphUri = graphNode.toString();
		log.info("The base URI of TriG graph is:" + graphUri);
		return graphUri;
	}
	
	public Model getGraphModel(DatasetGraph singleGraphDataset) throws Exception{
		if( singleGraphDataset.size() != 1 ){
			throw new Exception("This function could only get Graph Model of single dataset graph.");
		}
		log.info("Getting the RDF Model of TriG graph...");
		Node graphNode = singleGraphDataset.listGraphNodes().next();
		Graph graph = singleGraphDataset.getGraph(graphNode);
		Model model = ModelFactory.createModelForGraph(graph);
		log.info("The TriG Graph contains " + model.size() + "triples.");
		return model;
	}
	
	public String put(String graphUri, Model graphModel){
		String tsLocation = properties.getProperty("TripleStoreLocation");
		log.info("Creating an accessor for remote triple store: " + tsLocation);
		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(tsLocation);
		log.info("Pushing TriG graph into Triple Store...");
		accessor.putModel(graphUri, graphModel);
		String graphLocation = tsLocation + "/data?graph=" + graphUri;
		log.info("The TriG Graph is located at : " + graphLocation);
		return graphLocation;
	}
}

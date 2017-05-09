package org.waves_rsp.waves_configurator;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.tdb.base.file.Location;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Triple Store Accessor is the only entry point 
 *  to the triple store where we store the configurations
 *  
 * @author yufan
 *
 */
public class TripleStoreAccessor {
	
	private final String tsLocation = "http://localhost:3030";
	private final static Logger log = LogManager.getLogger();
	
	/**
	 * Convert TriG in String Format into JENA Model for the use of pushing into Triple Store
	 * 
	 * @param trig
	 * @return model
	 */
	public Model convertToModel(String trig){
		// Step 1: Read the TriG String into Jena Dataset
		StringReader reader = new StringReader(trig);
		Dataset dataset = DatasetFactory.create();
		dataset.begin(ReadWrite.WRITE) ; 
		try { 
            RDFDataMgr.read(dataset.asDatasetGraph(), reader, "<http://www.example.org/exampleDocument#>", Lang.TRIG); 
            dataset.commit(); 
        } 
        finally { 
            dataset.end(); 
        }
		// Step 2: Read the graph data from dataset
		Node graphNode = dataset.asDatasetGraph().listGraphNodes().next();
		Graph graph = dataset.asDatasetGraph().getGraph(graphNode);
		// Step3: Build the Jena model from graph
		Model model = ModelFactory.createModelForGraph(graph);
		return model;
	}

}

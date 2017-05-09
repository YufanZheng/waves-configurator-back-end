package org.waves_rsp.waves_configurator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
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
		Model model = ModelFactory.createDefaultModel();
		try( final InputStream in = new ByteArrayInputStream(trig.getBytes("UTF-8")) ) {
			// Read the TriG in string Format into Jena Model format
			log.info("Converting TriG Configuration string into Jena Model");
		    model.read(in, null, "TRIG");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return model;
	}

}

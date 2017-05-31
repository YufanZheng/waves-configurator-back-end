package org.waves_rsp.waves_configurator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.waves_rsp.sesame.RdfConfiguration;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ProjectNotExistException;
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
    private RDFHandler trigHandler = new RDFHandler();

    public TripleStoreAccessor() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("configurator.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ------------------------------------------------------------------------
    // Push project configuration into Triple Store
    // ------------------------------------------------------------------------

    /**
     * Put the graph into remote Triple Store 
     * @param graphUri		Graph URI
     * @param graphModel	Graph Model data
     * @return				The location of graph
     */
    public String addNewProject(String graphUri, Model graphModel) {
        String tsLocation = properties.getProperty("TripleStoreLocation");
        log.info("Creating an accessor for remote triple store: " + tsLocation);
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(tsLocation);
        log.info("Pushing TriG graph into Triple Store...");
        accessor.putModel(graphUri, graphModel);
        String graphLocation = tsLocation + "/data?graph=" + graphUri;
        log.info("The TriG Graph is located at : " + graphLocation);
        return graphLocation;
    }
    
    // ------------------------------------------------------------------------
    // Access triple store the get the information for all projects
    // ------------------------------------------------------------------------

    public Dataset getAllProjectDatasets() {
        log.info("Getting all the project graphs...");
        String tsLocation = properties.getProperty("TripleStoreLocation");
        Dataset dataset = RDFDataMgr.loadDataset(tsLocation);
        return dataset;
    }

    public ArrayList < String > getListProjectNames() {
        Dataset allProjects = getAllProjectDatasets();
        Iterator < Node > listProjectNodes = trigHandler.listProjectNodes(allProjects);
        ArrayList < String > listProjectNames = trigHandler.listProjectNames(listProjectNodes);
        return listProjectNames;
    }
    
    // ------------------------------------------------------------------------
    // Fetch specific project data from the project name 
    // ------------------------------------------------------------------------
    
    public Dataset getProjectDataset(String projectName) throws ProjectNotExistException, ZeroGraphException, MultiGraphsException {
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!exists(projectName)) {
            throw new ProjectNotExistException();
        }
        DatasetGraph dsGraph = DatasetGraphFactory.create();
        Graph projectGraph = getProjectGraph(projectName);
        Node projectGraphNode = getGraphNode(projectName);
        dsGraph.addGraph(projectGraphNode, projectGraph);
        return DatasetFactory.wrap(dsGraph);
    }
    
    public String getProjectLocation(String projectName) throws ProjectNotExistException {
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!exists(projectName)) {
            throw new ProjectNotExistException();
        }
        String url = properties.getProperty("TripleStoreLocation") + "/data?graph=" +
            properties.getProperty("WavesProjectPrefix") + projectName;
        return url;
    }
    
    public Graph getProjectGraph(String projectName) throws ProjectNotExistException {
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!exists(projectName)) {
            throw new ProjectNotExistException();
        }
        Node graphNode = getGraphNode(projectName);
        DatasetGraph allProjectGraphs = getAllProjectDatasets().asDatasetGraph();
        Graph projectGraph = allProjectGraphs.getGraph(graphNode);
        System.out.println(projectGraph);
        return projectGraph;
    }
    
    public Node getGraphNode(String projectName) throws ProjectNotExistException {
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!exists(projectName)) {
            throw new ProjectNotExistException();
        }
        String nodeString = properties.getProperty("WavesProjectPrefix") + projectName;
        Node graphNode = NodeFactory.createURI(nodeString);
        return graphNode;
    }
    
    public Properties getProjectConfig(String projectName) throws IOException, ProjectNotExistException{
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!exists(projectName)) {
            throw new ProjectNotExistException();
        }
    	String location = getProjectLocation(projectName);
    	log.info("Getting project information at: " + location );
		RdfConfiguration rcfg = new RdfConfiguration();
		rcfg.init(location, null);
    	return rcfg;
    }
    
    // ------------------------------------------------------------------------
    // Remove project
    // ------------------------------------------------------------------------
    
    public void removeProject(String projectName) throws Exception{
        if (projectName == null) {
            throw new NullPointerException();
        }
        if (!exists(projectName)) {
            throw new ProjectNotExistException();
        }
        Dataset dataset = getProjectDataset(projectName);
        String graphUri = trigHandler.extractBaseUri(dataset);
        log.info("Removing the dataset for graph: " + graphUri);
        this.getAllProjectDatasets().removeNamedModel(graphUri);
    }
    
    // ------------------------------------------------------------------------
    // Check if project exists
    // ------------------------------------------------------------------------

    public Boolean exists(String projectName) {
        return getListProjectNames().contains(projectName);
    }
}
package org.waves_rsp.waves_configurator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;

/**
 * RDFHandler is to process the TriG: parsing the data, extracting the information etc.
 * 
 * @author yufan
 *
 */
public class RDFHandler {

    private final static Logger log = LogManager.getLogger();
    private static Properties properties = new Properties();

    public RDFHandler() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("configurator.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------+
     |              			Parsing function        		            |
     +----------------------------------------------------------------------*/

    /**
     * Convert TriG in String Format into JENA Dataset
     * @param trig	TriG in String Format 
     * @return		TriG in Jena RDF Dataset Format
     * @throws ZeroGraphException		TriG contains zero graph
     * @throws MultiGraphsException		TriG contains more than one graphs
     */
    public Dataset toDataset(String trig) throws ZeroGraphException, MultiGraphsException {
        if (trig == null) {
            throw new NullPointerException();
        }
        // Step 1:  Read the String into a Dataset Graph
        StringReader reader = new StringReader(trig);
        log.info("Converting the TriG String into RDF Graph");
        Dataset dataset = DatasetFactory.create();
        dataset.begin(ReadWrite.WRITE);
        try {
            RDFDataMgr.read(dataset.asDatasetGraph(), reader, "", Lang.TRIG);
            dataset.commit();
        } finally {
            dataset.end();
        }
        // Step 2: Exception handling
        DatasetGraph dsGraph = dataset.asDatasetGraph();
        if (dsGraph.size() == 0) {
            log.error("Invalid TriG: it contains only zero graph");
            throw new ZeroGraphException();
        }
        if (dsGraph.size() >= 2) {
            log.error("Invalid TriG: it contains more than one graphs");
            throw new MultiGraphsException();
        }
        return dataset;
    }

    public Dataset toDataset(Graph trig) throws ZeroGraphException, MultiGraphsException {
        if (trig == null) {
            throw new NullPointerException();
        }
        DatasetGraph dsGraph = DatasetGraphFactory.create(trig);
        if (dsGraph.size() == 0) {
            log.error("Invalid TriG: it contains only zero graph");
            throw new ZeroGraphException();
        }
        if (dsGraph.size() >= 2) {
            log.error("Invalid TriG: it contains more than one graphs");
            throw new MultiGraphsException();
        }
        Dataset dataset = DatasetFactory.wrap(dsGraph);
        return dataset;
    }

    public Node toGraphNode(String projectName) {
        if (projectName == null) {
            throw new NullPointerException("Cannot parse an null project name to TriG graph node");
        }
        String nodeString = properties.getProperty("WavesProjectPrefix") + projectName;
        Node graphNode = NodeFactory.createURI(nodeString);
        return graphNode;
    }

    /*----------------------------------------------------------------------+
     |            Handling for one project graph dataset        	        |
     +----------------------------------------------------------------------*/

    /**
     * Get the base Graph URI of single graph data-set
     * @param trig			Single Graph Dataset
     * @return				Base Graph URI
     * @throws Exception	If the dataset graph doesn't contain only one graph 
     */
    public String getBaseUri(Dataset trig) throws Exception {
        if (trig == null) {
            throw new NullPointerException();
        }
        // Check if the trig contains only one graph
        DatasetGraph dsGraph = trig.asDatasetGraph();
        if (dsGraph.size() == 0) {
            throw new ZeroGraphException();
        }
        if (dsGraph.size() >= 2) {
            throw new MultiGraphsException();
        }
        log.info("Getting the base URI of TriG graph...");
        Node graphNode = dsGraph.listGraphNodes().next();
        String graphUri = graphNode.toString();
        log.info("The base URI of TriG graph is:" + graphUri);
        return graphUri;
    }

    /**
     * Get the Graph Model from single graph dataset
     * @param trig			Single Graph Dataset
     * @return				The Jena Model inside Graph
     * @throws Exception	If the dataset graph doesn't contain only one graph 
     */
    public Model getGraphModel(Dataset trig) throws Exception {
        if (trig == null) {
            throw new NullPointerException();
        }
        // Check if the trig contains only one graph
        DatasetGraph dsGraph = trig.asDatasetGraph();
        if (dsGraph.size() == 0) {
            throw new ZeroGraphException();
        }
        if (dsGraph.size() >= 2) {
            throw new MultiGraphsException();
        }
        log.info("Getting the RDF Model of TriG graph...");
        Node graphNode = dsGraph.listGraphNodes().next();
        Graph graph = dsGraph.getGraph(graphNode);
        Model model = ModelFactory.createModelForGraph(graph);
        log.info("The TriG Graph contains " + model.size() + " triples.");
        return model;
    }

    /**
     * Using SPARQL query to get the project name from DatasetGraph
     * @param singleGraphModel		Single Graph model
     * @return						Project Name
     * @throws Exception			If the dataset graph doesn't contain only one graph 
     */
    public String getProjectnName(Dataset trig) throws Exception {
        if (trig == null) {
            throw new NullPointerException();
        }
        // Check if the trig contains only one graph
        DatasetGraph dsGraph = trig.asDatasetGraph();
        if (dsGraph.size() == 0) {
            throw new ZeroGraphException();
        }
        if (dsGraph.size() >= 2) {
            throw new MultiGraphsException();
        }
        String projectName = "";
        String queryString =
            "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "prefix waves:  <http://www.waves-rsp.org/configuration#>\n" +
            "prefix rdfs:	<http://www.w3.org/2000/01/rdf-schema#>\n" +
            "\n" +
            "SELECT ?name \n" +
            "WHERE {\n" +
            "  ?installation rdf:type waves:Installation.\n" +
            "  ?installation waves:name ?name.\n" +
            "}";
        Query query = QueryFactory.create(queryString);
        Node graphNode = dsGraph.listGraphNodes().next();
        Graph graph = dsGraph.getGraph(graphNode);
        Model model = ModelFactory.createModelForGraph(graph);
        log.info("Executing SPARQL Query to get project name.");
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("name"); // Get a result variable - must be a literal
                projectName = name.getString();
            }
        }
        log.info("Project name of TriG graph is: " + projectName);
        return projectName;
    }

    /*----------------------------------------------------------------------+
     |         Extracting information from whole project dataset            |
     +----------------------------------------------------------------------*/

    public Iterator < Node > listProjectNodes(Dataset allProjectDatasets) {
        if (allProjectDatasets == null) {
            throw new NullPointerException("Cannot extract list project nodes from null Datasets.");
        }
        DatasetGraph allProjectGraphs = allProjectDatasets.asDatasetGraph();
        Iterator < Node > listGraphNodes = allProjectGraphs.listGraphNodes();
        log.info("Extracting all the graph nodes from datasets...");
        return listGraphNodes;
    }

    public ArrayList < String > listProjectNames(Iterator < Node > listGraphNodes) {
        if (listGraphNodes == null) {
            throw new NullPointerException("Cannot extract list project names from null list Graph Nodes.");
        }
        ArrayList < String > listProjectNames = new ArrayList < String > ();
        log.info("Extracting all the project names from graphs...");
        while (listGraphNodes.hasNext()) {
            String projectName = listGraphNodes.next().toString()
                .replace(properties.getProperty("WavesProjectPrefix").trim(), "");
            listProjectNames.add(projectName);
        }
        return listProjectNames;
    }

}
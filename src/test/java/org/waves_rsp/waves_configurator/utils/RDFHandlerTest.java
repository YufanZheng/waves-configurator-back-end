package org.waves_rsp.waves_configurator.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.Test;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;

public class RDFHandlerTest {

    public RDFHandler handler = new RDFHandler();
    private Dataset zeroGraphDataset = loadDataset("zero-graph.trig");
    private Dataset singleGraphDataset = loadDataset("single-graph.trig");
    private Dataset multiGraphsDataset = loadDataset("multi-graphs.trig");

    /*----------------------------------------------------------------------+
     |                 Tests for toDataset() function                       |
     +----------------------------------------------------------------------*/

    @Test public void parseSingleGraphTrig() {
        String singleGraphTrig = readFile("single-graph.trig");
        System.out.println("\n-------- Run test for convert one graph TriG string to Jena model --------\n");
        System.out.println("The trig string below is used for test:\n");
        System.out.print(singleGraphTrig + "\n");
        DatasetGraph graph = null;
        try {
            Dataset dataset = handler.toDataset(singleGraphTrig);
            graph = dataset.asDatasetGraph();
        } catch (Exception e) {} finally {
            assertTrue(graph.size() == 1);
        }
    }

    @Test public void parseZeroGraphTrig() {
        String zeroGraphTrig = readFile("zero-graph.trig");
        System.out.println("\n-------- Run test for convert zero graph TriG string to Jena model --------\n");
        System.out.println("The empty trig string is used for test:\n");
        try {
            handler.toDataset(zeroGraphTrig);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof ZeroGraphException);
        }
    }

    @Test public void parseMultiGraphsTrig() {
        String multiGraphsTrig = readFile("multi-graphs.trig");
        System.out.println("\n-------- Run test for convert multi graphs TriG string to Jena model --------\n");
        System.out.println("The trig string below is used for test:\n");
        System.out.print(multiGraphsTrig + "\n");
        try {
            handler.toDataset(multiGraphsTrig);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof MultiGraphsException);
        }
    }

    @Test public void parseInvalidTrig() {
        String invalidTrig = readFile("invalid-graph.trig");
        System.out.println("\n-------- Run test for convert invalid TriG string to Jena model --------\n");
        System.out.println("The trig string below is used for test:\n");
        System.out.print(invalidTrig + "\n");
        try {
            handler.toDataset(invalidTrig);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof JenaTransactionException);
        }
    }

    @Test public void parseNullTrig() {
        String nullTrig = null;
        System.out.println("\n-------- Run test for convert null TriG string to Jena model --------\n");
        System.out.println("The trig string below is used for test:\n");
        System.out.print(nullTrig + "\n");
        try {
            handler.toDataset(nullTrig);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    /*----------------------------------------------------------------------+
     |                Tests for getBaseUri() function                       |
     +----------------------------------------------------------------------*/

    @Test public void getNullGraphBaseUri() {
        System.out.println("\n-------- Run test for get base graph URI for null TriG --------\n");
        try {
            handler.getBaseUri(null);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test public void getZeroGraphBaseUri() {
        System.out.println("\n-------- Run test for get base graph URI for zero graph TriG --------\n");
        try {
            handler.getBaseUri(zeroGraphDataset);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof ZeroGraphException);
        }
    }

    @Test public void getSingleGraphBaseUri() {
        System.out.println("\n-------- Run test for get base graph URI for single graph TriG --------\n");
        String graphUri = null;
        try {
            graphUri = handler.getBaseUri(singleGraphDataset);
        } catch (Exception e) {} finally {
            assertTrue(!graphUri.isEmpty());
            System.out.println("Base Graph URI is: " + graphUri);
        }
    }

    @Test public void getMultiGraphBaseUri() {
        System.out.println("\n-------- Run test for get base graph URI for multi graphs TriG --------\n");
        try {
            handler.getBaseUri(multiGraphsDataset);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof MultiGraphsException);
        }
    }

    /*----------------------------------------------------------------------+
     |                Tests for getGraphModel() function                    |
     +----------------------------------------------------------------------*/

    @Test public void getNullGraphModel() {
        System.out.println("\n-------- Run test for get graph model URI for null TriG --------\n");
        try {
            handler.getGraphModel(null);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test public void getZeroGraphModel() {
        System.out.println("\n-------- Run test for get graph model for zero graph TriG --------\n");
        try {
            handler.getGraphModel(zeroGraphDataset);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof ZeroGraphException);
        }
    }

    @Test public void getSingleGraphModel() {
        System.out.println("\n-------- Run test for get graph model for single graph TriG --------\n");
        Model model = null;
        try {
            model = handler.getGraphModel(singleGraphDataset);
        } catch (Exception e) {} finally {
            assertTrue(model.size() != 0);
        }
    }

    @Test public void getMultiGraphModel() {
        System.out.println("\n-------- Run test for get graph model for multi graphs TriG --------\n");
        try {
            handler.getGraphModel(multiGraphsDataset);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof MultiGraphsException);
        }
    }

    /*----------------------------------------------------------------------+
     |              Tests for listProjectNodes() function                   |
     +----------------------------------------------------------------------*/

    @Test public void getListNodesForNull() {
        System.out.println("\n-------- Run test for get list project graph nodes for null TriG --------\n");
        try {
            handler.listProjectNodes(null);
            fail("expected exception was not occured.");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test public void getListNodesForZeroGraph() {
        System.out.println("\n-------- Run test for get list project graph nodes for zero graph TriG --------\n");
        Iterator < Node > nodes = null;
        try {
            nodes = handler.listProjectNodes(zeroGraphDataset);
        } catch (Exception e) {} finally {
            assertTrue(!nodes.hasNext());
        }
    }

    @Test public void getListNodesForSingleGraph() {
        System.out.println("\n-------- Run test for get list project graph nodes for single graph TriG --------\n");
        Iterator < Node > nodes = null;
        try {
            nodes = handler.listProjectNodes(singleGraphDataset);
        } catch (Exception e) {} finally {
            assertTrue(nodes.hasNext());
        }
    }

    @Test public void getListNodesForMultiGraphs() {
        System.out.println("\n-------- Run test for get list project graph nodes for multi graphs TriG --------\n");
        Iterator < Node > nodes = null;
        try {
            nodes = handler.listProjectNodes(singleGraphDataset);
        } catch (Exception e) {} finally {
            assertTrue(nodes.hasNext());
        }
    }

    /*----------------------------------------------------------------------+
     |          Functions to read TriG from resources folder                |
     +----------------------------------------------------------------------*/

    private String readFile(String filename) {
        StringBuilder result = new StringBuilder("");
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private Dataset loadDataset(String filename) {
        //Get file from resources folder
        String trig = readFile(filename);
        StringReader reader = new StringReader(trig);
        Dataset dataset = DatasetFactory.create();
        dataset.begin(ReadWrite.WRITE);
        try {
            RDFDataMgr.read(dataset.asDatasetGraph(), reader, "", Lang.TRIG);
            dataset.commit();
        } finally {
            dataset.end();
        }
        return dataset;
    }
}
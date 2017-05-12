package org.waves_rsp.waves_configurator;

import static org.junit.Assert.assertTrue;

import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.Test;
import org.waves_rsp.waves_configurator.exceptions.MultiGraphsException;
import org.waves_rsp.waves_configurator.exceptions.ZeroGraphException;

public class TripleStoreAccessorTest {
	
	public TripleStoreAccessor tsAccessor = new TripleStoreAccessor();
	
	String zeroGraphTrig = "";
	
	String singleGraphTrig = 
		"# This document encodes one graph.\n" 								+
		"@prefix ex: <http://www.example.org/vocabulary#> .\n"  			+
		"@prefix : <http://www.example.org/exampleDocument#> .\n" 			+
		"\n" 																+
		":G1 { \n" 															+	 
		"     :Monica a ex:Person ;\n" 										+
		"     ex:name 'Monica Murphy' ;\n" 									+
		"     ex:homepage <http://www.monicamurphy.org> ;\n" 				+
		"     ex:email <mailto:monica@monicamurphy.org> ;\n" 				+
		"     ex:hasSkill ex:Management ,\n" 								+
		"     ex:Programming . \n" 											+	 
		"}\n";
	
	String multiGraphsTrig = 
		"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"	+
		"@prefix dc: <http://purl.org/dc/terms/> .\n"						+
		"@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"					+
		"\n"																+
		"# default graph\n"													+
		"   {\n"															+
		"     <http://example.org/bob> dc:publisher 'Bob' .\n"				+
		"     <http://example.org/alice> dc:publisher 'Alice' .\n"			+
		"   }\n"															+
		"\n"																+
		"<http://example.org/bob>\n"										+
		"   {\n"															+
		"      _:a foaf:name 'Bob' .\n"										+
		"     _:a foaf:mbox <mailto:bob@oldcorp.example.org> .\n"			+
		"     _:a foaf:knows _:b .\n"										+
		" }\n"																+
		"\n"																+
		"<http://example.org/alice>\n"										+
		"    {\n"															+
		"       _:b foaf:name 'Alice' .\n"									+
		"       _:b foaf:mbox <mailto:alice@work.example.org> .\n"			+
		"    }	\n";
	
	String invalidTrig = 
		"# This document encodes one graph.\n" 								+
		"@prefix ex: <http://www.example.org/vocabulary#> .\n"  			+
		"@prefix : <http://www.example.org/exampleDocument#> .\n" 			+
		"\n" 																+
		":G1 { \n" 															+	 
		"     :Monica a ex:Person ;\n"; 									
	
	/*----------------------------------------------------------------------+
	 |                  Test for convert() function                         |
	 +----------------------------------------------------------------------*/
	
	@Test public void convertSingleGraphTrig(){
		System.out.println("\n-------- Run test for convert one graph TriG string to Jena model --------\n");
		System.out.println("The trig string below is used for test:\n");
		System.out.print(singleGraphTrig + "\n");
		try {
			DatasetGraph graph = tsAccessor.convert(singleGraphTrig);
			assertTrue(graph.size() == 1);
		} catch (Exception e) { }
	}
	
	@Test public void convertZeroGraphTrig(){
		System.out.println("\n-------- Run test for convert zero graph TriG string to Jena model --------\n");
		System.out.println("The empty trig string is used for test:\n");
		try {
			tsAccessor.convert(zeroGraphTrig);
		} catch (Exception e) { assertTrue(e instanceof ZeroGraphException ); }
	}
	
	@Test public void convertMultiGraphsTrig(){
		System.out.println("\n-------- Run test for convert one graph TriG string to Jena model --------\n");
		System.out.println("The trig string below is used for test:\n");
		System.out.print(multiGraphsTrig + "\n");
		try {
			tsAccessor.convert(multiGraphsTrig);
		} catch (Exception e) { assertTrue(e instanceof MultiGraphsException ); }
	}
	
	@Test public void convertInvalidTrig(){
		System.out.println("\n-------- Run test for convert invalid TriG string to Jena model --------\n");
		System.out.println("The trig string below is used for test:\n");
		System.out.print(invalidTrig + "\n");
		try {
			tsAccessor.convert(invalidTrig);
		} catch (Exception e) { assertTrue(e instanceof JenaTransactionException ); }
	}

}

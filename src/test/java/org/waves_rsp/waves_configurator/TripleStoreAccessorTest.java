package org.waves_rsp.waves_configurator;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class TripleStoreAccessorTest {
	
	public TripleStoreAccessor tsAccessor = new TripleStoreAccessor();
	
	@Test
	public void convertTrigStringToJenaModelTest(){
		String trig = "# This document encodes one graph.\n" +
					  "@prefix ex: <http://www.example.org/vocabulary#> .\n"  +
					  "@prefix : <http://www.example.org/exampleDocument#> .\n" +
					  "\n" +
					  ":G1 { \n" + 
					  "     :Monica a ex:Person ;\n" +
					  "     ex:name 'Monica Murphy' ;\n" +
					  "     ex:homepage <http://www.monicamurphy.org> ;\n" +
					  "     ex:email <mailto:monica@monicamurphy.org> ;\n" +
					  "     ex:hasSkill ex:Management ,\n" +
					  "     ex:Programming . \n" + 
					  "}\n";
		System.out.println("-------- Run test for convert TriG configuration string to Jena model --------\n");
		System.out.println("The trig string below is used for test:\n");
		System.out.print(trig + "\n");
		Model model = this.tsAccessor.convertToModel(trig);
		System.out.println("Converted TriG Model size: " + model.size());
		assertTrue( model.size() != 0 );
	}

}

package org.waves_rsp.waves_configurator.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClusterAccessorTest {
	
	public ClusterAccessor clAccessor = new ClusterAccessor();
	
	@Test public void sshCmd() {
		System.out.println( clAccessor.sshCmd() );
		assertTrue( !clAccessor.sshCmd().isEmpty() );
	}
}

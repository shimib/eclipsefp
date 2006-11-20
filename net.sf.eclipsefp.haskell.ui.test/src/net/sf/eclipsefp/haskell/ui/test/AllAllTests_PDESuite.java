/*******************************************************************************
 * Copyright (c) 2005, 2006 Thiago Arrais and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thiago Arrais - Initial API and implementation
 *******************************************************************************/
package net.sf.eclipsefp.haskell.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllAllTests_PDESuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for net.sf.eclipsefp.haskell.ui");
		//$JUnit-BEGIN$
		suite.addTest(net.sf.eclipsefp.haskell.ui.test.util.preferences.AllTests_PDESuite.suite());
		suite.addTest(net.sf.eclipsefp.haskell.ui.test.editor.AllTests_PDESuite.suite());
		suite.addTest(net.sf.eclipsefp.haskell.ui.test.editor.codeassist.AllTests_PDESuite.suite());
		suite.addTest( net.sf.eclipsefp.haskell.ui.test.console.AllTests_PDESuite.suite());
		suite.addTest(net.sf.eclipsefp.haskell.ui.test.AllAllTests.suite());
		suite.addTest( net.sf.eclipsefp.haskell.ui.test.preferences.AllTests_PDESuite.suite());
		//$JUnit-END$
		return suite;
	}

}

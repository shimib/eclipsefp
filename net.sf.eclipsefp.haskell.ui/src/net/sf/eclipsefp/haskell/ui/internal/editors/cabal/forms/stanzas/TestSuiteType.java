/**
 * (c) 2011, Alejandro Serrano
 * Released under the condidtions of the EPL.
 */
package net.sf.eclipsefp.haskell.ui.internal.editors.cabal.forms.stanzas;

import net.sf.eclipsefp.haskell.core.cabalmodel.CabalSyntax;


public enum TestSuiteType {
  Stdio (CabalSyntax.VALUE_EXITCODE_STDIO_1_0.getCabalName(), "Executable (exitcode-stdio runner)"),
  Detailed (CabalSyntax.VALUE_DETAILED_0_9.getCabalName(), "Module (detailed runner)");

  String cabalName;
  String shownName;

  TestSuiteType(final String cabalName, final String shownName) {
    this.cabalName = cabalName;
    this.shownName = shownName;
  }

  public String getCabalName() {
    return cabalName;
  }

  public String getShownName() {
    return shownName;
  }
}

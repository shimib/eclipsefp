// Copyright (c) 2003-2005 by Leif Frenzel - see http://leiffrenzel.de
package net.sf.eclipsefp.haskell.core.preferences;

import net.sf.eclipsefp.haskell.core.HaskellCorePlugin;
import net.sf.eclipsefp.haskell.util.FileUtil;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;


/** <p>initializer for the core preferences (declared in the
  * <code>plugin.xml</code>).</p>
  *
  * @author Leif Frenzel
  */
public class CorePreferenceInitializer extends AbstractPreferenceInitializer
                                       implements ICorePreferenceNames {
  @Override
  public void initializeDefaultPreferences() {
    IEclipsePreferences coreNode = DefaultScope.INSTANCE.getNode( HaskellCorePlugin.getPluginId() );
    coreNode.put( SELECTED_COMPILER, "ghcCompiler" ); //$NON-NLS-1$
    coreNode.put( FOLDERS_SRC, FileUtil.DEFAULT_FOLDER_SRC );
   // coreNode.put( FOLDERS_DOC, FileUtil.DEFAULT_FOLDER_DOC );
   // coreNode.put( FOLDERS_OUT, "out" ); //$NON-NLS-1$
   // coreNode.put( TARGET_BINARY, "bin/theResult" ); //$NON-NLS-1$
    coreNode.putBoolean( FOLDERS_IN_NEW_PROJECT, true );

    coreNode.putBoolean( DEBUG_BREAK_ON_ERROR, false );
    coreNode.putBoolean( DEBUG_BREAK_ON_EXCEPTION, false );
    coreNode.putBoolean( DEBUG_PRINT_WITH_SHOW, true );
    coreNode.putInt( RUN_COMMAND_HISTORY_MAX, 20 );
  }
}
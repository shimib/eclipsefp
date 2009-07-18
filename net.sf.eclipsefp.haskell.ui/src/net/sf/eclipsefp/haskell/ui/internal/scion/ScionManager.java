package net.sf.eclipsefp.haskell.ui.internal.scion;

import java.util.HashMap;
import java.util.Map;
import net.sf.eclipsefp.haskell.core.project.HaskellNature;
import net.sf.eclipsefp.haskell.scion.client.ScionInstance;
import net.sf.eclipsefp.haskell.ui.HaskellUIPlugin;
import net.sf.eclipsefp.haskell.ui.internal.preferences.IPreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Manages instances of Scion servers.
 *
 * This class ensures that there is exactly one running Scion instance for each
 * open project. This instance can be accessed through
 * {@link #getScionInstance(IResource)}.
 *
 * This works by listening for resource changes.
 */
public class ScionManager implements IResourceChangeListener {

  private String serverExecutable = null;
  private final Map<IProject, ScionInstance> instances = new HashMap<IProject, ScionInstance>();

  public ScionManager() {
    // the work is done in the start() method
  }

  public void start() {
    IPreferenceStore preferenceStore = HaskellUIPlugin.getDefault().getPreferenceStore();
    serverExecutable = preferenceStore.getString( IPreferenceConstants.SCION_SERVER_EXECUTABLE );
    preferenceStore.addPropertyChangeListener( new IPropertyChangeListener() {
      public void propertyChange( final PropertyChangeEvent event ) {
        if (event.getProperty().equals( IPreferenceConstants.SCION_SERVER_EXECUTABLE )) {
          if (event.getNewValue() instanceof String && !((String)event.getNewValue()).equals( serverExecutable )) {
            serverExecutable = (String) event.getNewValue();
            serverExecutableChanged();
          }
        }
      }
    });

    try {
      ResourcesPlugin.getWorkspace().getRoot().accept( new IResourceVisitor() {

        public boolean visit( final IResource resource ) throws CoreException {
          return updateForResource( resource );
        }
      } );
    } catch( CoreException ex ) {
      HaskellUIPlugin.log(
          "Error when processing resource delta from ScionManager", ex );
    }

    ResourcesPlugin.getWorkspace().addResourceChangeListener( this,
        IResourceChangeEvent.POST_CHANGE );
  }

  public void stop() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );

    for( IProject project: instances.keySet() ) {
      stopInstance( instances.get(project) );
    }
    instances.clear();
  }

  /**
   * Returns the instance manager for the given resource. The resource must be
   * part of a currently opened project.
   */
  public ScionInstance getScionInstance( final IResource resource ) {
    IProject project = resource.getProject();
    if( instances.containsKey( project ) ) {
      return instances.get( project );
    }
    return null;
  }

  /**
   * Called after a resource in the workspace was changed. It finds all projects
   * that were opened/closed, and starts/stops Scion instances accordingly.
   */
  public void resourceChanged( final IResourceChangeEvent event ) {
    try {
      event.getDelta().accept( new IResourceDeltaVisitor() {

        public boolean visit( final IResourceDelta delta ) throws CoreException {
          return updateForResource( delta.getResource() );
        }
      } );
    } catch( CoreException ex ) {
      HaskellUIPlugin.log(
          "Error when processing resource delta from ScionManager", ex );
    }
  }

  /**
   * Called when the preference value for the server executable path has changed.
   * We restart all instances.
   */
  private void serverExecutableChanged() {
    for (IProject project : instances.keySet()) {
      stopInstance( instances.get(project) );
      ScionInstance instance = startInstance( project );
      instances.put(project, instance);
    }
  }

  private boolean updateForResource( final IResource resource ) throws CoreException {
    if( resource instanceof IProject ) {
      IProject project = ( IProject )resource;
      if( project.hasNature( HaskellNature.NATURE_ID ) ) {
        if( project.isOpen() && !instances.containsKey( project ) ) {
          ScionInstance instance = startInstance( project );
          instances.put( project, instance );
        }
        if( !project.isOpen() && instances.containsKey( project ) ) {
          stopInstance( instances.get(project) );
          instances.remove( project );
        }
        return false; // projects can't be children of other projects, can they?
      }
    }
    return true;
  }

  /**
   * Starts and returns a new Scion instance for the given project.
   * Does not add the instance to the instances map.
   */
  private ScionInstance startInstance( final IProject project ) {
    ScionInstance instance = new ScionInstance( serverExecutable );
    return instance;
  }

  /**
   * Stops the Scion instance for the given project.
   * Does not remove the instance fromthe instances map.
   */
  private void stopInstance( final ScionInstance instance ) {
    instance.stop();
  }

}

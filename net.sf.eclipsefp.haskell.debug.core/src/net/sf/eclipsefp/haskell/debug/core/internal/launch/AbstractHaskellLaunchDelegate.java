package net.sf.eclipsefp.haskell.debug.core.internal.launch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.eclipsefp.haskell.debug.core.internal.HaskellDebugCore;
import net.sf.eclipsefp.haskell.debug.core.internal.util.CoreTexts;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;


public abstract class AbstractHaskellLaunchDelegate implements ILaunchConfigurationDelegate{

  public void launch( final ILaunchConfiguration configuration,
      final String mode, final ILaunch launch, final IProgressMonitor monitor )
      throws CoreException {
    if( !monitor.isCanceled() ) {
      try {
        IPath loc = getExecutableLocation( configuration );
        checkCancellation( monitor );
        String[] arguments = determineArguments( configuration );
        checkCancellation( monitor );
        String[] cmdLine = createCmdLine( loc, arguments );
        checkCancellation( monitor );
        File workingDir = determineWorkingDir( configuration );
        checkCancellation( monitor );
        IProcess process = createProcess( configuration, launch, loc, cmdLine,
            workingDir );
        if( process != null ) {
          postProcessCreation( configuration, mode, launch, process );
        }
        /*
         * DebugPlugin.getDefault().getLaunchManager().addLaunchListener( new
         * ILaunchesListener() {
         *
         * public void launchesRemoved( final ILaunch[] launches ) { for
         * (ILaunch l:launches){ System.out.println("removed:" +l.toString()); }
         *
         * }
         *
         * public void launchesChanged( final ILaunch[] launches ) { for
         * (ILaunch l:launches){ System.out.println("changed:" +l.toString()); }
         *
         * }
         *
         * public void launchesAdded( final ILaunch[] launches ) { for (ILaunch
         * l:launches){ System.out.println("added:" +l.toString()); }
         *
         * } });
         */

        if( process != null && !isBackground( configuration ) ) {
          while( !process.isTerminated() ) {
            try {
              if( monitor.isCanceled() ) {
                process.terminate();
                break;
              }
              Thread.sleep( 50 );
            } catch( InterruptedException iex ) {
              // ignored
            }
          }
        }
      } catch( LaunchCancelledException lcex ) {
        // canceled on user request
      }
    }
  }

  protected abstract void postProcessCreation(final ILaunchConfiguration configuration,final String mode,final ILaunch launch,IProcess process) throws CoreException;

  private IProcess createProcess( final ILaunchConfiguration configuration,
      final ILaunch launch, final IPath location, final String[] cmdLine,
      final File workingDir ) throws CoreException {
    // Process proc = DebugPlugin.exec( cmdLine, workingDir );
    ProcessBuilder pb = new ProcessBuilder( cmdLine );
    pb.directory( workingDir );
    if( configuration.getAttribute( ILaunchAttributes.SYNC_STREAMS, true ) ) {
      pb.redirectErrorStream( true );
    }
    try {
      Process proc = pb.start();
      Map<String, String> processAttrs = new HashMap<String, String>();
      String programName = determineProgramName( location );
      processAttrs.put( IProcess.ATTR_PROCESS_TYPE, programName );
      IProcess process = null;
      if( proc != null ) {
        String loc = location.toOSString();
        process = DebugPlugin.newProcess( launch, proc, loc, processAttrs );
        process.setAttribute( IProcess.ATTR_CMDLINE, CommandLineUtil
            .renderCommandLine( cmdLine ) );
      }
      return process;
    } catch( IOException e ) {
      Status status = new Status( IStatus.ERROR,
          HaskellDebugCore.getPluginId(),
          CoreTexts.haskellLaunchDelegate_noProcess, e );
      throw new CoreException( status );
    }

  }

  private String[] createCmdLine( final IPath location, final String[] arguments ) {
    int cmdLineLength = 1;
    if( arguments != null ) {
      cmdLineLength += arguments.length;
    }
    String[] cmdLine = new String[ cmdLineLength ];
    cmdLine[ 0 ] = location.toOSString();
    if( arguments != null ) {
      System.arraycopy( arguments, 0, cmdLine, 1, arguments.length );
    }
    return cmdLine;
  }

  private File determineWorkingDir( final ILaunchConfiguration config )
      throws CoreException {
    String name = ILaunchAttributes.WORKING_DIRECTORY;
    String attribute = config.getAttribute( name, ( String )null );
    File result = null;
    if( attribute != null ) {
      result = new Path( attribute ).toFile();
    }
    return result;
  }

  private String[] determineArguments( final ILaunchConfiguration config )
      throws CoreException {
    String extra = config.getAttribute( ILaunchAttributes.EXTRA_ARGUMENTS,
        ILaunchAttributes.EMPTY );
    String args = config.getAttribute( ILaunchAttributes.ARGUMENTS,
        ILaunchAttributes.EMPTY );
    return CommandLineUtil.parse( extra + " " + args ); //$NON-NLS-1$
  }

  private void checkCancellation( final IProgressMonitor monitor ) {
    if( monitor.isCanceled() ) {
      throw new LaunchCancelledException();
    }
  }

  private String determineProgramName( final IPath location ) {
    String programName = location.lastSegment();
    String extension = location.getFileExtension();
    if( extension != null ) {
      int len = programName.length() - ( extension.length() + 1 );
      programName = programName.substring( 0, len );
    }
    return programName.toLowerCase();
  }


  // helping methods
  // ////////////////

  public IPath getExecutableLocation( final ILaunchConfiguration config )
      throws CoreException {
    String defaultValue = null;
    String location = config.getAttribute( ILaunchAttributes.EXECUTABLE,
        defaultValue );
    if( isEmpty( location ) ) {
      String msg = CoreTexts.haskellLaunchDelegate_noExe;
      String pluginId = HaskellDebugCore.getPluginId();
      IStatus status = new Status( IStatus.ERROR, pluginId, 0, msg, null );
      throw new CoreException( status );
    }

    return new Path( location );
  }

  private boolean isEmpty( final String location ) {
    return location == null || location.trim().length() == 0;
  }

  private boolean isBackground( final ILaunchConfiguration config )
      throws CoreException {
    return config.getAttribute( ILaunchAttributes.RUN_IN_BACKGROUND, true );
  }

  private class LaunchCancelledException extends RuntimeException {

    private static final long serialVersionUID = 1912643423745032866L;

    private LaunchCancelledException() {
      super();
    }
  }

  public static void runInConsole(final List<String> commands,final File directory,final String title) throws CoreException,IOException{
    ProcessBuilder pb=new ProcessBuilder( commands );
    pb.directory( directory );
    //pb.redirectErrorStream( true );
    final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    String configTypeId = ExecutableHaskellLaunchDelegate.class.getName();
    ILaunchConfigurationType configType  = launchManager.getLaunchConfigurationType( configTypeId );
    final ILaunchConfigurationWorkingCopy wc=configType.newInstance( null, launchManager.generateUniqueLaunchConfigurationNameFrom( title + System.currentTimeMillis()));

    wc.setAttribute( IDebugUIConstants.ATTR_PRIVATE, true );
    wc.setAttribute( IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true );

    final ILaunch launch = new Launch(wc,ILaunchManager.RUN_MODE,null);

    Process jp=pb.start();
    IProcess ep = DebugPlugin.newProcess(launch, jp,title);
    ep.setAttribute( IProcess.ATTR_CMDLINE,
        CommandLineUtil.renderCommandLine( commands.toArray( new String[commands.size()] )) );
    ep.setAttribute( IProcess.ATTR_PROCESS_TYPE, HaskellLaunchDelegate.class.getName() );
    launch.addProcess(ep);

    launchManager.addLaunch(launch);
//    launchManager.addLaunchListener( new ILaunchListener() {
//
//      public void launchRemoved( final ILaunch arg0 ) {
//        if (arg0==launch && arg0.isTerminated()){
//          launchManager.removeLaunchListener( this );
//          try {
//            wc.delete();
//          } catch (CoreException ce){
//            HaskellDebugCore.log( CoreTexts.launchconfiguration_delete_failed, ce );
//          }
//        }
//
//      }
//
//      public void launchChanged( final ILaunch arg0 ) {
//        // NOOP
//        if (arg0==launch && arg0.isTerminated()){
//          launchManager.removeLaunchListener( this );
//          try {
//            wc.delete();
//          } catch (CoreException ce){
//            HaskellDebugCore.log( CoreTexts.launchconfiguration_delete_failed, ce );
//          }
//        }
//      }
//
//      public void launchAdded( final ILaunch arg0 ) {
//        // NOOP
//      }
//    });

  }

}

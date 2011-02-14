package jenkins.plugins.clangscanbuild.commands;

import hudson.FilePath;
import hudson.Launcher.ProcStarter;

/**
 * This interface abstracts the operations which the xcodebuild commands needs in order to
 * execute.  This was done so that unit tests could mock this interface and test command
 * building independently of jenkins.
 * 
 * @author Josh Kenneyd
 */
public interface BuildContext {

	/**
	 * Returns workspace location of current executing build
	 */
	public FilePath getWorkspace();
	
	/**
	 * Returns a ProcStart which is pre-configured to log standard error and out
	 * to the build listener.  The present working directory is also pre-configured
	 * to point to the workspace of the currently executing build.
	 */
	public ProcStarter getProcStarter();
	
	/**
	 * This method starts a process and will not return control until
	 * either the process is complete or the process is interrupted.  
	 * Caught exceptions (IOException,InterrupredException) are logged 
	 * to the build listener and a return code of 1 will be returned. 
	 * Upon success, a return code of 0 is returned.
	 */
	public int waitForProcess( ProcStarter processStarter );
	
	/**
	 * Logs a message to the build listener.
	 */
	public void log( String message );
	
}

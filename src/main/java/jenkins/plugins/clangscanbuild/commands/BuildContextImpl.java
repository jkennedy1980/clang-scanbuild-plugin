package jenkins.plugins.clangscanbuild.commands;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class BuildContextImpl implements BuildContext{

	private AbstractBuild build;
	private Launcher launcher;
	private BuildListener listener;

	public BuildContextImpl( AbstractBuild build, Launcher launcher, BuildListener listener ){
		super();
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
	}

	public FilePath getWorkspace() {
		return build.getWorkspace();
	}

	public int waitForProcess( ProcStarter processStarter ){
		try {
			return processStarter.join();
		} catch (IOException e) {
			log( "Error starting process: " + processStarter + "\n" + e.getMessage() );
		} catch (InterruptedException e) {
			log( "Process was interrupted: " + processStarter + "\n" + e.getMessage() );
		}
		return 1;
	}

	public void log( String message ){
		listener.getLogger().println( message );
	}

	public ProcStarter getProcStarter() {
		ProcStarter procStarter = launcher.launch();
		procStarter.pwd( getWorkspace() ); // sets present working directory for command
		procStarter.stdout( listener ); // maps output from command to console output.  Some commands will need to override this because they need to capture the output.
		return procStarter;
	}

	@Override
	public FilePath getBuildFolder() {
		return new FilePath( build.getRootDir() );
	}

}

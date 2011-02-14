package jenkins.plugins.clangscanbuild;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;
import jenkins.plugins.clangscanbuild.commands.BuildContextImpl;
import jenkins.plugins.clangscanbuild.commands.ScanBuildCommand;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This builder provides a new build step for freestyle jobs.  Users can 
 * execute Clang scan-build against their XCode projects
 *
 * @author Joshua Kennedy
 */
public class ClangScanBuildBuilder extends Builder{
	
	@Extension
    public static final ClangScanBuildDescriptor DESCRIPTOR = new ClangScanBuildDescriptor();

	/**
	 * Each of the fields below corresponds to a field in the config.jelly file.
	 * The data for these fields is provided by the user on the build-step on the 
	 * job configuration screen.
	 */
	private String target;
    private String targetSdk;
    private String config;
    private String clangInstallationName;

    @DataBoundConstructor
    public ClangScanBuildBuilder( 
    		String target, 
    		String targetSdk, 
    		String config, 
    		String clangInstallationName ){
    	
        this.target = Util.fixEmptyAndTrim( target );
        this.targetSdk = Util.fixEmptyAndTrim( targetSdk );
        this.config = Util.fixEmptyAndTrim( config );     
        this.clangInstallationName = Util.fixEmptyAndTrim( clangInstallationName );
    }

    public String getClangInstallationName(){
    	return clangInstallationName;
    }
    
    public String getTarget() {
		return target;
	}

	public String getTargetSdk() {
		return targetSdk;
	}

	public String getConfig() {
		return config;
	}

	/**
	 * This method is invoked when a job is actually executed.  It is the magic method.
	 * @return boolean - if 'false', build will be aborted
	 */
	@Override
    public boolean perform( AbstractBuild build, Launcher launcher, BuildListener listener ) {
		
		ClangScanBuildToolInstallation clangInstallation = DESCRIPTOR.getNamedInstallation( getClangInstallationName() );
		if( clangInstallation == null ){
			// somehow config has gotten out of whack.  User has a named clang installation that no longer exists in
			// the master hudson config.  We need it to get the path to clang.
			listener.fatalError( "Unable to locate the clang installation named '" + getClangInstallationName() + "'.  Please confirm a clang installation named '" + getClangInstallationName() + "' is defined in the jenkins master config. " );
			return false;
		}
		

		ScanBuildCommand xcodebuild = new ScanBuildCommand();
		xcodebuild.setTarget( getTarget() );
		xcodebuild.setTargetSdk( getTargetSdk() );
		xcodebuild.setConfig( getConfig() );
		xcodebuild.setClangOutputFolderPath( "clang-output" );
		
		try {
			String path = clangInstallation.getExecutable( launcher ) ;
			if( path == null ){
				listener.fatalError( "Unable to locate 'scan-build' within '" + clangInstallation.getHome() + "' as configured in clang installation named '" + clangInstallation.getName() + "' in the master config." );
				return false;
			}
			xcodebuild.setClangScanBuildPath( path );
		} catch ( Exception e) {
			listener.fatalError( "Unable to locate 'scan-build' within '" + clangInstallation.getHome() + "' as configured in clang installation named '" + clangInstallation.getName() + "' in the master config." );
			return false;
		}
		
		int rc = CommandExecutor.execute( xcodebuild ).withContext( new BuildContextImpl( build, launcher, listener ) );

        return rc == CommandExecutor.SUCCESS;
    }

    public ClangScanBuildDescriptor getDescriptor() {
    	return DESCRIPTOR;
    }
    
}


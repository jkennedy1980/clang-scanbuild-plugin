package jenkins.plugins.clangscanbuild;

import java.io.IOException;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.plugins.clangscanbuild.commands.BuildContextImpl;
import jenkins.plugins.clangscanbuild.commands.ScanBuildCommand;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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
    private String xcodeProjectSubPath;
    private String workspace;
    private String scheme;
    private String scanbuildargs;

    @DataBoundConstructor
    public ClangScanBuildBuilder( 
    		String target, 
    		String targetSdk, 
    		String config, 
    		String clangInstallationName,
    		String xcodeProjectSubPath,
    		String workspace,
    		String scheme,
    		String scanbuildargs ){
    	
        this.target = Util.fixEmptyAndTrim( target );
        this.targetSdk = Util.fixEmptyAndTrim( targetSdk );
        this.config = Util.fixEmptyAndTrim( config );     
        this.clangInstallationName = Util.fixEmptyAndTrim( clangInstallationName );
        this.xcodeProjectSubPath = Util.fixEmptyAndTrim( xcodeProjectSubPath );
        this.workspace = Util.fixEmptyAndTrim( workspace );
        this.scheme = Util.fixEmptyAndTrim( scheme );
        this.scanbuildargs = Util.fixEmptyAndTrim( scanbuildargs );
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
	
	public String getWorkspace(){
		return workspace;
	}
	
	public String getScheme(){
		return scheme;
	}
	
	public String getScanbuildargs(){
		return scanbuildargs;
	}
	
	/**
	 * Removing slashes here in case the user adds a starting slash to the path.
	 */
	public String getXcodeProjectSubPath(){
		if( xcodeProjectSubPath == null ) return null;
		if( xcodeProjectSubPath.startsWith("/") || xcodeProjectSubPath.startsWith("\\") ){
			return xcodeProjectSubPath.substring(1);
		}
		return xcodeProjectSubPath;
	}
	
	/**
	 * This method is invoked when a job is actually executed.  It is the magic method.
	 * @return boolean - if 'false', build will be aborted
	 */
	@Override
    public boolean perform( @SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher, BuildListener listener ) {
		
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
		xcodebuild.setAdditionalScanBuildArguments( getScanbuildargs() );
		xcodebuild.setClangOutputFolder( new FilePath( build.getWorkspace(), ClangScanBuildUtils.REPORT_OUTPUT_FOLDERNAME) );
		xcodebuild.setWorkspace( getWorkspace() );
		xcodebuild.setScheme( getScheme() );
		
		if( getXcodeProjectSubPath() != null ){
			xcodebuild.setProjectDirectory( new FilePath( build.getWorkspace(), getXcodeProjectSubPath() ) );
		}else{
			xcodebuild.setProjectDirectory( build.getWorkspace() );
		}
		
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


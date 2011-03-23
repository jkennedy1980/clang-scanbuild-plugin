package jenkins.plugins.clangscanbuild.commands;

import hudson.FilePath;
import hudson.Launcher.ProcStarter;
import hudson.util.ArgumentListBuilder;
import jenkins.plugins.clangscanbuild.CommandExecutor;

public class ScanBuildCommand implements Command{
	
	private String target;
    private String targetSdk;
    private String config = "Debug";
    private String clangScanBuildPath;
    private FilePath clangOutputFolder;
    private FilePath projectDirectory;
	
	public int execute( BuildContext context ) throws Exception {

		if( clangOutputFolder.exists() ){
			// this should never happen because this folder is in the build directory - famous last words
			context.log( "Deleting " + getClangOutputFolder().getRemote() + " contents from previous build." );
			clangOutputFolder.deleteContents();
		}else{
			clangOutputFolder.mkdirs();
		}

		ArgumentListBuilder args = new ArgumentListBuilder();
		args.add( getClangScanBuildPath() );
		args.add( "-k" ); // keep going on failure
		args.add( "-v" ); // verbose
		args.add( "-v" ); // even more verbose
		args.add( "-o" ); // output folder
		args.add( "" + clangOutputFolder.getRemote().replaceAll( " ", "/ " ) + "" );
		
		args.add( "xcodebuild" );
		
		if( !isBlank( getTarget() ) ){
			args.add( "-target", getTarget() );
		}else{
			args.add( "-activetarget" );
		}
		
		if( !isBlank( getConfig() ) ){
			args.add( "-configuration", getConfig() );
		}else{
			args.add( "-activeconfiguration" );
		}
		
		args.add( "-sdk", getTargetSdk() );
		args.add( "clean" ); // clang scan requires a clean
		args.add( "build" );
		
		ProcStarter starter = context.getProcStarter();
		starter.cmds( args );
		starter.pwd( getProjectDirectory() );
		
		context.log( "COMMANDS:" + starter.cmds() );
		
		int rc = context.waitForProcess( starter );

		if( rc == CommandExecutor.SUCCESS ){
			context.log( "XCODEBUILD SUCCESS" );
		}else{
			context.log( "XCODEBUILD ERROR" );
		}
		
		return rc;

	}

	private boolean isBlank( String value ){
		if( value == null ) return true;
		return value.trim().length() <= 0;
	}
	
	public String getTargetSdk() {
		return targetSdk;
	}

	public void setTargetSdk(String targetSdk) {
		this.targetSdk = targetSdk;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getClangScanBuildPath() {
		return clangScanBuildPath;
	}

	public void setClangScanBuildPath(String clangScanBuildPath) {
		this.clangScanBuildPath = clangScanBuildPath;
	}

	public FilePath getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(FilePath projectDirectory) {
		this.projectDirectory = projectDirectory;
	}

	public FilePath getClangOutputFolder() {
		return clangOutputFolder;
	}

	public void setClangOutputFolder(FilePath clangOutputFolder) {
		this.clangOutputFolder = clangOutputFolder;
	}
	

}

package jenkins.plugins.clangscanbuild.publisher;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.actions.ClangScanBuildProjectAction;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBug;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

import org.kohsuke.stapler.DataBoundConstructor;

public class ClangScanBuildPublisher extends Recorder{
	
	private static final Logger LOGGER = Logger.getLogger( ClangScanBuildPublisher.class.getName() );
	
	@Extension
	public static final ClangScanBuildPublisherDescriptor DESCRIPTOR = new ClangScanBuildPublisherDescriptor();

	private static final Pattern BUG_TYPE_PATTERN = Pattern.compile( "<!--\\sBUGTYPE\\s(.*)\\s-->" );
	private static final Pattern BUG_DESC_PATTERN = Pattern.compile( "<!--\\sBUGDESC\\s(.*)\\s-->" );
	private static final Pattern BUGFILE_PATTERN = Pattern.compile( "<!--\\sBUGFILE\\s(.*)\\s-->" );
	private static final Pattern BUGCATEGORY_PATTERN = Pattern.compile( "<!--\\sBUGCATEGORY\\s(.*)\\s-->" );

	private String scanBuildOutputFolder;
	private int bugThreshold;
	private boolean failWhenThresholdExceeded;
	
	@DataBoundConstructor
	public ClangScanBuildPublisher( 
			String scanBuildOutputFolder,
			int bugThreshold,
			boolean failWhenThresholdExceeded){
		
		super();
		System.err.println("STARTING RECORDER");
		this.scanBuildOutputFolder = scanBuildOutputFolder;
		this.bugThreshold = bugThreshold;
		this.failWhenThresholdExceeded = failWhenThresholdExceeded;
	}
	
	public String getScanBuildOutputFolder() {
		return scanBuildOutputFolder;
	}

	public void setScanBuildOutputFolder(String scanBuildOutputFolder) {
		this.scanBuildOutputFolder = scanBuildOutputFolder;
	}

	public int getBugThreshold() {
		return bugThreshold;
	}

	public void setBugThreshold(int bugThreshold) {
		this.bugThreshold = bugThreshold;
	}

	public boolean isFailWhenThresholdExceeded() {
		return failWhenThresholdExceeded;
	}

	public void setFailWhenThresholdExceeded(boolean failWhenThresholdExceeded) {
		this.failWhenThresholdExceeded = failWhenThresholdExceeded;
	}
	
	@Override
	public Action getProjectAction( AbstractProject<?, ?> project ){
		return new ClangScanBuildProjectAction( project );
	}

	@Override
	public ClangScanBuildPublisherDescriptor getDescriptor() {
		return DESCRIPTOR;
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
		// TODO: annotate the start of the publish phase
		listener.getLogger().println( "STARTING CLANG PUBLISHER" );
		
		FilePath workspaceOutputFolder = ensureWorkspaceOutputFolder( build );
			
		FilePath buildOutputFolder = new FilePath( new FilePath( build.getArtifactsDir() ), getScanBuildOutputFolder() );
		
		copyClangReportsToBuildArtifacts( workspaceOutputFolder, buildOutputFolder, listener );

		FilePath[] clangReports = locateClangBugReports( workspaceOutputFolder );

		ClangScanBuildBugSummary previousBugSummary = getBugSummaryForLastBuild( build );
		ClangScanBuildBugSummary bugSummary = new ClangScanBuildBugSummary( build.number );
		
		for( FilePath report : clangReports ){
			ClangScanBuildBug bug = createBugFromClangScanBuildHtml( build.getProject().getName(), report, previousBugSummary );
			bugSummary.add( bug );
		}

		final ClangScanBuildAction action = new ClangScanBuildAction( build, bugSummary );
        build.getActions().add( action );

        if( action.getResult().isFailed() ){
        	listener.getLogger().println( "REPORT INDICATED A FAILURE" );
            build.setResult( Result.UNSTABLE );
        }
		
		//evaluateBuildStatus( build, listener, reports );
		
		return true;
	}
	
	private ClangScanBuildBug createBugFromClangScanBuildHtml( String projectName, FilePath report, ClangScanBuildBugSummary previousBugSummary ){
		ClangScanBuildBug bug = createBugInstance( projectName, report );
		if( previousBugSummary != null ){
			// This marks bugs as new if they did not exist in the last build report
			bug.setNewBug( !previousBugSummary.contains( bug ) );
		}
		return bug;
	}

	private FilePath ensureWorkspaceOutputFolder(AbstractBuild<?, ?> build)
			throws IOException, InterruptedException {
		FilePath workspaceOutputFolder = new FilePath( build.getWorkspace(), getScanBuildOutputFolder() );
		if( !workspaceOutputFolder.exists() ) workspaceOutputFolder.mkdirs();
		return workspaceOutputFolder;
	}

	private ClangScanBuildBugSummary getBugSummaryForLastBuild(
			AbstractBuild<?, ?> build) {
		ClangScanBuildBugSummary previousBugSummary = null;
		if( build.getPreviousBuild() != null ){
			ClangScanBuildAction previousAction = build.getPreviousBuild().getAction( ClangScanBuildAction.class );
			if( previousAction != null ){
				previousBugSummary = previousAction.getBugSummary();
			}
		}
		return previousBugSummary;
	}

	/**
	 * Clang always creates a subfolder within the specified output folder that has a unique name.
	 * This method locates the first subfolder of the output folder and copies its contents
	 * to the build archive folder.
	 */
	private void copyClangReportsToBuildArtifacts( FilePath workspaceOutputFolder, FilePath buildOutputFolder, BuildListener listener ){
		try{
			List<FilePath> subFolders = workspaceOutputFolder.listDirectories();
			if( subFolders.isEmpty() ){
				listener.getLogger().println( "Could not locate a unique scan-build output folder in: " + workspaceOutputFolder );
				return;
			}
	
			FilePath clangDateFolder = workspaceOutputFolder.listDirectories().get( 0 );
			clangDateFolder.copyRecursiveTo( buildOutputFolder );
		}catch( Exception e ){
			listener.fatalError( "Unable to copy Clan scan-build output to build archive folder." );
		}
	}

	private ClangScanBuildBug createBugInstance( String projectName, FilePath report ){
		ClangScanBuildBug instance = new ClangScanBuildBug();
		instance.setReportFile( report.getName() );
		
		String contents = null;
		try {
			contents = report.readToString();
			instance.setBugDescription( getMatch( BUG_DESC_PATTERN, contents ) );
			instance.setBugType( getMatch( BUG_TYPE_PATTERN, contents ) );
			instance.setBugCategory( getMatch( BUGCATEGORY_PATTERN, contents ) );
			String sourceFile = getMatch( BUGFILE_PATTERN, contents );
			// TODO: slashes make this not cross platform...fix it
			String search = "/jobs/" + projectName + "/workspace/";

			int position = sourceFile.lastIndexOf( search );
			if( position > 0 ){
				sourceFile = sourceFile.substring( position + search.length() );
			}
			
			instance.setSourceFile( sourceFile );
		}catch( IOException e ){
			LOGGER.log( Level.INFO, "Unable to read file or locate clang markers in content: " + report );
		}

		return instance;
	}

	private void evaluateBuildStatus( AbstractBuild<?, ?> build, BuildListener listener, FilePath[] reports ){
		if( isFailWhenThresholdExceeded() ){
			if( reports.length > getBugThreshold() ){
				listener.getLogger().println( "Marking build as failed due to the clang bug threshold being exceeded by " + ( reports.length - getBugThreshold() ) );
				build.setResult( Result.FAILURE );
			}
		}else{
			if( reports.length > getBugThreshold() ){
				listener.getLogger().println( "Marking build as unstable due to the clang bug threshold being exceeded by " + ( reports.length - getBugThreshold() ) );
				build.setResult( Result.UNSTABLE );
			}
		}
	}
	
	private String getMatch( Pattern pattern, String contents ){
		Matcher matcher = pattern.matcher( contents );
		while( matcher.find() ){
			return matcher.group(1);
		}
		return null;
	}
	
	protected static FilePath[] locateClangBugReports( FilePath clangOutputFolder ) throws IOException, InterruptedException {
        List<FilePath> files = new ArrayList<FilePath>();
        files.addAll( Arrays.asList( clangOutputFolder.list( "**/report-*.html" ) ) );
        return files.toArray( new FilePath[ files.size() ] );
	}
	
}

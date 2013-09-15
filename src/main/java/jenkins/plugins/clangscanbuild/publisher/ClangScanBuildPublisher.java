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

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.actions.ClangScanBuildProjectAction;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBug;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

public class ClangScanBuildPublisher extends Recorder{
	
	private static final Logger LOGGER = Logger.getLogger( ClangScanBuildPublisher.class.getName() );
	
	@Extension
	public static final ClangScanBuildPublisherDescriptor DESCRIPTOR = new ClangScanBuildPublisherDescriptor();

	private static final Pattern BUG_TYPE_PATTERN = Pattern.compile( "<!--\\sBUGTYPE\\s(.*)\\s-->" );
	private static final Pattern BUG_DESC_PATTERN = Pattern.compile( "<!--\\sBUGDESC\\s(.*)\\s-->" );
	private static final Pattern BUGFILE_PATTERN = Pattern.compile( "<!--\\sBUGFILE\\s(.*)\\s-->" );
	private static final Pattern BUGCATEGORY_PATTERN = Pattern.compile( "<!--\\sBUGCATEGORY\\s(.*)\\s-->" );

	private int unstableBugThreshold;
	private boolean markBuildUnstableWhenThresholdIsExceeded;
	private int failBugThreshold;
	private boolean markBuildFailedWhenThresholdIsExceeded;

	public ClangScanBuildPublisher( 
			boolean markBuildUnstableWhenThresholdIsExceeded, 
			int unstableBugThreshold,
			boolean markBuildFailedWhenThresholdIsExceeded,
			int failBugThreshold
			){
		
		super();
		this.markBuildUnstableWhenThresholdIsExceeded = markBuildUnstableWhenThresholdIsExceeded;
		this.unstableBugThreshold = unstableBugThreshold;
		this.markBuildFailedWhenThresholdIsExceeded = markBuildFailedWhenThresholdIsExceeded;
		this.failBugThreshold = failBugThreshold;
	}

	public int getUnstableBugThreshold() {
		return unstableBugThreshold;
	}
	
	public boolean isMarkBuildUnstableWhenThresholdIsExceeded(){
		return markBuildUnstableWhenThresholdIsExceeded;
	}

	public void setUnstableBugThreshold(int unstableBugThreshold) {
		this.unstableBugThreshold = unstableBugThreshold;
	}

	public int getFailBugThreshold() {
		return failBugThreshold;
	}

	public boolean isMarkBuildFailedWhenThresholdIsExceeded(){
		return markBuildFailedWhenThresholdIsExceeded;
	}

	public void setFailedBugThreshold(int failBugThreshold) {
		this.failBugThreshold = failBugThreshold;
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

		listener.getLogger().println( "Publishing Clang scan-build results" );
		
		FilePath reportOutputFolder = new FilePath(build.getWorkspace(), ClangScanBuildUtils.REPORT_OUTPUT_FOLDERNAME); 
		FilePath reportMasterOutputFolder = ClangScanBuildUtils.locateClangScanBuildReportFolder(build);
		
		// This copies the reports out of the generate date sub folder to the root of the reports folder and then deletes the clang generated folder
		copyClangReportsOutOfGeneratedSubFolder( reportOutputFolder, listener );
		
		// This copies the report dir to master
		copyClangReportsToMaster( reportOutputFolder, reportMasterOutputFolder, listener );
		
		// this digs into the clang results looking for the subfolder created by clang
		List<FilePath> clangReports = locateClangBugReports( reportOutputFolder );
		
		// this loads the previous bug summary for the last build.  it is need to identify bugs added since last build
		ClangScanBuildBugSummary previousBugSummary = getBugSummaryForLastBuild( build );

		// this builds and new bug summary and populates it with bugs
		ClangScanBuildBugSummary newBugSummary = new ClangScanBuildBugSummary( build.number );
		for( FilePath report : clangReports ){
			// bugs are parsed inside this method:
			ClangScanBuildBug bug = createBugFromClangScanBuildHtml( build.getProject().getName(), report, previousBugSummary, build.getWorkspace().getRemote() );
			newBugSummary.add( bug );
		}
		
		// this line dumps a bugSummary.xml file to the build artifacts.  did this instead of using job config xml for performance
		//FilePath bugSummaryXMLFile = new FilePath( reportOutputFolder, "bugSummary.xml" );
		FilePath bugSummaryXMLFile = new FilePath( new FilePath( build.getRootDir() ), "bugSummary.xml" );
		String bugSummaryXML = AbstractBuild.XSTREAM.toXML( newBugSummary );
		bugSummaryXMLFile.write( bugSummaryXML, "UTF-8" );
		
		// this adds a build actions which records the bug count into the build results.  This count is used to generate the trend charts
		final ClangScanBuildAction action = new ClangScanBuildAction( build, newBugSummary.getBugCount(), markBuildUnstableWhenThresholdIsExceeded, unstableBugThreshold, bugSummaryXMLFile );
        build.getActions().add( action );

        // This checks if the build should be marked failed or unstable due to exceeding the bug thresholds.
        // Marking the build Failed takes priority over marking it unstable.
        if( action.buildFailedDueToExceededThreshold() ){
            listener.getLogger().println( "Clang scan-build fail threshhold exceeded." );
            build.setResult( Result.FAILURE );
        }
        else if( action.buildUnstableDueToExceededThreshold() ){
            listener.getLogger().println( "Clang scan-build unstable threshhold exceeded." );
            build.setResult( Result.UNSTABLE );
        }

		return true;
	}
	
	private ClangScanBuildBug createBugFromClangScanBuildHtml( String projectName, FilePath report, ClangScanBuildBugSummary previousBugSummary, String workspacePath ){
		ClangScanBuildBug bug = createBugInstance( projectName, report, workspacePath );

		// this checks to see if the bug is new since the last build
		if( previousBugSummary != null ){
			// This marks bugs as new if they did not exist in the last build report
			bug.setNewBug( !previousBugSummary.contains( bug ) );
		}
		return bug;
	}

	private ClangScanBuildBugSummary getBugSummaryForLastBuild( AbstractBuild<?, ?> build) {
		if( build.getPreviousBuild() != null ){
			ClangScanBuildAction previousAction = build.getPreviousBuild().getAction( ClangScanBuildAction.class );
			if( previousAction != null ){
				return previousAction.loadBugSummary();
			}
		}
		return null;
	}

	/**
	 * Clang always creates a subfolder within the specified output folder that has a unique name.
	 * This method locates the first subfolder of the output folder and copies its contents
	 * to the build archive folder.
	 */
	private void copyClangReportsOutOfGeneratedSubFolder( FilePath reportsFolder, BuildListener listener ){
		try{
			List<FilePath> subFolders = reportsFolder.listDirectories();
			if( subFolders.isEmpty() ){
				listener.getLogger().println( "Could not locate a unique scan-build output folder in: " + reportsFolder );
				return;
			}
	
			FilePath clangDateFolder = subFolders.get( 0 );
			clangDateFolder.copyRecursiveTo( reportsFolder );
			clangDateFolder.deleteRecursive();
		}catch( Exception e ){
			listener.fatalError( "Unable to copy Clan scan-build output to build archive folder." );
		}
	}
	/**
	 * Copy clang output folder to have access to html files from the master
	 */
	private void copyClangReportsToMaster( FilePath reportsFolder, FilePath materPath, BuildListener listener ){
		try{
			reportsFolder.copyRecursiveTo( materPath );
		}catch( Exception e ){
			listener.fatalError( "Unable to copy Clang scan-build output to master." );
		}
	}

	/**
	 * This method creates a bug instance from scan-build HMTL report.  It does this by using reg-ex searches
	 * to located HTML comments in the file which are easily parseable and appear in every HTML bug report from
	 * scan-build.  If scan-build ever adds an XML option, this functionality can be replaced with an XML parsing
	 * routine.
	 */
	private ClangScanBuildBug createBugInstance( String projectName, FilePath report, String workspacePath ){
		// the report parameter is the file the points to an HTML report generated by clang
		ClangScanBuildBug instance = new ClangScanBuildBug();
		instance.setReportFile( report.getName() );
		
		String contents = null;
		try {
			// this code digs into the HTML report content to locate the bug markers using regex
			contents = report.readToString();
			instance.setBugDescription( getMatch( BUG_DESC_PATTERN, contents ) );
			instance.setBugType( getMatch( BUG_TYPE_PATTERN, contents ) );
			instance.setBugCategory( getMatch( BUGCATEGORY_PATTERN, contents ) );
			String sourceFile = getMatch( BUGFILE_PATTERN, contents );

			// This attempts to shorten the file path by removing the workspace path and
			// leaving only the path relative to the workspace.
			int position = sourceFile.lastIndexOf( workspacePath );
			if( position >= 0 ){
				sourceFile = sourceFile.substring( position + workspacePath.length() );
			}
			
			instance.setSourceFile( sourceFile );
		}catch( IOException e ){
			LOGGER.log( Level.ALL, "Unable to read file or locate clang markers in content: " + report );
		}

		return instance;
	}

	private String getMatch( Pattern pattern, String contents ){
		Matcher matcher = pattern.matcher( contents );
		while( matcher.find() ){
			return matcher.group(1);
		}
		return null;
	}
	
	/**
	 * This locates all the generated HTML bug reports from scan-build and returns them as a list.
	 */
	protected List<FilePath> locateClangBugReports( FilePath clangOutputFolder ) throws IOException, InterruptedException {
		List<FilePath> files = new ArrayList<FilePath>();
		if( !clangOutputFolder.exists() ) return files;
        files.addAll( Arrays.asList( clangOutputFolder.list( "**/report-*.html" ) ) );
        return files;
	}
	
}

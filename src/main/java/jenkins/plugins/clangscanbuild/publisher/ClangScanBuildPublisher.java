package jenkins.plugins.clangscanbuild.publisher;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

public class ClangScanBuildPublisher extends Recorder{
	
	@Extension
	public static final ClangScanBuildPublisherDescriptor DESCRIPTOR = new ClangScanBuildPublisherDescriptor();

	private static final Pattern BUG_TYPE_PATTERN = Pattern.compile( "<!--\\sBUGTYPE\\s(.*)\\s-->" );
	private static final Pattern BUG_DESC_PATTERN = Pattern.compile( "<!--\\sBUGDESC\\s(.*)\\s-->" );
		
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
	public ClangScanBuildPublisherDescriptor getDescriptor() {
		return DESCRIPTOR;
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
		listener.getLogger().println( "STARTING CLANG PUBLISHER" );
		
		FilePath clangOutputFolder = new FilePath( build.getWorkspace(), getScanBuildOutputFolder() );
		FilePath[] reports = locateBugReports( clangOutputFolder );
		
		for( FilePath report : reports ){
			String contents = report.readToString();
			
			String bugType = getMatch( BUG_TYPE_PATTERN, contents );
			String bugDesc = getMatch( BUG_DESC_PATTERN, contents );
			
			listener.getLogger().println( "Found clang bug -> type: " + bugType + "; description: " + bugDesc );
		}

		evaluateBuildStatus( build, listener, reports );
		
		return true;
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
	
	protected static FilePath[] locateBugReports( FilePath clangOutputFolder ) throws IOException, InterruptedException {
        List<FilePath> files = new ArrayList<FilePath>();
        files.addAll( Arrays.asList( clangOutputFolder.list( "**/report-*.html" ) ) );
        return files.toArray( new FilePath[ files.size() ] );
	}
	
}

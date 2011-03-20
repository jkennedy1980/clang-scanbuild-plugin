package jenkins.plugins.clangscanbuild.actions;

import java.io.IOException;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.model.AbstractBuild;
import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

import org.kohsuke.stapler.StaplerProxy;

/**
 * This contributes the menu to the left used to access reports/whatever from inside a 
 * particular job's results.  This is called a "BuildAction" because it contributes
 * a link to the left and a URL to a specific build.
 * 
 * @author Josh Kennedy
 */
public class ClangScanBuildAction implements Action, StaplerProxy, ModelObject{

	private int bugThreshold;
	private String reportsUrl;
	private FilePath bugSummaryXML;
	private boolean markBuildUnstable;
	private int bugCount;
	
	public ClangScanBuildAction( AbstractBuild<?,?> build, int bugCount, boolean markBuildUnstable, int bugThreshold, String artifactsSubFolderName, FilePath bugSummaryXML ){
		this.bugThreshold = bugThreshold;
		this.bugCount = bugCount;
		this.bugSummaryXML = bugSummaryXML;
		this.markBuildUnstable = markBuildUnstable;
		this.reportsUrl = "/" + build.getUrl() + "artifact/" + artifactsSubFolderName;
	}
	
	public boolean buildFailedDueToExceededThreshold(){
		if( !markBuildUnstable ) return false;
		return getBugCount() > bugThreshold;
	}
	
	public int getBugThreshhold(){
		return bugThreshold;
	}
	
	/**
	 * The only thing stored in the actual builds in the bugCount and bugThreshold.  This was done in order to make the
	 * build XML smaller to reduce load times.  The counts are need in order to render the trend charts.
	 * 
	 * This method actually loads the XML file that was generated at build time and placed alongside the clang output files
	 * This XML contains the list of bugs and is used to render the report which links to the clang files.
	 * 
	 * DON'T CALL THIS UNLESS YOU NEED THE ACTUAL BUG SUMMARY
	 */
	public ClangScanBuildBugSummary loadBugSummary(){
		if( bugSummaryXML == null ) return null;
		
		try{
			return (ClangScanBuildBugSummary) AbstractBuild.XSTREAM.fromXML( bugSummaryXML.read() );
		}catch( IOException ioe ){
			System.err.println( ioe );
			return null;
		}
	}
	
	public int getBugCount(){
		return bugCount;
	}
	
	public String getReportsUrl(){
		return this.reportsUrl;
	}
	
	/**
	 * Indicates which icon should be displayed next to the link
	 */
	@Override
	public String getIconFileName() {
		return ClangScanBuildUtils.getIconsPath() + "scanbuild-32x32.png";
	}

	/**
	 * Title of link display on job results screen.
	 */
	@Override
	public String getDisplayName() {
		return "Clang scan-build bugs";
	}

	/**
	 * This object will be reference if a request comes into the following url:
	 * http://[jenkins]/job/[job name]/[job number]/clangBugReport
	 */
	@Override
	public String getUrlName() {
		return "clangScanBuildBugs";
	}

	/**
	 * This method needs to return the object that is responsible
	 * for handling web requests.  This file defines a new url
	 * strategy to use which can provide custom urls for this plugin
	 * 
	 */
	@Override
	public Object getTarget(){
		return this;
	}
    
}

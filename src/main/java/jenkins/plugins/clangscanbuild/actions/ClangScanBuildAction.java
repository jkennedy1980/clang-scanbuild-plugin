package jenkins.plugins.clangscanbuild.actions;

import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.AbstractBuild;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;
import jenkins.plugins.clangscanbuild.reports.ClangScanBuildReport;

import org.kohsuke.stapler.StaplerProxy;

/**
 * This contributes the menu to the left used to access reports/whatever from inside a 
 * particular job's results.  This is called a "BuildAction" because it contributes
 * a link to the left and a URL to a specific build.
 * 
 * @author Josh Kennedy
 */
public class ClangScanBuildAction implements HealthReportingAction, StaplerProxy{

	//private static final Logger logger = Logger.getLogger( ClangScanBuildAction.class.getName() );
	public final AbstractBuild<?,?> build;
	
	public ClangScanBuildReport report;
	public ClangScanBuildBugSummary bugSummary;
	
	public ClangScanBuildAction( AbstractBuild<?,?> build, ClangScanBuildBugSummary bugSummary ){
		this.build = build;
		this.bugSummary = bugSummary;
	}
	
	public ClangScanBuildBugSummary getBugSummary(){
		return bugSummary;
	}
	
	public int getBugCount(){
		return bugSummary.getBugCount();
	}
	
	/**
	 * Indicates which icon should be displayed next to the link
	 */
	@Override
	public String getIconFileName() {
		return "graph.gif";
	}

	/**
	 * Title of link display on job results screen.
	 */
	@Override
	public String getDisplayName() {
		return "Clang Bug Report";
	}

	/**
	 * This object will be reference if a request comes into the following url:
	 * http://[jenkins]/job/[job name]/[job number]/clangBugReport
	 */
	@Override
	public String getUrlName() {
		return "clangBugBuildReport";
	}

	/**
	 * This method needs to return the object that is responsible
	 * for handling web requests.  This file defines a new url
	 * strategy to use which can provide custom urls for this plugin
	 * 
	 */
	@Override
	public Object getTarget(){
		return getResult();
	}
	
	public synchronized ClangScanBuildReport getResult() {
        ClangScanBuildAction action = build.getAction( ClangScanBuildAction.class );
		
		report = new ClangScanBuildReport( action.getBugSummary(), build );
        return report;
    }

	/**
	 * Called after build to determine build score.  Lowest build score wins.
	 */
	@Override
	public HealthReport getBuildHealth(){
		System.err.println( "INSIDE CLANG SCAN BUILD ACTION - getBuildHealth" );
		HealthReport report = new HealthReport();
		report.setDescription( "FAILED DUE TO CLANG ERRORS" );
		report.setScore( 20 );
		return report;
	}

}

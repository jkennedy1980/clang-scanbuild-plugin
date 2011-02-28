package jenkins.plugins.clangscanbuild.actions;

import hudson.model.Action;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;
import jenkins.plugins.clangscanbuild.reports.ClangBuildGraph;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This contributes the menu to the left used to access reports/whatever from inside a 
 * project.  This is called a "ProjectAction" because it contributes
 * a link to the left and a URL to a specific projects dashboard.
 * 
 * @author Josh Kennedy
 */
public class ClangScanBuildProjectAction implements Action{
	
	private static final Logger logger = Logger.getLogger( ClangScanBuildProjectAction.class.getName() );
	
	public final AbstractProject<?,?> project;
	
	public ClangScanBuildProjectAction( AbstractProject project ) {
		super();
		this.project = project;
		logger.log( Level.ALL, "OHMAN" );
	}
	  
	@Override
	public String getIconFileName() {
		return "graph.gif";
	}

	@Override
	public String getDisplayName() {
		return "Clang Scan-Build Bug Trend";
	}

	@Override
	public String getUrlName() {
		return "clangBugSummary";
	}
	
    public void doSummary( StaplerRequest req, StaplerResponse rsp ) throws IOException {
    	//rsp.serveFile( request, res );
    }

    public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {

        if( ChartUtil.awtProblemCause != null ){
            // not available. send out error message
            rsp.sendRedirect2( req.getContextPath() + "/images/headless.png" );
            return;
        }
    	
        AbstractBuild<?,?> lastBuild = getLastNonErrorBuild();
    	if( lastBuild == null ) return;
    	
		//TODO: factor out to reusable class -- perhaps in buildAction
		List<ClangScanBuildBugSummary> bugSummaries = new ArrayList<ClangScanBuildBugSummary>();
        for( AbstractBuild<?,?> b = lastBuild; b != null; b = b.getPreviousBuild() ){
            if( b.getResult() == Result.FAILURE ) continue;
            
            ClangScanBuildAction action = b.getAction( ClangScanBuildAction.class );
            if( action != null ){
            	ClangScanBuildBugSummary summary = action.getBugSummary();
            	if( summary != null ){
            		bugSummaries.add( summary );
            	}
            }
        }
       
    	ClangBuildGraph g = new ClangBuildGraph( Calendar.getInstance(), bugSummaries );
    	g.doPng( req, rsp );
    }
    
    public AbstractBuild<?,?> getLastNonErrorBuild() {
        for( AbstractBuild<?,?> build = project.getLastBuild(); build != null; build = build.getPreviousBuild() ){
            if( build.getResult() == Result.FAILURE ) continue;
            return build;
        }
        return null;
    }
    
}

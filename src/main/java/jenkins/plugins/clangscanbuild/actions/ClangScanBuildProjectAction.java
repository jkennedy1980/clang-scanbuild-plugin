package jenkins.plugins.clangscanbuild.actions;

import hudson.model.Action;
import hudson.model.AbstractProject;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.List;

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildHistoryGatherer;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildHistoryGathererImpl;
import jenkins.plugins.clangscanbuild.reports.ClangBuildGraph;
import jenkins.plugins.clangscanbuild.reports.GraphPoint;

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

	private static final String DEFAULT_IMAGE = "/images/headless.png";
	public final AbstractProject<?,?> project;
	private ClangScanBuildHistoryGatherer gatherer = new ClangScanBuildHistoryGathererImpl();
	
	public ClangScanBuildProjectAction( AbstractProject<?,?> project ) {
		super();
		this.project = project;
	}
	  
	@Override
	public String getIconFileName() {
		return ClangScanBuildUtils.getIconsPath() + "scanbuild-32x32.png";
	}
	
	/**
	 * Doing this wastefully because i do not know the lifecycle of this object.  Is it a singleton?
	 */
	public ClangBuildGraph getGraph(){
		return new ClangBuildGraph( gatherer.gatherHistoryDataSet( project.getLastBuild() ) );
	}

	@Override
	public String getDisplayName() {
		return "Clang scan-build trend";
	}

	@Override
	public String getUrlName() {
		return "clangScanBuildTrend";
	}

    public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
    	System.err.println("DOING PNG");

        if( ChartUtil.awtProblemCause != null ){
            rsp.sendRedirect2( req.getContextPath() + DEFAULT_IMAGE );
            return;
        }

    	getGraph().doPng( req, rsp );
    }
    
    public void doMap( StaplerRequest req, StaplerResponse rsp ) throws IOException {
    	System.err.println("DOING MAP");

    	getGraph().doMap( req, rsp );
    }
    
    public boolean buildDataExists(){
    	List<GraphPoint> points = gatherer.gatherHistoryDataSet( project.getLastBuild() );
    	return points.size() > 0;
    }
    
}

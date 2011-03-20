package jenkins.plugins.clangscanbuild.actions;

import hudson.model.Action;
import hudson.model.AbstractProject;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Map;

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildHistoryGatherer;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildHistoryGathererImpl;
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

	@Override
	public String getDisplayName() {
		return "Clang scan-build trend";
	}

	@Override
	public String getUrlName() {
		return "clangScanBuildTrend";
	}

    public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {

        if( ChartUtil.awtProblemCause != null ){
            rsp.sendRedirect2( req.getContextPath() + DEFAULT_IMAGE );
            return;
        }
    	
    	Map<Integer,Integer> bugCountsByBuildNumber = gatherer.gatherHistory( project.getLastBuild() );
    	if( bugCountsByBuildNumber.size() <= 0 ){
    		rsp.sendRedirect2( req.getContextPath() + ClangScanBuildUtils.getTransparentImagePath() );
    		return;
    	}
    	
    	new ClangBuildGraph( bugCountsByBuildNumber ).doPng( req, rsp );
    }
    
    public boolean buildDataExists(){
    	Map<Integer,Integer> bugCountsByBuildNumber = gatherer.gatherHistory( project.getLastBuild() );
    	return bugCountsByBuildNumber.size() > 0;
    }
    
}

package jenkins.plugins.clangscanbuild.reports;

import hudson.model.ModelObject;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ClangScanBuildBugSummaryReport implements ModelObject {

	private List<ClangScanBuildBugSummary> bugSummaries;
	private boolean isFailed;
	
	public ClangScanBuildBugSummaryReport( List<ClangScanBuildBugSummary> bugSummaries ){
		super();
		this.bugSummaries = bugSummaries;
	}
	
	@Override
	public String getDisplayName() {
		return "Clang Scan-Build Report";
	}

	/**
	 * Handles /graph web requests
	 */
	public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {

        if( ChartUtil.awtProblemCause != null ){
            // not available. send out error message
            rsp.sendRedirect2( req.getContextPath() + "/images/headless.png" );
            return;
        }
		
        
        ClangBuildGraph graph = new ClangBuildGraph( Calendar.getInstance(), bugSummaries );
        graph.doPng( req, rsp );

	}

	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed( boolean isFailed ){
		this.isFailed = isFailed;
	}

}

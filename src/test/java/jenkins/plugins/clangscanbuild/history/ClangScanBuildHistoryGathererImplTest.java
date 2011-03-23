package jenkins.plugins.clangscanbuild.history;

import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;

import java.util.Map;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class ClangScanBuildHistoryGathererImplTest extends HudsonTestCase{

	private ClangScanBuildHistoryGathererImpl classUnderTest = new ClangScanBuildHistoryGathererImpl( 5 );

	@Test
	public void testBuildSummaryForBuildsExceedingThresholdNotReturned() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
		// The test instance is set to a threshold of 5...builds 2 and 7 should be excluded
		performBuildWithClangAction( project, 1 );
		performBuildWithOutClangAction( project );
		FreeStyleBuild lastBuild = performBuildWithClangAction( project, 3 );

		Map<Integer,Integer> summaries = classUnderTest.gatherHistory( lastBuild );
		System.err.println( summaries );
		Assert.assertEquals( 2, summaries.size() );
		Assert.assertEquals( 1, summaries.get(1) );
		Assert.assertEquals( 3, summaries.get(3) );
	}

	@Test
	public void testFirstBuildDoesNotFail() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
		FreeStyleBuild build1 = performBuildWithClangAction( project, 1 );
		
		Map<Integer,Integer> summaries = classUnderTest.gatherHistory( build1 );
		
		Assert.assertEquals( 1, summaries.size() );
		Assert.assertEquals( 1, summaries.get(1) );
	}
	
	private class TestClangScanBuildAction extends ClangScanBuildAction{

		public TestClangScanBuildAction( AbstractBuild<?,?> build, int bugCount, int threshold ){
			super( build, bugCount, true, threshold, null );
		}

	}
	
	private FreeStyleBuild performBuildWithClangAction( FreeStyleProject project, int bugCount ) throws Exception {
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		build.addAction( new TestClangScanBuildAction( build, bugCount, 0 ) );
		return build;
	}
	
	private FreeStyleBuild performBuildWithOutClangAction( FreeStyleProject project ) throws Exception {
		return project.scheduleBuild2(0).get();
	}

}

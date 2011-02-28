package jenkins.plugins.clangscanbuild.history;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

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
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 1, "one" ) );
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 2, "two" ) );
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 3, "three" ) );
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 4, "four" ) );
		performBuildWithClangAction( project, null );
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 6, "six" ) );
		FreeStyleBuild lastBuild = performBuildWithClangAction( project, buildClangScanBuildBugSummary( 7, "seven" ) );
		
		List<ClangScanBuildBugSummary> summaries = classUnderTest.gatherHistory( lastBuild );
		
		Assert.assertEquals( 5, summaries.size() );
		Assert.assertEquals( "seven", summaries.get(0).getBugs().get(0).getBugDescription() );
		Assert.assertEquals( "six", summaries.get(1).getBugs().get(0).getBugDescription() );
		Assert.assertEquals( "four", summaries.get(2).getBugs().get(0).getBugDescription() );
		Assert.assertEquals( "three", summaries.get(3).getBugs().get(0).getBugDescription() );
		Assert.assertEquals( "two", summaries.get(4).getBugs().get(0).getBugDescription() );
	}

	@Test
	public void testFirstBuildDoesNotFail() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
		FreeStyleBuild build1 = performBuildWithClangAction( project, buildClangScanBuildBugSummary( 1, "one" ) );
		
		List<ClangScanBuildBugSummary> summaries = classUnderTest.gatherHistory( build1 );
		
		Assert.assertEquals( 1, summaries.size() );
		Assert.assertEquals( "one", summaries.get(0).getBugs().get(0).getBugDescription() );
	}
	
	@Test
	public void testBuildWithNullSummaryIgnored() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 1, "one" ) );
		performBuildWithClangAction( project, null );
		FreeStyleBuild build3 = performBuildWithClangAction( project, buildClangScanBuildBugSummary( 3, "three" ) );

		List<ClangScanBuildBugSummary> summaries = classUnderTest.gatherHistory( build3 );
		
		Assert.assertEquals( 2, summaries.size() );
		Assert.assertEquals( "three", summaries.get(0).getBugs().get(0).getBugDescription() );
		Assert.assertEquals( "one", summaries.get(1).getBugs().get(0).getBugDescription() );
	}
	
	@Test
	public void testBuildWithoutActionIgnored() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
		performBuildWithClangAction( project, buildClangScanBuildBugSummary( 1, "one" ) );
		performBuildWithOutClangAction( project );
		FreeStyleBuild build3 = performBuildWithClangAction( project, buildClangScanBuildBugSummary( 3, "three" ) );

		List<ClangScanBuildBugSummary> summaries = classUnderTest.gatherHistory( build3 );
		
		Assert.assertEquals( 2, summaries.size() );
		Assert.assertEquals( "three", summaries.get(0).getBugs().get(0).getBugDescription() );
		Assert.assertEquals( "one", summaries.get(1).getBugs().get(0).getBugDescription() );
	}
	
	private ClangScanBuildBugSummary buildClangScanBuildBugSummary( int buildNumber, String... bugs ){
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( buildNumber );
		for( String bugDescription : bugs ){
			ClangScanBuildBug instance = new ClangScanBuildBug();
			instance.setBugDescription( bugDescription );
			summary.add( instance );
		}
		return summary;
	}
	
	private class TestClangScanBuildAction extends ClangScanBuildAction{

		private ClangScanBuildBugSummary summary;
		
		public TestClangScanBuildAction( ClangScanBuildBugSummary summary ){
			super( null, null );
			this.summary = summary;
		}
		
		public ClangScanBuildBugSummary getBugSummary(){
			return summary;
		}

	}
	
	private FreeStyleBuild performBuildWithClangAction( FreeStyleProject project, ClangScanBuildBugSummary summary ) throws Exception {
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		build.addAction( new TestClangScanBuildAction( summary ) );
		return build;
	}
	
	private FreeStyleBuild performBuildWithOutClangAction( FreeStyleProject project ) throws Exception {
		return project.scheduleBuild2(0).get();
	}

}

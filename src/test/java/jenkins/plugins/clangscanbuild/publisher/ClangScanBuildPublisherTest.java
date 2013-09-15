package jenkins.plugins.clangscanbuild.publisher;

import hudson.model.FreeStyleProject;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class ClangScanBuildPublisherTest extends HudsonTestCase{

	@Test
	public void testRoundTripConfiguration() throws Exception{
		
		FreeStyleProject p = createFreeStyleProject();
		
		ClangScanBuildPublisher publisherBefore = new ClangScanBuildPublisher( true, 45, true, 10 );
		p.getPublishersList().add( publisherBefore );

		HtmlForm form = createWebClient().getPage( p, "configure" ).getFormByName( "config" );
		submit( form );

		ClangScanBuildPublisher publisherAfter = p.getPublishersList().get( ClangScanBuildPublisher.class );

		assertEqualBeans( publisherBefore, publisherAfter, "unstableBugThreshold,markBuildUnstableWhenThresholdIsExceeded,failBugThreshold,markBuildFailedWhenThresholdIsExceeded" );
	}
	
}

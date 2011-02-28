package jenkins.plugins.clangscanbuild.history;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class ClangScanBuildBugSummaryTest {

	@Test
	public void testContains(){
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( 1 );
		Assert.assertTrue( summary.add( buildClangScanBuildBug( "one" ) ) );
		Assert.assertTrue( summary.add( buildClangScanBuildBug( "two" ) ) );
		Assert.assertTrue( summary.add( buildClangScanBuildBug( "three" ) ) );
		
		Assert.assertTrue( summary.contains( buildClangScanBuildBug( "two" ) ) );
		Assert.assertFalse( summary.contains( buildClangScanBuildBug( "four" ) ) );
	}
	
	@Test
	public void testAddBugs(){
		
		Collection<ClangScanBuildBug> previousBugs = new ArrayList<ClangScanBuildBug>();
		previousBugs.add( buildClangScanBuildBug( "two" ) );
		previousBugs.add( buildClangScanBuildBug( "three" ) );
		
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( 1 );
		summary.add( buildClangScanBuildBug( "one" ) );
		summary.addBugs( previousBugs );
		
		Assert.assertTrue( summary.contains( buildClangScanBuildBug( "one" ) ) );
		Assert.assertTrue( summary.contains( buildClangScanBuildBug( "two" ) ) );
		Assert.assertTrue( summary.contains( buildClangScanBuildBug( "three" ) ) );
	}
	
	@Test
	public void testBugCollectionIsImmutable(){
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( 1 );
		summary.add( buildClangScanBuildBug( "one" ) );
		summary.add( buildClangScanBuildBug( "two" ) );
		
		summary.getBugs().remove( buildClangScanBuildBug( "one" ) ); //this wont actually be removed
		
		Assert.assertTrue( summary.contains( buildClangScanBuildBug( "one" ) ) );
	}
	
	@Test
	public void testBugCount(){
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( 1 );
		Assert.assertEquals( 0, summary.getBugCount() );
		
		summary.add( buildClangScanBuildBug( "one" ) );
		summary.add( buildClangScanBuildBug( "two" ) );

		Assert.assertEquals( 2, summary.getBugCount() );
	}
	
	@Test
	public void testBuildNumber(){
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( 1 );
		Assert.assertEquals( 1, summary.getBuildNumber() );
	}
	
	@Test
	public void testDuplicate(){
		ClangScanBuildBugSummary summary = new ClangScanBuildBugSummary( 1 );
		Assert.assertTrue( summary.add( buildClangScanBuildBug( "two" ) ) );
		Assert.assertFalse( summary.add( buildClangScanBuildBug( "two" ) ) );
	}
	
	private ClangScanBuildBug buildClangScanBuildBug( String description ){
		ClangScanBuildBug bug = new ClangScanBuildBug();
		bug.setBugDescription( description );
		return bug;
	}
	
}

package jenkins.plugins.clangscanbuild.portlet.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.joda.time.LocalDate;
import org.kohsuke.stapler.DataBoundConstructor;


public class ClangScanBuildTrendChart extends DashboardPortlet{
	
	@DataBoundConstructor
	public ClangScanBuildTrendChart( String name ){
		super( name );
	}
	
	public static class ClangSummary{
		private Job job;
		private int clangBugs;
		
		public Job getJob() {
			return job;
		}
		public void setJob(Job job) {
			this.job = job;
		}
		public int getClangBugs() {
			return clangBugs;
		}
		public void setClangBugs(int clangBugs) {
			this.clangBugs = clangBugs;
		}
	}
	
	public Graph getSummaryGraph(){
		List<Job> jobs = getDashboard().getJobs();

		// Fill a HashMap with the data will be showed in the chart
		Map<LocalDate, ClangSummary> summaries = loadChartDataWithinRange( jobs, 60 );

		return createTrendChart( summaries, 500, 250 );
	}
	
	public Map<LocalDate, ClangSummary> loadChartDataWithinRange( List<Job> jobs, int daysNumber ){

	    Map<LocalDate, ClangSummary> summaries = new HashMap<LocalDate, ClangSummary>();

	    // Get the last build (last date) of the all jobs
	    LocalDate lastDate = getLastDate( jobs );

	    // No builds
	    if( lastDate == null ) return null;

	    // Get the first date from last build date minus number of days
	    LocalDate firstDate = lastDate.minusDays(daysNumber);

	    // For each job, get Emma coverage results according with
	    // date range (last build date minus number of days)
	    for (Job job : jobs) {

	      Run run = job.getLastBuild();
	      if( run == null ) continue;
	      
	      LocalDate runDate = new LocalDate(run.getTimestamp());

	      while( runDate.isAfter( firstDate ) ){
	          summarize( summaries, run, runDate, job );
	          run = run.getPreviousBuild();
	          if( null == run ) break;

	          runDate = new LocalDate(run.getTimestamp());
	        }
	    }

	    // Sorting by date, ascending order
	    Map<LocalDate, ClangSummary> sortedSummaries = new TreeMap<LocalDate, ClangSummary>( summaries );
	    return sortedSummaries;
	 }
	
	public LocalDate getLastDate( List<Job> jobs ){
		LocalDate lastDate = null;
		for( Job job : jobs ){
			Run lastRun = job.getLastBuild();
			if( lastRun == null ) return null;
	
			LocalDate date = new LocalDate( lastRun.getTimestamp() );
			if( lastDate == null ){
			      lastDate = date;
			}
			if( date.isAfter( lastDate ) ){
			      lastDate = date;
			}
		}
		return lastDate;
	 }
	
	 private static void summarize( Map<LocalDate, ClangSummary> summaries, Run run, LocalDate runDate, Job job ){
		 summaries.put( runDate, getResult( run ) );
	 }
	 
	 private static ClangSummary getResult( Run run ){
		ClangScanBuildAction clangScanBuildAction = run.getAction( ClangScanBuildAction.class );
		if( clangScanBuildAction == null ) return null;
		
		System.err.println( "INSIDE CLANG SCAN BUILD TREND - " + clangScanBuildAction );
		
	    ClangSummary c = new ClangSummary();
	    return c;
	 }
	 
	 private static CategoryDataset buildDataSet( Map<LocalDate, ClangSummary> summaries ){
	    DataSetBuilder<String, LocalDate> dataSetBuilder = new DataSetBuilder<String, LocalDate>();

	    for( Map.Entry<LocalDate, ClangSummary> entry : summaries.entrySet() ){
	    	dataSetBuilder.add( entry.getValue().getClangBugs(), "bugcount", entry.getKey() );
	    }

	    return dataSetBuilder.build();
	 }
	 
	 private static Graph createTrendChart( final Map<LocalDate, ClangSummary> summaries, int widthParam, int heightParam ){
	
		 return new Graph( -1, widthParam, heightParam ){
			   
			 public static final String AXIS_LABEL = "Days";
			 public static final String AXIS_LABEL_VALUE = "Coverage(%)";
			 public static final float LINE_THICKNESS = 3.5f;
			 public static final float FOREGROUND_ALPHA = 0.8f;
			 public static final double DEFAULT_MARGIN = 0.0;
			 public static final int CHART_UPPER_BOUND = 100;
			 public static final int CHART_LOWER_BOUND = 0;
			  
		 	@Override
		 	protected JFreeChart createGraph() {
		 		System.err.println( "INSIDE CREATE TEND CHART" );
		 		// Show empty chart
		 		if( summaries == null ){
		 			return ChartFactory.createStackedAreaChart( null, AXIS_LABEL, AXIS_LABEL_VALUE, null, PlotOrientation.VERTICAL, true, false, false );
		 		}
	
		 		int lineNumber = 0;
	
		 		JFreeChart chart = ChartFactory.createLineChart( "", AXIS_LABEL, AXIS_LABEL_VALUE, buildDataSet( summaries ), PlotOrientation.VERTICAL, true, false, false );
		 		chart.setBackgroundPaint( Color.white );
	
		 		CategoryPlot plot = chart.getCategoryPlot();
	
		 		// Line thickness
		 		CategoryItemRenderer renderer = plot.getRenderer();
		 		BasicStroke stroke = new BasicStroke( LINE_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
		 		renderer.setSeriesStroke( lineNumber++, stroke );
		 		renderer.setSeriesStroke( lineNumber++, stroke );
		 		renderer.setSeriesStroke( lineNumber++, stroke );
		 		renderer.setSeriesStroke( lineNumber, stroke );
	
		 		plot.setBackgroundPaint( Color.WHITE );
		 		plot.setOutlinePaint( null );
		 		plot.setForegroundAlpha( FOREGROUND_ALPHA );
		 		plot.setRangeGridlinesVisible( true );
	        	plot.setRangeGridlinePaint( Color.black );
	
		        CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
		        plot.setDomainAxis( domainAxis );
		        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
		        domainAxis.setLowerMargin( DEFAULT_MARGIN );
		        domainAxis.setUpperMargin( DEFAULT_MARGIN );
		        domainAxis.setCategoryMargin( DEFAULT_MARGIN );
	
		        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
		        rangeAxis.setUpperBound( CHART_UPPER_BOUND );
		        rangeAxis.setLowerBound( CHART_LOWER_BOUND );
	
		        return chart;
	       }
		 	
	    };
	    
	 }

	 @Extension
	 public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
		 @Override
		 public String getDisplayName() {
			 return "Clang Scan-Build Trend";
		 }
	    
	 }

}
package jenkins.plugins.clangscanbuild.reports;

import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Calendar;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.ui.RectangleInsets;

/**
 * This is called from the report page
 * 
 * @author Joshua Kennedy
 */
public class ClangBuildGraph extends Graph{
	
	private Map<Integer, Integer> bugCountsByBuildNumber;

	public ClangBuildGraph( Map<Integer, Integer> bugCountsByBuildNumber ){
		super( Calendar.getInstance(), 350, 150 );
		this.bugCountsByBuildNumber = bugCountsByBuildNumber;
	}
	
	@Override
	protected JFreeChart createGraph(){
		
		DataSetBuilder<String, String> dataSetBuilder = new DataSetBuilder<String, String>();
		int largestBugCount = 0;
		for( Integer buildNumber : bugCountsByBuildNumber.keySet() ){
			Integer bugCount = bugCountsByBuildNumber.get( buildNumber );
			 dataSetBuilder.add( bugCount, "bugcount", String.valueOf( buildNumber ) );
			 if( bugCount > largestBugCount ) largestBugCount = bugCount;
		}

        final JFreeChart chart = ChartFactory.createLineChart(
            null, // chart title
            null, // unused
            "Bugs", // range axis label
            dataSetBuilder.build(), // data
            PlotOrientation.VERTICAL, // orientation
            false, // include legend
            false, // tooltips
            false // urls
        );

        chart.setBackgroundPaint( Color.white );

        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis( "Build Number" );
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        rangeAxis.setUpperBound( largestBugCount + 5 );
        rangeAxis.setLowerBound( 0 );

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke( new BasicStroke( 4.0f ) );
        ColorPalette.apply( renderer );

        // crop extra space around the graph
        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

        return chart;
	}

}
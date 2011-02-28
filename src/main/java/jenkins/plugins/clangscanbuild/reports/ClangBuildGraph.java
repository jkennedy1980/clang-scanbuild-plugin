package jenkins.plugins.clangscanbuild.reports;

import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Calendar;
import java.util.List;

import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * This is called from the report page
 * 
 * @author Joshua Kennedy
 */
public class ClangBuildGraph extends Graph{

	private List<ClangScanBuildBugSummary> bugSummaries;
	
	public ClangBuildGraph( Calendar timestamp, List<ClangScanBuildBugSummary> bugSummaries ){
		super( timestamp, 350, 150 );
		this.bugSummaries = bugSummaries;
	}
	
	@Override
	protected JFreeChart createGraph(){
		
		DataSetBuilder<String, String> dataSetBuilder = new DataSetBuilder<String, String>();
		int largestBugCount = 0;
		for( ClangScanBuildBugSummary bugSummary : bugSummaries ){
			 dataSetBuilder.add( bugSummary.getBugCount(), "bugcount", String.valueOf( bugSummary.getBuildNumber() ) );
			 if( bugSummary.getBugCount() > largestBugCount ){
				 largestBugCount = bugSummary.getBugCount();
			 }
		}

        final JFreeChart chart = ChartFactory.createLineChart(
                null, // chart title
                null, // unused
                "%", // range axis label
                dataSetBuilder.build(), // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );
        

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound( largestBugCount + 10 );
        rangeAxis.setLowerBound(0);

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(4.0f));
        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
	}

}
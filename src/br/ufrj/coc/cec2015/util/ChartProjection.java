package br.ufrj.coc.cec2015.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartProjection extends JFrame {
	private static final long serialVersionUID = 9036503967468200772L;
	
	private static final String DEFAULT_CHART_TITLE = "Scatter Chart";
	private static final String DEFAULT_SERIES_DESCRIPTION = "Generation";

	private XYSeries series = new XYSeries(DEFAULT_SERIES_DESCRIPTION);
	final ChartPanel chartPanel;

	public ChartProjection(String s) {
		super(s);
		initialize();
		chartPanel = createProjectionsPanel();
		this.add(chartPanel, BorderLayout.CENTER);
	}
	
	private void initialize() {
    	double[][] initialData = new double[1][2];
		for (int i = 0; i < initialData.length; i++) {
			series.add(initialData[i][0], initialData[i][1]);
		}
	}

	public void update(double[][] projectionsData, String chartTitle, String seriesDescription) {
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            	chartPanel.getChart().setTitle(chartTitle);
            	series.clear();
        		for (int i = 0; i < projectionsData.length; i++) {
        			series.add(projectionsData[i][0], projectionsData[i][1]);
        		}
            	series.setDescription(seriesDescription);
            }
        });
	}
	
	@SuppressWarnings("serial")
	private ChartPanel createProjectionsPanel() {
		JFreeChart jfreechart = ChartFactory.createScatterPlot(DEFAULT_CHART_TITLE, "X", "Y", createSampleData(), PlotOrientation.VERTICAL, true, true, true);
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
		domain.setRange(Properties.SEARCH_RANGE[0], Properties.SEARCH_RANGE[1]);
		domain.setTickUnit(new NumberTickUnit(20));
		NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
		range.setRange(Properties.SEARCH_RANGE[0], Properties.SEARCH_RANGE[1]);
		range.setTickUnit(new NumberTickUnit(20));
		return new ChartPanel(jfreechart) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 600);
			}
		};
	}

	private XYDataset createSampleData() {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		xySeriesCollection.addSeries(series);
		return xySeriesCollection;
	}

	public void exportToPng(String pngFilename) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
        		try {
        			OutputStream out = new FileOutputStream(pngFilename);
        			ChartUtilities.writeChartAsPNG(out, chartPanel.getChart(), chartPanel.getWidth(), chartPanel.getHeight());
        		} catch (IOException ex) {
        			throw new RuntimeException(ex);
        		}
            }
        });
	}
}
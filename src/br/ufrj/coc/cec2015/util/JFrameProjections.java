package br.ufrj.coc.cec2015.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
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
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class JFrameProjections extends JFrame {
	private static final long serialVersionUID = 9036503967468200772L;

	private static String DEFAULT_CHART_TITLE = "Scatter Chart";
	private static String DEFAULT_SERIES_DESCRIPTION = "População (" + Properties.ARGUMENTS.get().getPopulationSize() + ")";

	private XYSeries series = new XYSeries(DEFAULT_SERIES_DESCRIPTION);
	private XYSeries best = new XYSeries("Melhor indivíduo");
	private final ChartPanel chartPanel;

	public JFrameProjections(String s) {
		super(s);
		initialize();
		chartPanel = createProjectionsPanel();
		this.add(chartPanel, BorderLayout.CENTER);
	}

	private void initialize() {
		double[][] initialData = new double[1][2];
		this.best.add(0, 0);
		for (int i = 0; i < initialData.length; i++) {
			this.series.add(initialData[i][0], initialData[i][1]);
		}
	}

	public void close() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public void update(double[][] projectionsSeries, int bestIndex, int round) {
		String algorithmName = Properties.ARGUMENTS.get().getName();
		String chartTitle = "Projeções no plano formado pelas componentes principais V1 e V2";
		String subTitleMain = algorithmName + " - Função F" + Properties.ARGUMENTS.get().getFunctionNumber() + " - Dimensão " + Properties.ARGUMENTS.get().getIndividualSize();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				chartPanel.getChart().setTitle(chartTitle);
				chartPanel.getChart().clearSubtitles();
				chartPanel.getChart().addSubtitle(new TextTitle(subTitleMain));
				chartPanel.getChart().addSubtitle(new TextTitle("Rodada " + (round + 1)));

				series.clear();
				best.clear();
				for (int i = 0; i < projectionsSeries.length; i++) {
					XYDataItem dataItem = new XYDataItem(projectionsSeries[i][0], projectionsSeries[i][1]);
					if (i == bestIndex)
						best.add(dataItem);
					else
						series.add(dataItem);
				}
			}
		});
	}

	@SuppressWarnings("serial")
	private ChartPanel createProjectionsPanel() {
		JFreeChart jfreechart = ChartFactory.createScatterPlot(DEFAULT_CHART_TITLE, "X (Indivíduo . V1)", "Y (Indivíduo . V2)", createSampleData(), PlotOrientation.VERTICAL, true, true, true);
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();

		XYItemRenderer renderer = xyPlot.getRenderer();
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
		
		renderer.setSeriesPaint(1, Color.RED);
		renderer.setSeriesShape(1, new Ellipse2D.Double(-4, -4, 8, 8));

		adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
		adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
		
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);

		return new ChartPanel(jfreechart) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 650);
			}
		};
	}

	private void adjustAxis(NumberAxis axis, boolean vertical) {
		axis.setRange(Properties.SEARCH_RANGE[0] - 50, Properties.SEARCH_RANGE[1] + 50);
		axis.setTickUnit(new NumberTickUnit(25));
		axis.setVerticalTickLabels(vertical);
	}

	private XYDataset createSampleData() {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		xySeriesCollection.addSeries(this.series);
		xySeriesCollection.addSeries(this.best);
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
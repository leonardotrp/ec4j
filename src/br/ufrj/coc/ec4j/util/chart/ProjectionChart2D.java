package br.ufrj.coc.ec4j.util.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

import br.ufrj.coc.ec4j.util.Properties;

public class ProjectionChart2D extends JFrame {
	private static final long serialVersionUID = 9036503967468200772L;

	private static String DEFAULT_CHART_TITLE = "Chart Projection";
	private static String DEFAULT_SERIES_DESCRIPTION = "População (" + Properties.ARGUMENTS.get().getPopulationSize() + ")";

	private XYSeries series = new XYSeries(DEFAULT_SERIES_DESCRIPTION);
	private XYSeries best = new XYSeries("Melhor indivíduo");
	private final ChartPanel chartPanel;

	public ProjectionChart2D(String s) {
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

	public class ProjectionData {
		private double[][] series;
		private int best;
		private String title;
		private List<String> subTitles = new ArrayList<String>();
		private String pngFilename;

		public ProjectionData() {
			super();
		}

		public double[][] getSeries() {
			return series;
		}

		public void setSeries(double[][] series) {
			this.series = series;
		}

		public int getBest() {
			return best;
		}

		public void setBest(int best) {
			this.best = best;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<String> getSubTitles() {
			return subTitles;
		}

		public String getPngFilename() {
			return pngFilename;
		}

		public void setPngFilename(String pngFilename) {
			this.pngFilename = pngFilename;
		}
	}

	public void update(ProjectionData data) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				chartPanel.getChart().setTitle(data.getTitle());

				chartPanel.getChart().clearSubtitles();
				for (String subTitle : data.getSubTitles())
					chartPanel.getChart().addSubtitle(new TextTitle(subTitle));

				series.clear();
				best.clear();

				for (int i = 0; i < data.series.length; i++) {
					XYDataItem dataItem = new XYDataItem(data.series[i][0], data.series[i][1]);
					if (i == data.best)
						best.add(dataItem);
					else
						series.add(dataItem);
				}

				if (data.getPngFilename() != null) {
					try {
						OutputStream out = new FileOutputStream(data.getPngFilename());
						ChartUtilities.writeChartAsPNG(out, chartPanel.getChart(), chartPanel.getWidth(), chartPanel.getHeight());
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
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
		double[] range = Properties.getSearchRange();
		axis.setRange(range[0] - 50, range[1] + 50);
		axis.setTickUnit(new NumberTickUnit(25));
		axis.setVerticalTickLabels(vertical);
	}

	private XYDataset createSampleData() {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		xySeriesCollection.addSeries(this.series);
		xySeriesCollection.addSeries(this.best);
		return xySeriesCollection;
	}
}
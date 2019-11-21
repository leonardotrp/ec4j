package br.ufrj.coc.cec2015.util.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class EvolutionChart2D extends JFrame {
	private static final long serialVersionUID = 9036503967468200772L;
	private static final Color[] COLORS = new Color[] { Color.BLUE, Color.RED, Color.DARK_GRAY, Color.ORANGE, Color.CYAN };

	private static String DEFAULT_CHART_TITLE = "Gráfico de Evolução dos Erros";

	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
	private boolean empty = true;
	private ChartPanel chartPanel;
	private String yAxisLabel;

	public EvolutionChart2D(String yAxisLabel) {
		super("Evolution Chart2D");
		chartPanel = createEvolutionPanel(yAxisLabel);
		super.add(chartPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}

	public void setTitle(String title, String... subTitles) {
		chartPanel.getChart().setTitle(title + "\n\r" + subTitles[0]);
	}

	public void addSerie(List<Double> serieX, List<Double> serieY, String description) {
		if (empty) {
			xySeriesCollection.removeSeries(0);
			empty = false;
		}

		XYSeries xySeries = createXYSeries(serieX, serieY, description);
		xySeriesCollection.addSeries(xySeries);
	}

	private XYSeries createXYSeries(List<Double> serieX, List<Double> serieY, String description) {
		XYSeries xySeries = new XYSeries(description);
		for (int idx = 0; idx < serieX.size(); idx++) {
			XYDataItem dataItem = new XYDataItem(serieX.get(idx), serieY.get(idx));
			xySeries.add(dataItem);
		}
		return xySeries;
	}

	public void toFile(String filePng) {
		try {
			OutputStream out = new FileOutputStream(filePng);
			ChartUtilities.writeChartAsPNG(out, chartPanel.getChart(), chartPanel.getWidth(), chartPanel.getHeight());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	JFreeChart createScatterPlot(String title, String xAxisLabel, String yAxisLabel, XYDataset dataset, PlotOrientation orientation) {

		ParamChecks.nullNotPermitted(orientation, "orientation");
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(yAxisLabel);
		yAxis.setAutoRangeIncludesZero(false);

		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true);
		plot.setRenderer(renderer);
		plot.setOrientation(orientation);

		for (int idx = 0; idx < COLORS.length; idx++) {
			renderer.setSeriesPaint(idx, COLORS[idx]);
		}
		
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		
		return chart;

	}

	@SuppressWarnings("serial")
	private ChartPanel createEvolutionPanel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
		JFreeChart jfreechart = createScatterPlot(DEFAULT_CHART_TITLE, "Percentual de Avaliação (% MaxFES = Dim.10000)", yAxisLabel, this.createSampleData(), PlotOrientation.VERTICAL);
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();

		LogAxis yAxis = new LogAxis(this.yAxisLabel);
		NumberFormat numberFormat = new DecimalFormat("0.0E0");
		yAxis.setNumberFormatOverride(numberFormat);
		xyPlot.setRangeAxis(yAxis);

		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        LegendTitle legend = new LegendTitle(xyPlot);
        legend.setVisible(true);
        legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        legend.setFrame(new LineBorder());
        legend.setBackgroundPaint(Color.white);
        legend.setPosition(RectangleEdge.BOTTOM);
        jfreechart.addLegend(legend);
		
		return new ChartPanel(jfreechart) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 650);
			}
		};
	}

	private XYDataset createSampleData() {
		XYSeries series = new XYSeries("");
		double[][] initialData = new double[1][2];
		for (int i = 0; i < initialData.length; i++) {
			series.add(initialData[i][0], initialData[i][1]);
		}
		this.xySeriesCollection.addSeries(series);
		return this.xySeriesCollection;
	}
	
	public void close() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
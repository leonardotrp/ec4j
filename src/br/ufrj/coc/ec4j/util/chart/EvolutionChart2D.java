package br.ufrj.coc.ec4j.util.chart;

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
	private static final Color[] COLORS1 = new Color[] { Color.BLUE, Color.RED, Color.BLACK, Color.MAGENTA, Color.YELLOW};
	private static final Color[] COLORS2 = new Color[] { Color.DARK_GRAY, Color.LIGHT_GRAY, Color.ORANGE, Color.PINK, Color.GREEN};

	private static String DEFAULT_CHART_TITLE = "Gráfico de Evolução dos Erros";

	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
	XYSeriesCollection xySeriesCollection2 = new XYSeriesCollection();
	private boolean empty1 = true, empty2 = true;
	private ChartPanel chartPanel;

	public EvolutionChart2D(String yAxisLabel, String yAxisLabel2) {
		super("Evolution Chart2D");
		chartPanel = createEvolutionPanel(yAxisLabel, yAxisLabel2);
		super.add(chartPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}

	public void setTitle(String title, String... subTitles) {
		chartPanel.getChart().setTitle(title + "\n\r" + subTitles[0]);
	}

	public void addSerie(List<Double> serieX, List<Double> serieY, String description, int yAxis) {
		XYSeries xySeries = createXYSeries(serieX, serieY, description);
		if (yAxis == 0 && empty1) {
			xySeriesCollection.removeSeries(0);
			empty1 = false;
		}
		if (yAxis == 1 && empty2) {
			xySeriesCollection2.removeSeries(0);
			empty2 = false;
		}
		if (yAxis == 0)
			xySeriesCollection.addSeries(xySeries);
		else if (yAxis == 1)
			xySeriesCollection2.addSeries(xySeries);
	}

	private XYSeries createXYSeries(List<Double> serieX, List<Double> serieY, String description) {
		XYSeries xySeries = new XYSeries(description);
		for (int idx = 0; idx < serieY.size(); idx++) {
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

	JFreeChart createScatterPlot(String title, String xAxisLabel, String yAxisLabel, String yAxisLabel2, XYDataset dataset1, XYDataset dataset2, PlotOrientation orientation) {
		ParamChecks.nullNotPermitted(orientation, "orientation");
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis1 = new NumberAxis(yAxisLabel);
		yAxis1.setAutoRangeIncludesZero(false);

		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, true);
		XYPlot plot = new XYPlot(dataset1, xAxis, yAxis1, renderer1);
		plot.setOrientation(orientation);
		for (int idx = 0; idx < COLORS1.length; idx++)
			renderer1.setSeriesPaint(idx, COLORS1[idx]);

		if (yAxisLabel2 != null) {
			NumberAxis yAxis2 = new NumberAxis(yAxisLabel2);
			yAxis2.setAutoRangeIncludesZero(false);
	
			XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, true);
			plot.setDataset(1, dataset2);
			plot.setDomainAxis(xAxis);
			plot.setRangeAxis(1, yAxis2);
			plot.mapDatasetToRangeAxis(1, 1);
			plot.setRenderer(1, renderer2);
			for (int idx = 0; idx < COLORS2.length; idx++)
				renderer2.setSeriesPaint(idx, COLORS2[idx]);
		}

		return new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	}

	private static XYDataset createSampleData(XYSeriesCollection xySeriesCollection) {
		XYSeries series = new XYSeries("");
		double[][] initialData = new double[1][2];
		for (int i = 0; i < initialData.length; i++) {
			series.add(initialData[i][0], initialData[i][1]);
		}
		xySeriesCollection.addSeries(series);
		return xySeriesCollection;
	}
	
	@SuppressWarnings("serial")
	private ChartPanel createEvolutionPanel(String yAxisLabel, String yAxisLabel2) {
		XYDataset sampleDataY1 = createSampleData(this.xySeriesCollection);
		XYDataset sampleDataY2 = yAxisLabel2 != null ? createSampleData(this.xySeriesCollection2) : null;
		
		JFreeChart jfreechart = createScatterPlot(DEFAULT_CHART_TITLE, "Percentual de Avaliação (% MaxFES = Dim.10000)", yAxisLabel, yAxisLabel2, sampleDataY1, sampleDataY2, PlotOrientation.VERTICAL);
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();

		LogAxis yAxis = new LogAxis(yAxisLabel);
		NumberFormat numberFormat = new DecimalFormat("0.0E0");
		yAxis.setNumberFormatOverride(numberFormat);
		xyPlot.setRangeAxis(yAxis);

		if (yAxisLabel2 != null) {
			LogAxis yAxis2 = new LogAxis(yAxisLabel2);
			yAxis2.setNumberFormatOverride(numberFormat);
			xyPlot.setRangeAxis(1, yAxis2);
		}
		
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

	public void close() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
package br.ufrj.coc.cec2015.util.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class EvolutionChart2D extends JFrame {
	private static final long serialVersionUID = 9036503967468200772L;

	private static String DEFAULT_CHART_TITLE = "Gráfico de Evolução dos Erros";

	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
	private final ChartPanel chartPanel;

	public EvolutionChart2D(List<Double> serieX, List<Double> serieY, String description) {
		super("Evolution Chart2D");
		
		this.addSerie(serieX, serieY, description);
		
		chartPanel = createEvolutionPanel();
		super.add(chartPanel, BorderLayout.CENTER);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void setTitle(String title, String... subTitles) {
		chartPanel.getChart().setTitle(title);
		chartPanel.getChart().clearSubtitles();
		for (String subTitle : subTitles)
			chartPanel.getChart().addSubtitle(new TextTitle(subTitle));
	}

	public void addSerie(List<Double> serieX, List<Double> serieY, String description) {
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

	@SuppressWarnings("serial")
	private ChartPanel createEvolutionPanel() {
		JFreeChart jfreechart = ChartFactory.createXYLineChart(DEFAULT_CHART_TITLE, "Percentual de Avaliação (% MaxFES = Dim.10000)", "Média dos Erros", this.xySeriesCollection, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		
		LogarithmicAxis yAxis = new LogarithmicAxis("Média dos Erros");
		NumberFormat numberFormat = new DecimalFormat("0.####E0");
		//numberFormat.setMinimumIntegerDigits(1);
		//numberFormat.setMaximumFractionDigits(2);
		yAxis.setNumberFormatOverride(numberFormat);
		xyPlot.setRangeAxis(yAxis);
		
		/*
		 * XYItemRenderer renderer = xyPlot.getRenderer(); renderer.setSeriesPaint(0,
		 * Color.BLUE); renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
		 * 
		 * renderer.setSeriesPaint(1, Color.RED); renderer.setSeriesShape(1, new
		 * Ellipse2D.Double(-4, -4, 8, 8));
		 */
		//adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
		//adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);

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
}
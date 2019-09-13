package br.ufrj.coc.cec2015.util.chart;

import java.awt.Font;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.GridArrangement;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.DefaultXYDataset;

public class GridLegendDemo{
    public GridLegendDemo() {
        JFrame frame = new JFrame("Grid Legend Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultXYDataset dataset1 = new DefaultXYDataset();
        for(int i = 0; i< 8; i++){
            double[][] data = new double[2][10];
            for(int j = 0; j < 10; j++){
                data[0][j] = j;
                data[1][j] = i*j;
            }
            dataset1.addSeries("Series " + i, data);
        }
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setUseOutlinePaint(true);
        XYPlot plot = new XYPlot(dataset1,new NumberAxis("x"),new NumberAxis("y"),renderer);
        JFreeChart chart = new JFreeChart("Grid Legend Demo", new Font("Arial", 1, 12), plot, false);
        LegendTitle legend = new LegendTitle(plot, new GridArrangement(4, 2), new GridArrangement(4, 2));
        chart.addLegend(legend);
		ChartPanel cpanel = new ChartPanel(chart);
		frame.getContentPane().add(cpanel);
		frame.pack();
		frame.setVisible(true);
    }
    public static void main(String[] args){
        GridLegendDemo demo = new GridLegendDemo();
    }
}
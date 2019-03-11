package br.ufrj.coc.cec2015.util;

import java.util.Locale;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.Builder;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

public class PlotUtil {
	public static void main(String[] args) throws Exception {
		String fileCsv = "C:\\dev\\workspace\\CEC2015\\results\\8cd4004d-376f-42cc-acaa-2cc1c009fa32\\DE\\1_projection.csv";
		// Table table = Table.read().csv(fileCsv);

		ColumnType[] types = new ColumnType[21];
		for (int i = 0; i < 21; i++)
			types[i] = ColumnType.DOUBLE;

		Builder builder = CsvReadOptions.builder(fileCsv).separator(',').header(false).locale(Locale.ENGLISH).columnTypes(types);
		CsvReadOptions options = builder.build();

		Table table = Table.read().csv(options);

		System.out.println(table.printAll());

		Trace[] traces = new ScatterTrace[10];
		
		Layout layout = Layout.builder().title("Scatter").height(600).width(800).xAxis(Axis.builder().title("Eigenvector V1").build()).yAxis(Axis.builder().title("Eigenvector V2").build()).build();
		for (int i = 0; i < 10; i++) {
			String xCol = "C" + (2*i);
			String yCol = "C" + (2*i + 1);
			String name = "(VX" + (i+1) + ", " + "VX" + (i+1) + ")";
			ScatterTrace trace = ScatterTrace.builder(table.numberColumn(xCol), table.numberColumn(yCol)).name(name).build();
			traces[i] = trace;
		}
		Figure figure = new Figure(layout, traces);

		Plot.show(figure);
	}
}

package br.ufrj.coc.ec4j.util;
/*
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.Builder;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.components.Page;
import tech.tablesaw.plotly.traces.ScatterTrace;
*/
public class TableSawUtil {/*
	private static Figure getFigure(String csvFile) throws IOException {
		ColumnType[] types = new ColumnType[] { ColumnType.DOUBLE, ColumnType.DOUBLE };
		Builder builder = CsvReadOptions.builder(csvFile).separator(',').header(false).locale(Locale.ENGLISH).columnTypes(types);
		CsvReadOptions options = builder.build();

		Table table = Table.read().csv(options);

		System.err.println(table.printAll());

		Layout layout = Layout.builder().title("Scatter").height(600).width(800).xAxis(Axis.builder().title("VX").build()).yAxis(Axis.builder().title("VY").build()).build();
		
		//Trace[] traces = new ScatterTrace[10];
		//for (int i = 0; i < 10; i++) {
		//	String xCol = "C" + (2 * i);
		//	String yCol = "C" + (2 * i + 1);
		//	String name = "(VX" + (i + 1) + ", " + "VX" + (i + 1) + ")";
		//	ScatterTrace trace = ScatterTrace.builder(table.numberColumn(xCol), table.numberColumn(yCol)).name(name).build();
		//	traces[i] = trace;
		//}

		ScatterTrace trace = ScatterTrace.builder(table.numberColumn("C0"), table.numberColumn("C1")).build();
		return new Figure(layout, trace);
	}
	
	public static void main(String[] args) throws IOException {
		/*
		String csvFile = "C:\\dev\\workspace\\CEC2015\\results\\projection_round.csv";
		Figure figure = getFigure(csvFile);
		plotToFile(figure, csvFile.replace(".csv", ".html"));
		*
		plotAll();
	}
	
	public static void plotAll() throws IOException {
		String dir = "C:/dev/workspace/CEC2015/results/projections";
		Files.list(Paths.get(dir)).forEach(cvsFile -> {
			try {
				Figure figure = getFigure(cvsFile.toString());
				plotToFile(figure, cvsFile.toString().replace(".csv", ".html"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

    private static void plotToFile(Figure figure, String outputFile) {
        Page page = Page.pageBuilder(figure, "target").build();
        String output = page.asJavascript();
        try {
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
}

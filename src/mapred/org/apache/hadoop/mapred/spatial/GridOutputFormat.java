package org.apache.hadoop.mapred.spatial;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.spatial.CellInfo;
import org.apache.hadoop.spatial.Shape;
import org.apache.hadoop.util.Progressable;

public class GridOutputFormat<S extends Shape> extends FileOutputFormat<IntWritable, Text> {
  public static final String OUTPUT_CELLS = "edu.umn.cs.spatial.mapReduce.GridOutputFormat.CellsInfo";
  public static final String OVERWRITE = "edu.umn.cs.spatial.mapReduce.GridOutputFormat.Overwrite";
  public static final String RTREE = "edu.umn.cs.spatial.mapReduce.GridOutputFormat.RTree";

  @Override
  public RecordWriter<IntWritable, Text> getRecordWriter(FileSystem ignored,
      JobConf job,
      String name,
      Progressable progress)
      throws IOException {
    // Output file name
    Path outFile = FileOutputFormat.getTaskOutputPath(job, name);

    // Get file system
    FileSystem fileSystem = outFile.getFileSystem(job);

    // Get grid info
    CellInfo[] cellsInfo = decodeCells(job.get(OUTPUT_CELLS));
    boolean overwrite = job.getBoolean(OVERWRITE, false);
    GridRecordWriter writer =
        new GridRecordWriter(fileSystem, outFile, cellsInfo, overwrite);
    return writer;
  }
  
  public static String encodeCells(CellInfo[] cellsInfo) {
    String encodedCellsInfo = "";
    for (CellInfo cellInfo : cellsInfo) {
      if (encodedCellsInfo.length() > 0)
        encodedCellsInfo += ";";
      Text text = new Text();
      cellInfo.toText(text);
      encodedCellsInfo += text.toString();
    }
    return encodedCellsInfo;
  }
  
  public static CellInfo[] decodeCells(String encodedCells) {
    String[] parts = encodedCells.split(";");
    CellInfo[] cellsInfo = new CellInfo[parts.length];
    for (int i = 0; i < parts.length; i++) {
      cellsInfo[i] = new CellInfo(parts[i]);
    }
    return cellsInfo;
  }
}


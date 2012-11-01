package edu.umn.cs.spatialHadoop.mapReduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.spatial.Shape;


/**
 * An input format used with spatial data. It filters generated splits before
 * creating record readers.
 * @author eldawy
 *
 * @param <S>
 */
public class ShapeInputFormat<S extends Shape> extends SpatialInputFormat<LongWritable, S> {

  @Override
  public RecordReader<LongWritable, S> getRecordReader(InputSplit split,
      JobConf job, Reporter reporter) throws IOException {
    // Create record reader
    reporter.setStatus(split.toString());
    return new ShapeRecordReader<S>(job, (FileSplit)split);
  }
}

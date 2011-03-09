package edu.umn.cs.spatial.mapReduce;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.net.NetworkTopology;

import edu.umn.edu.spatial.Rectangle;



/**
 * Reads and parses a file that contains records of type Rectangle.
 * Records are assumed to be fixed size and of the format
 * <id>,<left>,<top>,<right>,<bottom>
 * When a record of all zeros is encountered, it is assumed to be the end of file.
 * This means, no more records are processed after a zero-record.
 * Records are read one be one.
 * @author aseldawy
 *
 */
public class SpatialRangeInputFormat extends FileInputFormat<Rectangle, Rectangle> {

	@Override
	public RecordReader<Rectangle, Rectangle> getRecordReader(InputSplit split,
			JobConf job, Reporter reporter) throws IOException {
	    reporter.setStatus(split.toString());
		@SuppressWarnings("unchecked")
		Class<RecordReader<Rectangle, Rectangle>> klass =
			(Class<RecordReader<Rectangle, Rectangle>>) RectangleRecordReader.class
				.asSubclass(RecordReader.class);

		return new RectangleRecordReader((FileSplit)split, job, reporter);
	}

	@Override
	public InputSplit[] getSplits(JobConf job, int numSplits) 
	throws IOException {
	    FileStatus[] files = listStatus(job);
	    
	    // Save the number of input files for metrics/loadgen
	    job.setLong(NUM_INPUT_FILES, files.length);
	    long totalSize = 0;                           // compute total size
	    for (FileStatus file: files) {                // check we have valid files
	      if (file.isDirectory()) {
	        throw new IOException("Not a file: "+ file.getPath());
	      }
	      totalSize += file.getLen();
	    }

	    // generate splits
	    ArrayList<FileSplit> splits = new ArrayList<FileSplit>(numSplits);
	    NetworkTopology clusterMap = new NetworkTopology();
	    int i = 0;
	    for (FileStatus file: files) {
	      Path path = file.getPath();
	      FileSystem fs = path.getFileSystem(job);
	      long length = file.getLen();
	      BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, length);
	      if ((length != 0) && isSplitable(fs, path)) {
		    	Vector<Long> starts = new Vector<Long>();
		    	Vector<Long> lengths = new Vector<Long>();
		    	String blocks2readStr = job.get(SpatialJoinInputFormat.BLOCKS2READ+'.'+i++, "a");
		    	SplitCalculator.calculateSplits(job, file, starts, lengths, blocks2readStr);

				for (int splitNum = 0; splitNum < starts.size(); splitNum++) {
					long splitStart = starts.elementAt(splitNum);
					long splitSize = lengths.elementAt(splitNum);
					String[] splitHosts = getSplitHosts(blkLocations,
							splitStart, splitSize, clusterMap);
					splits.add(makeSplit(path, splitStart,
							splitSize, splitHosts));
				}
	      } else { 
	        //Create empty hosts array for zero length files
	        splits.add(makeSplit(path, 0, length, new String[0]));
	      }
	    }
	    LOG.debug("Total # of splits: " + splits.size());
	    return splits.toArray(new FileSplit[splits.size()]);
	}

}

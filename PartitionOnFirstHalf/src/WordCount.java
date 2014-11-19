import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {

	public static class Map extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		private IntWritable one = new IntWritable(1);

		/**
		 * Called once for each key-value pair in the input split
		 */
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();

			// remove punctuation chars, convert to lower
			// case and split on 1 or more white spaces
			String[] tokens = line.replaceAll("[^a-zA-Z ]", "").toLowerCase()
					.split("\\s+");

			for (int i = 0; i < tokens.length; i++) {
				// emit some dummy text to demonstrate the partitioning
				// on the first part of the composite key
				context.write(new Text(tokens[i] + " " + "dummy_text"), one);
			}

		}

	}

	public static class Reduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable intWritable : values) {
				sum += intWritable.get();
			}
			// the first part of the key is the word occurred in the
			// the document=>discarding second dummy part of the key
			Text nk = new Text(key.toString().split(" ")[0]);

			context.write(nk, new IntWritable(sum));

		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "custom partitioning");
		job.setJarByClass(WordCount.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setPartitionerClass(CustomPartitioner.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(
				"hdfs://localhost:54310/custom_partitioning/input/"));
		FileOutputFormat.setOutputPath(job, new Path(
				"hdfs://localhost:54310/custom_partitioning/output"));
		job.waitForCompletion(true);
	}

}
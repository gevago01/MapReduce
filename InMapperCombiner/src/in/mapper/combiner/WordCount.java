package in.mapper.combiner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

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
		private HashMap<String, Integer> word_freq = new HashMap<String, Integer>();

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

			for (String word : tokens) {
				//get word frequency
				Integer freq = word_freq.get(word);

				if (freq == null) {
					// map does not contain key
					word_freq.put(word, 1);
				} else {
					// key already in the map
					word_freq.put(word, freq.intValue() + 1);
				}
			}

		}

		/**
		 * Called once at the end of the task- after all key/value pairs have
		 * been presented to the map method. I will use this method for
		 * implementing an in mapper combiner. CAUTION: Assumes that the data is
		 * small enough to reside in mapper's main memory. In case of bigger
		 * buffering buffering should be used.(once buffer full, remove and emit
		 * pairs in the context)
		 */
		@Override
		protected void cleanup(Context con) {
			Set<String> keyset = word_freq.keySet();

			for (String word : keyset) {
				Text word_text = new Text(word);
				IntWritable iw = new IntWritable(word_freq.get(word));
				try {
					con.write(word_text, iw);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "in_mapper_combiner");
		job.setJarByClass(WordCount.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(
				"hdfs://localhost:54310/in_mapper_combiner/input/"));
		FileOutputFormat.setOutputPath(job, new Path(
				"hdfs://localhost:54310/in_mapper_combiner/output"));
		job.waitForCompletion(true);
	}

}
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class CustomPartitioner extends Partitioner<Text, IntWritable> {
	HashPartitioner<Text, IntWritable> hashPartitioner = new HashPartitioner<Text, IntWritable>();
	

	@Override
	public int getPartition(Text key, IntWritable value, int numPartitions) {
		//split the composite key
		String []tokens=key.toString().split(" ");
		//partitioning must be performed on tokens[0] 
		//the first part of the composite key
		try {
			// Execute the default partitioner over the first part of the key
			Text newKey = new Text(tokens[0]);
			return hashPartitioner.getPartition(newKey, value, numPartitions);
		} catch (Exception e) {
			e.printStackTrace();
			//on exception return random partition
			return (int) (Math.random() * numPartitions); 
		}
	}
}

package edu.sdu.bigdata.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public final class SortIntegers {
    private SortIntegers() {
    }

    public static class NumberMapper extends Mapper<LongWritable, Text, IntWritable, NullWritable> {
        private final IntWritable number = new IntWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String text = value.toString().trim();
            if (!text.isEmpty()) {
                number.set(Integer.parseInt(text));
                context.write(number, NullWritable.get());
            }
        }
    }

    public static class RankReducer
            extends Reducer<IntWritable, NullWritable, IntWritable, IntWritable> {
        private int rank = 0;
        private final IntWritable rankOut = new IntWritable();

        @Override
        protected void reduce(IntWritable key, Iterable<NullWritable> values, Context context)
                throws IOException, InterruptedException {
            for (NullWritable ignored : values) {
                rank++;
                rankOut.set(rank);
                context.write(rankOut, key);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: SortIntegers <input> <output>");
            System.exit(2);
        }
        Job job = Job.getInstance(new Configuration(), "lab1-sort-integers");
        job.setJarByClass(SortIntegers.class);
        job.setMapperClass(NumberMapper.class);
        job.setReducerClass(RankReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}


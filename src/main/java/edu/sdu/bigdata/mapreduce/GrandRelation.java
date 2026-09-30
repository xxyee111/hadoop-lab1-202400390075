package edu.sdu.bigdata.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GrandRelation {
    private GrandRelation() {
    }

    public static class RelationMapper extends Mapper<LongWritable, Text, Text, Text> {
        private final Text person = new Text();
        private final Text tagged = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty()) {
                return;
            }
            String[] parts = line.split("\\s+");
            if (parts.length < 2 || ("child".equalsIgnoreCase(parts[0])
                    && "parent".equalsIgnoreCase(parts[1]))) {
                return;
            }
            String child = parts[0];
            String parent = parts[1];

            person.set(parent);
            tagged.set("C:" + child);
            context.write(person, tagged);

            person.set(child);
            tagged.set("P:" + parent);
            context.write(person, tagged);
        }
    }

    public static class JoinReducer extends Reducer<Text, Text, Text, Text> {
        private final Text grandchildOut = new Text();
        private final Text grandparentOut = new Text();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            context.write(new Text("grandchild"), new Text("grandparent"));
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            List<String> children = new ArrayList<>();
            List<String> parents = new ArrayList<>();
            for (Text value : values) {
                String tagged = value.toString();
                if (tagged.startsWith("C:")) {
                    children.add(tagged.substring(2));
                } else if (tagged.startsWith("P:")) {
                    parents.add(tagged.substring(2));
                }
            }
            for (String child : children) {
                for (String parent : parents) {
                    grandchildOut.set(child);
                    grandparentOut.set(parent);
                    context.write(grandchildOut, grandparentOut);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: GrandRelation <input> <output>");
            System.exit(2);
        }
        Job job = Job.getInstance(new Configuration(), "lab1-grand-relation");
        job.setJarByClass(GrandRelation.class);
        job.setMapperClass(RelationMapper.class);
        job.setReducerClass(JoinReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

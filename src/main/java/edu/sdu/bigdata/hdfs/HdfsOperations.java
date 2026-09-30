package edu.sdu.bigdata.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class HdfsOperations {
    private HdfsOperations() {
    }

    public static void upload(FileSystem fs, String localFile, Path destination, String mode)
            throws IOException {
        Path parent = destination.getParent();
        if (parent != null) {
            fs.mkdirs(parent);
        }
        if (fs.exists(destination) && "append".equalsIgnoreCase(mode)) {
            try (FSDataOutputStream out = fs.append(destination);
                 java.io.FileInputStream in = new java.io.FileInputStream(localFile)) {
                IOUtils.copyBytes(in, out, 8192, false);
            }
        } else {
            fs.copyFromLocalFile(false, true, new Path(new File(localFile).toURI()), destination);
        }
        System.out.println("UPLOAD mode=" + mode + " path=" + destination + " length="
                + fs.getFileStatus(destination).getLen());
    }

    public static File downloadWithAutoRename(FileSystem fs, Path source, File localDirectory,
                                               Configuration conf) throws IOException {
        if (!localDirectory.exists() && !localDirectory.mkdirs()) {
            throw new IOException("Cannot create local directory: " + localDirectory);
        }
        String name = source.getName();
        File target = new File(localDirectory, name);
        int index = 1;
        while (target.exists()) {
            int dot = name.lastIndexOf('.');
            String stem = dot >= 0 ? name.substring(0, dot) : name;
            String ext = dot >= 0 ? name.substring(dot) : "";
            target = new File(localDirectory, stem + "_" + index + ext);
            index++;
        }
        FileSystem localFs = FileSystem.getLocal(conf);
        org.apache.hadoop.fs.FileUtil.copy(fs, source, localFs,
                new Path(target.toURI()), false, true, conf);
        System.out.println("DOWNLOAD source=" + source + " local=" + target.getAbsolutePath());
        return target;
    }

    public static void cat(FileSystem fs, Path file) throws IOException {
        System.out.println("CAT path=" + file);
        try (FSDataInputStream in = fs.open(file)) {
            IOUtils.copyBytes(in, System.out, 8192, false);
        }
        System.out.println();
    }

    private static String statusLine(FileStatus status) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(status.getModificationTime()));
        return String.format("permission=%s length=%d modified=%s path=%s",
                status.getPermission(), status.getLen(), time, status.getPath());
    }

    public static void stat(FileSystem fs, Path file) throws IOException {
        System.out.println("STAT " + statusLine(fs.getFileStatus(file)));
    }

    public static void listRecursive(FileSystem fs, Path directory) throws IOException {
        System.out.println("LIST_RECURSIVE root=" + directory);
        RemoteIterator<LocatedFileStatus> files = fs.listFiles(directory, true);
        while (files.hasNext()) {
            System.out.println(statusLine(files.next()));
        }
    }

    public static void createFile(FileSystem fs, Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            fs.mkdirs(parent);
        }
        try (FSDataOutputStream out = fs.create(file, true)) {
            out.writeUTF("created by HdfsOperations");
        }
        System.out.println("CREATE_FILE path=" + file);
    }

    public static void deleteFile(FileSystem fs, Path file) throws IOException {
        boolean deleted = fs.delete(file, false);
        System.out.println("DELETE_FILE path=" + file + " deleted=" + deleted);
    }

    public static void createDirectory(FileSystem fs, Path directory) throws IOException {
        boolean created = fs.mkdirs(directory);
        System.out.println("CREATE_DIR path=" + directory + " result=" + created);
    }

    public static boolean deleteDirectoryIfEmpty(FileSystem fs, Path directory) throws IOException {
        if (!fs.exists(directory)) {
            System.out.println("DELETE_DIR path=" + directory + " result=false reason=missing");
            return false;
        }
        if (fs.listStatus(directory).length != 0) {
            System.out.println("DELETE_DIR path=" + directory + " result=false reason=not-empty");
            return false;
        }
        boolean deleted = fs.delete(directory, false);
        System.out.println("DELETE_DIR path=" + directory + " result=" + deleted);
        return deleted;
    }

    public static void appendContent(FileSystem fs, Path file, String content, String position)
            throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if ("tail".equalsIgnoreCase(position)) {
            try (FSDataOutputStream out = fs.append(file)) {
                out.write(bytes);
            }
        } else if ("head".equalsIgnoreCase(position)) {
            Path backup = new Path(file.toString() + ".backup");
            fs.delete(backup, false);
            if (!fs.rename(file, backup)) {
                throw new IOException("Cannot rename source before prepend: " + file);
            }
            boolean completed = false;
            try (FSDataOutputStream out = fs.create(file, false);
                 FSDataInputStream in = fs.open(backup)) {
                out.write(bytes);
                IOUtils.copyBytes(in, out, 8192, false);
                completed = true;
            } finally {
                if (completed) {
                    fs.delete(backup, false);
                } else {
                    fs.delete(file, false);
                    fs.rename(backup, file);
                }
            }
        } else {
            throw new IllegalArgumentException("position must be head or tail");
        }
        System.out.println("APPEND position=" + position + " path=" + file + " length="
                + fs.getFileStatus(file).getLen());
    }

    public static void move(FileSystem fs, Path source, Path destination) throws IOException {
        Path parent = destination.getParent();
        if (parent != null) {
            fs.mkdirs(parent);
        }
        if (!fs.rename(source, destination)) {
            throw new IOException("Cannot move " + source + " to " + destination);
        }
        System.out.println("MOVE source=" + source + " destination=" + destination);
    }

    private static void runDemo(Configuration conf, FileSystem fs, String localInput,
                                File downloadDirectory, Path root) throws Exception {
        fs.delete(root, true);
        fs.mkdirs(root);

        Path mainFile = new Path(root, "input/demo.txt");
        upload(fs, localInput, mainFile, "overwrite");
        upload(fs, localInput, mainFile, "append");

        if (!downloadDirectory.exists() && !downloadDirectory.mkdirs()) {
            throw new IOException("Cannot create download directory");
        }
        File collision = new File(downloadDirectory, mainFile.getName());
        try (OutputStream out = new FileOutputStream(collision)) {
            out.write("existing local file".getBytes(StandardCharsets.UTF_8));
        }
        downloadWithAutoRename(fs, mainFile, downloadDirectory, conf);

        cat(fs, mainFile);
        stat(fs, mainFile);
        listRecursive(fs, root);

        Path createdFile = new Path(root, "created/deep/empty.txt");
        createFile(fs, createdFile);
        deleteFile(fs, createdFile);

        Path emptyDirectory = new Path(root, "empty/a/b");
        createDirectory(fs, emptyDirectory);
        deleteDirectoryIfEmpty(fs, emptyDirectory);
        deleteDirectoryIfEmpty(fs, new Path(root, "input"));

        appendContent(fs, mainFile, "TAIL-LINE\n", "tail");
        appendContent(fs, mainFile, "HEAD-LINE\n", "head");

        Path deleteTarget = new Path(root, "delete-target.txt");
        createFile(fs, deleteTarget);
        deleteFile(fs, deleteTarget);

        Path moveSource = new Path(root, "move/source.txt");
        upload(fs, localInput, moveSource, "overwrite");
        move(fs, moveSource, new Path(root, "move/destination.txt"));

        cat(fs, mainFile);
        listRecursive(fs, root);
        System.out.println("ALL_HDFS_API_CASES_PASSED root=" + root);
    }

    private static void usage() {
        System.out.println("Usage: HdfsOperations demo <localInput> <downloadDir> <hdfsRoot>");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4 || !"demo".equalsIgnoreCase(args[0])) {
            usage();
            System.exit(2);
        }
        Configuration conf = new Configuration();
        try (FileSystem fs = FileSystem.get(conf)) {
            runDemo(conf, fs, args[1], new File(args[2]), new Path(args[3]));
        }
    }
}

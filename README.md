# Hadoop 大数据系统基本实验

本仓库对应“大数据管理与分析”实验一，包含 HDFS Java API 与 MapReduce 三项编程任务。代码以 Java 8 和 Hadoop 3.2.2 为目标环境。

## 目录

- `src/main/java/edu/sdu/bigdata/hdfs/HdfsOperations.java`：上传、下载、查看、递归列表、创建、删除、追加和移动等 HDFS API。
- `src/main/java/edu/sdu/bigdata/mapreduce/MergeDedup.java`：合并两个输入文件并去重。
- `src/main/java/edu/sdu/bigdata/mapreduce/SortIntegers.java`：对多文件整数进行全局升序排序并生成位次。
- `src/main/java/edu/sdu/bigdata/mapreduce/GrandRelation.java`：根据 child-parent 关系计算 grandchild-grandparent 关系。
- `data/`：三项 MapReduce 任务和 HDFS API 演示使用的输入数据。
- `scripts/linux-commands-ubuntu.sh`：在 Ubuntu 环境中执行实验要求的 17 组 Linux 命令。
- `.github/workflows/linux-command-verification.yml`：使用 GitHub Actions 的 Ubuntu 24.04 运行器保留 Linux 命令执行记录。

## 编译与运行

在 Hadoop 已启动并且 `hadoop` 命令可用时，执行：

```bash
mkdir -p target/classes
javac -encoding UTF-8 -classpath "$(hadoop classpath)" -d target/classes $(find src/main/java -name '*.java')
jar -cf target/hadoop-lab1.jar -C target/classes .
```

HDFS API 演示：

```bash
hadoop jar target/hadoop-lab1.jar edu.sdu.bigdata.hdfs.HdfsOperations \
  demo data/hdfs-input.txt downloads /user/hadoop/lab1-api
```

三项 MapReduce 程序：

```bash
hadoop jar target/hadoop-lab1.jar edu.sdu.bigdata.mapreduce.MergeDedup /lab1/dedup/input /lab1/dedup/output
hadoop jar target/hadoop-lab1.jar edu.sdu.bigdata.mapreduce.SortIntegers /lab1/sort/input /lab1/sort/output
hadoop jar target/hadoop-lab1.jar edu.sdu.bigdata.mapreduce.GrandRelation /lab1/relation/input /lab1/relation/output
```

实验报告中的运行结果来自公开仓库对应代码的实际执行记录。

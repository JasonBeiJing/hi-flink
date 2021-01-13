package com.beijing.flink.batch;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.util.Collector;
import org.apache.flink.util.Preconditions;

import com.beijing.flink.batch.datasource.WordCountData;

public class WordCountBatch {
    public static void main(String[] args) throws Exception {
		// 构建运行时环境
        final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);
        final ExecutionEnvironment env = getEnv(params);

        // 获取批处理数据源 - DataSet
        DataSet dataSource = getDatasource(env, params);

        // 算子运算
		DataSet<Tuple2<String, Integer>> counts = dataSource.flatMap(new Tokenizer()).groupBy(0).sum(1);

		// 输出结果
        if (params.has("output")) {
            counts.writeAsCsv(params.get("output"), "\n", " ");
            env.execute("WordCount Example");
        } else {
            counts.print();
        }
    }

    // 批处理执行环境 - ExecutionEnvironment
    private static ExecutionEnvironment getEnv(MultipleParameterTool params) {
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		env.getConfig().setGlobalJobParameters(params);
		return env;
	}

    private static DataSet<String> getDatasource(ExecutionEnvironment env, MultipleParameterTool params) {
		// get input data
		DataSet<String> dataSource = null;
		if (params.has("input")) {
			for (String input : params.getMultiParameterRequired("input")) {
				DataSource<String> source = env.readTextFile(input);
				if (dataSource == null) {
					dataSource = source;
				} else {
					dataSource = dataSource.union(source);
				}
			}
			Preconditions.checkNotNull(dataSource, "Input DataSet should not be null.");
		} else {
			dataSource = env.fromElements(WordCountData.WORDS);
		}
		return dataSource;
	}

    static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        /**
         *
         */
        private static final long serialVersionUID = 5079589968599423718L;

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // normalize and split the line
            System.err.println("有序==>" + value);
            String[] tokens = value.toLowerCase().split("\\W+");
            // emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }
}

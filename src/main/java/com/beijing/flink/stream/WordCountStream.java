package com.beijing.flink.stream;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.util.Preconditions;

import com.beijing.flink.stream.datasource.WordCountData;


public class WordCountStream {

	public static void main(String[] args) throws Exception {
		// 构建运行时环境
		final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);
		final StreamExecutionEnvironment env = getEnv(params);

		// 获取流处理数据源 - DataStream
		DataStream<String> text = getDatasource(env, params);

		// 算子运算
		DataStream<Tuple2<String, Integer>> counts = text.flatMap(new Tokenizer()).keyBy(value -> value.f0).sum(1);

		// 输出结果
		if (params.has("output")) {
			counts.writeAsText(params.get("output"));
		} else {
			counts.print();
		}
		env.execute("Streaming WordCount");
	}

	// 流处理执行环境 - StreamExecutionEnvironment
	private static StreamExecutionEnvironment getEnv(MultipleParameterTool params) {
		final StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
		environment.getConfig().setGlobalJobParameters(params);
		return environment;
	}

	private static DataStream<String> getDatasource(StreamExecutionEnvironment env, MultipleParameterTool params) {
		DataStream<String> dataSource = null;
		if (params.has("input")) {
			for (String input : params.getMultiParameterRequired("input")) {
				if (dataSource == null) {
					dataSource = env.readTextFile(input);
				} else {
					dataSource = dataSource.union(env.readTextFile(input));
				}
			}
			Preconditions.checkNotNull(dataSource, "Input DataStream should not be null.");
		} else {
			dataSource = env.fromElements(WordCountData.WORDS);
		}
		return dataSource;
	}

	public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5580735946454213546L;

		@Override
		public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
			// normalize and split the line
			System.err.println("无序==>" + value);
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
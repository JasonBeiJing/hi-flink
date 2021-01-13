package com.beijing.flink;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HiJava11 {

	public static void main(String[] args) {
		var a = 66L;
		System.out.println(a);

		List<User> users = List.of(
				new User(1L, "zhangsan", 18),
				new User(1L, "lisi", 19),
				new User(1L, "wangwu", 20)
		);
		String names = users.parallelStream()
				.map(User::getName)
				.distinct()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.joining(",", "[", "]"));
		System.err.println("names: -> " + names);
	}

	static class User implements Serializable {
		private Long id;
		private String name;
		private int age;

		public User(Long id, String name, int age) {
			this.id = id;
			this.name = name;
			this.age = age;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}

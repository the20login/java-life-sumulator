package com.company.life_simulator.world;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//http://stackoverflow.com/questions/35742640/calling-sequential-on-parallel-stream-makes-all-previous-operations-sequential
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-XX:+AggressiveOpts"})
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class ParallelComputationBenchmark
{
	private List<Integer> data;
	private ForkJoinPool forkJoinPool;

	@Setup
	public void init()
	{
		data = new Random().ints(10000).boxed().collect(Collectors.toList());
		forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	}

	@Benchmark
	public Set<String> testParallelStreamWithCopy() throws ExecutionException, InterruptedException
	{
		return forkJoinPool.submit(()-> {
			return data.parallelStream()
					.map(this::slowOperation)
					.collect(Collectors.toList())
					.stream()
					.map(this::fastOperation)//some fast operation, but must be in single thread
					.collect(Collectors.toSet());
		}).get();
	}

	@Benchmark
	public Set<String> testParallelStreamWithSynchronized() throws ExecutionException, InterruptedException
	{
		return forkJoinPool.submit(()-> data.parallelStream()
				.map(this::slowOperation)
				.map(this::synchronizedFastOperation)//some fast operation, but must be in single thread
				.collect(Collectors.toSet())
		).get();
	}

	private String slowOperation(int value)
	{
		return Thread.currentThread().getName();
	}

	private synchronized String synchronizedFastOperation(String value)
	{
		return value;
	}

	private String fastOperation(String value)
	{
		return value;
	}
}

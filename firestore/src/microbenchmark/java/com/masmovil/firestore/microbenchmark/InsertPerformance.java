package com.masmovil.firestore.microbenchmark;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

public class InsertPerformance {

	@State(Scope.Thread)
	public static class ExecutionPlan {

		//TODO: replace CREDENTIALS_PATH with your credentials.
		private final static String CREDENTIALS_PATH = "/Users/pjgg/Desktop/keyfile.json";

		VehicleRepository repository;
		Vehicle vehicle;

		@Param({ "1"})
		public int iterations;

		@Setup(Level.Invocation)
		public void setUp() {
			repository = new VehicleRepository(CREDENTIALS_PATH, 4);
			vehicle = new Vehicle("Toyota", "Auris", true);
		}

		@Fork(value = 1, warmups = 1)
		@Benchmark
		@BenchmarkMode(Mode.Throughput)
		@OutputTimeUnit(TimeUnit.SECONDS)
		@Warmup(iterations = 1)
		@Threads(1)
		public void insertSingleItemTest(ExecutionPlan plan) {
			for (int i = plan.iterations; i > 0; i--) {
				plan.repository.insert(vehicle).subscribe(id -> {
					System.out.println(id);
				});
				System.out.println("Dont stop!!!. " + i);

			}
		}

/*
		@Ignore
		@Fork(value = 1, warmups = 1)
		@Benchmark
		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.MILLISECONDS)
		@Warmup(iterations = 5)
		public void insertTenItemsTest(ExecutionPlan plan) {
			plan.iterations = 10;
			for (int i = plan.iterations; i > 0; i--) {
				plan.repository.insert(vehicle);
			}
		}*/

	}
}

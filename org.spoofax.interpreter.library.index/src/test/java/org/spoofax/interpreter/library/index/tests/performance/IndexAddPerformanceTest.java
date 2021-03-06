package org.spoofax.interpreter.library.index.tests.performance;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Clock;

@BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 3, callgc = true, clock = Clock.CPU_TIME)
@RunWith(value = Parameterized.class)
public class IndexAddPerformanceTest extends IndexPerformanceTest {
    @Rule
    public BenchmarkRule benchmarkRun;

    public IndexAddPerformanceTest(int numItems, int numFiles) {
        super(numItems, numFiles);

        try {
            benchmarkRun =
                new BenchmarkRule(new CSVResultsConsumer((this.numItems * 5) + "," + this.numFiles, new FileWriter(
                    "add_" + this.numFiles + ".csv", true)));
        } catch(IOException e) {
            e.printStackTrace();
        }

        index.reset();
    }

    @Test
    public void add() {
        for(int i = 0; i < numItems; ++i) {
			add(def1, getNextFile());
			add(def2, getNextFile());
			add(def3, getNextFile());
			add(use1, getNextFile());
			add(type1, getNextFile());
        }
    }
}

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode({Mode.Throughput, Mode.SampleTime}) // Measures ops/second and sampling time
@OutputTimeUnit(TimeUnit.MILLISECONDS)            // Results in milliseconds
@State(Scope.Thread)                               // Each thread gets its own state
public class Block2Poly {

    private String input;

    @Setup(Level.Iteration)
    public void setup() {
        input = "Example input string";
    }

    @Benchmark
    public String benchmarkStringConcatenation() {
        return input + " concatenated";
    }

    @Benchmark
    public String benchmarkStringBuilder() {
        return new StringBuilder(input).append(" concatenated").toString();
    }
}
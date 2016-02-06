package com.emorozov.datasciencelab;

import com.emorozov.datasciencelab.sampler.CpuSampler;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.lessThan;

public class DataScienceLabDemoTestPerformanceAnomaliesSteps {

    @Data
    public class Endpoint {
        private String domain;
        private int port;
        private String path;
        private String method;
    }

    @Data
    @AllArgsConstructor
    private static class RInput {
        private String metrics;
        private Integer samplerCount;
        private Double criticalQiantile;
    }

    @Data
    private static class ROutput {
        private Integer outliers;
        @JsonCreator
        public ROutput(@JsonProperty("outliers") Integer outliers) {
            this.outliers = outliers;
        }
    }

    private int samplersCount = 0;

    @Before
    public void clearTemporaryFiles() throws Exception {
        Files.deleteIfExists(Paths.get("metrics.csv"));
        Files.deleteIfExists(Paths.get("input.json"));
        Files.deleteIfExists(Paths.get("output.json"));
    }

    @Given("^endpoints are up and running$")
    public void systemEndpointsAreUpAndRunning(final List<Endpoint> endpoints) throws Exception {
        // TODO Actually validate that endpoints are up and running
    }

    @When("^(.+) requests are submitted for endpoints$")
    public void requestsAreSubmitted(final Integer loops,
                                     final List<Endpoint> endpoints) {

        // Set status vars - number of endpoints and a CPU sampler
        this.samplersCount = endpoints.size() + 1;

        // Create JMeter engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        // Init JMeter engine
        // TODO Refactor config
        JMeterUtils.setJMeterHome("JMETER_HOME");
        JMeterUtils.loadJMeterProperties("JMETER_HOME/bin/jmeter.properties");
        JMeterUtils.initLocale();

        // Build samplers, use path for name of the sampler
        Stream<HTTPSampler> httpSamplers = endpoints.stream().map(endpoint -> {
            HTTPSampler httpSampler = new HTTPSampler();
            httpSampler.setName(endpoint.getPath());
            httpSampler.setDomain(endpoint.getDomain());
            httpSampler.setPort(endpoint.getPort());
            httpSampler.setPath(endpoint.getPath());
            httpSampler.setMethod(endpoint.getMethod());
            return httpSampler;
        });

        // Build a CPU sampler
        CpuSampler cpuSampler = new CpuSampler();
        cpuSampler.setName("cpu");

        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setName("Default Loop Controller");
        loopController.setLoops(loops);
        loopController.setFirst(true);
        loopController.initialize();

        // Thread Group
        org.apache.jmeter.threads.ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Default Thread Group");
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopController);

        // Create results collector
        ResultCollector logger = new ResultCollector();
        logger.setName("Default Results Collector");
        logger.setFilename("metrics.csv");

        // Create a test structure
        HashTree testPlanTree = new HashTree();

        // Add Test Plan
        TestPlan testPlan = new TestPlan("Sample R integration test plan");
        testPlanTree.add(testPlan);

        // Hang off Thread Group off Test Plan
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);

        // Add samplers
        threadGroupHashTree.add(httpSamplers.toArray());
        threadGroupHashTree.add(cpuSampler);

        // Add logger
        testPlanTree.add(testPlan, logger);

        // Run Test Plan
        jmeter.configure(testPlanTree);
        jmeter.run();
        threadGroup.waitThreadsStopped();
    }

    @Then("^metrics outliers are below (.+) percent with critical quantile (.+)$")
    public void outliersAcrossInputAreBelowThreshold(final Integer outliersThreshold,
                                                     final Double criticalQuantile)
            throws Exception {

        // Get the object mapper
        ObjectMapper mapper = new ObjectMapper();

        // Create inputs for analytics
        RInput input = new RInput("metrics.csv", this.samplersCount, criticalQuantile);

        mapper.writeValue(new File("input.json"), input);

        // Run analytics
        // TODO Refactor config
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("Rscript ./src/test/r/performance-anomalies.R");
        pr.waitFor();

        // Get the outputs
        ROutput output = mapper.readValue(new File("output.json"), ROutput.class);

        // Assert
        assertThat(output.getOutliers(), lessThan(outliersThreshold));
    }
}

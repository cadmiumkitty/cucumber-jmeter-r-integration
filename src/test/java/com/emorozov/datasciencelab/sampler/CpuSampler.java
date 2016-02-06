package com.emorozov.datasciencelab.sampler;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class CpuSampler extends AbstractSampler {

    private static final double SCALING_FACTOR = 100;

    public OperatingSystemMXBean osBean;

    public CpuSampler() {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public SampleResult sample(Entry entry) {
        long timestamp = System.currentTimeMillis();
        long systemLoadAverage = (long) (osBean.getSystemLoadAverage() * SCALING_FACTOR);
        SampleResult result = SampleResult.createTestSample(
                timestamp,
                timestamp + systemLoadAverage);
        result.setSuccessful(true);
        result.setDataType("cpu");
        return result;
    }
}

# Anomaly Detection in Performance Test Data

## Overview

Example of Spring Boot, Cucumber, JMeter and R integration that demonstrates
anomaly detection in performance test data.

This demo was shown at DataScienceLab Data Science in FinTech meetup
(http://www.meetup.com/data-science-lab/events/227113953).

## Configuration

Configuration is hardcoded to demo the integration, update JMeter and Rscript
paths in `src/test/java/com/emorozov/datasciencelab/DataScienceLabDemoTestPerformanceAnomaliesSteps.java`
before you start.

## R Integration

Once R is installed, install the packages manually prior to running Cucumeber files.
This is a workaround for package installation failing from Java Runtime.exec().

```
install.packages('dplyr')
install.packages('tidyr')
install.packages('mvoutlier')
install.packages('jsonlite')
```

## R Specifics

Since sample is very small (only 50 runs taking about 10 seconds), CPU metrics
tend to introduce significant number of outliers.

In the current implementation cpu metrics are excluded in the
`performance-anomalies.R` script.

## Todo

 1. Create more generic Cucumber step definitions, so that mvoutlier
    parameters can be passed into the script from feature files.
 1. Make sure that dependencies are checked in R script and installed
    automatically.
 1. Refactor config, so paths are not hardcoded.
library(dplyr)
library(tidyr)
library(mvoutlier)
library(jsonlite);

# Get the input
input <- fromJSON('input.json');

# Get actual data
metrics.csv.file <- as.character(input["metrics"])
metrics.csv <- read.csv2(file = metrics.csv.file, sep = ",", header = T)

# Clean up - add sample var based on the number of samplers writing to the log
metrics.csv$sample <- rep(1:(nrow(metrics.csv)/as.numeric(input["samplerCount"])), 
                          each = as.numeric(input["samplerCount"]))

# Mutate to wide form
metrics.csv.tall <- metrics.csv %>%
    mutate(metric = paste(dataType, label, sep = "")) %>%
    select(metric, sample, elapsed)

# Remove sample vars
metrics.csv.wide <- spread(metrics.csv.tall, metric, elapsed)
metrics.csv.wide.clean <- metrics.csv.wide %>%
    select(-sample, -cpu)

# Detect multivariate outliers
outliers <- sign2(metrics.csv.wide.clean, qcrit = as.numeric(input["criticalQiantile"]), makeplot = T)

# Create and write out output
output <- list(outliers = sum(!outliers$wfinal01))
output.json <- toJSON(output, auto_unbox = T, pretty = T)
write(output.json, "output.json")
Feature: System performs with acceptable level of anomalies

  Scenario: Requests are submitted to all endpoints to detect anomalies

    Given endpoints are up and running
      | domain    | port | path   | method |
      | localhost | 8080 | users  | GET    |
      | localhost | 8080 | trades | GET    |

    When 50 requests are submitted for endpoints
      | domain    | port | path   | method |
      | localhost | 8080 | users  | GET    |
      | localhost | 8080 | trades | GET    |

    Then metrics outliers are below 5 percent with critical quantile 0.995
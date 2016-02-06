Feature: Data Science in FinTech Meetup demo is working

  Scenario: Requests to /users endpoint succeed

    Given Endpoint /users is up

    When I issue GET request

    Then I get Success response

  Scenario: Requests to /trades endpoint succeed

    Given Endpoint /trades is up

    When I issue GET request

    Then I get Success response

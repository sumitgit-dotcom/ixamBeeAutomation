Feature: ixamBee Home Page

  @smoke
  Scenario: Open ixamBee home page and verify title
    Given I open the ixamBee home page
    Then the page title should contain "ixambee"
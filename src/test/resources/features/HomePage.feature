Feature: ixamBee Home Page

  @smoke
  Scenario: Open ixamBee home page and verify title
    Given I open the ixamBee home page
    Then the page title should contain "ixambee"
    
    
     @dropdown
  Scenario: Hover on Online Course and print dropdown items
    Given I open the ixamBee home page
    When I hover on the "Online Course" dropdown
    Then I should see the Online Course dropdown items listed
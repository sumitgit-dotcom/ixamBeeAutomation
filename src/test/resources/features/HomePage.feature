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
    
      @search
  Scenario: search
    Given I open the ixamBee home page
    When I click the header search bar
    And I type "rbi grade b" in the search field
    And I click the "Exam" chip
    Then the search results should be filtered by Exam
    
    
    @demo
  Scenario: demo
    Given I open the ixamBee home page
    When I scroll and click the "IBPS RRB Scale II - IT Officer Online..." card
    And I close the popup on the course page
    And I scroll and click "Get Free Demo"
    And I enter phone number "8871220199" in the demo form
    And I click the "Send OTP" button
    Then the OTP request should be submitted
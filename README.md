Conferencer
===========

Vision statement
----------------

For 'volunteer organizers',
who want to 'organize technical events'
'conferencer'
is a 'collaboration tool'
which lets them 'track the status of the event program'.

Unlike 'Excel' or 'Google Spreadsheets', this solution
allows easy updating and sharing of the status of the program.

Unlike call-for-paper applications, this solution
helps organizers who actively invite speakers instead of relying on call-for-papers.


Key stakeholders
----------------

10-20 volunteer program committee members per event who checks and updates the status of their talks a few times per week.

3-5 event organizers who coordinate the program committee members.

Up to a hundred speakers who submit, review and approve their talks. They often don't want to interact with the system.

Several hundred conference attendees who want to see the program before and during the conference.


Usage flow
----------
1. Event organizers define the event and adds other organizers and committee members
   * All users sign up using OAuth providers such as Linkedin, Google or Facebook
2. Event organizers and committee members define event topics
3. Program committee members add talk suggestions with the correct topics
4. The system can email prospective speakers and deal with responses (nice to have)
5. Program committee members update the status of a talk
6. Event organizers view the status of the whole program per topic
7. Event organizers plan the program of the event using the topics
8. Event organizers publizise the program

Domain model
------------

@startuml
Event "*" - "*" User: organizes
Event -- "*" Topic
Event -- "*" Talk
Topic "*" - "*" Talk: < tagged with
Talk -- "*" TalkUpdate
TalkUpdate "*" -- User
User - "*" Identity: authenticates with
@enduml

 
Demo script
-----------

1. Log in with a new gmail account
  * I will see an empty list of my events
2. Create a new event
  * The event will be added to my list of events
3. Add other organizers to the event
  * The event will contain the organizers
  * Organizers can log in to see the events
4. Add topics to the event
5. Add talks to the event
6. Update status and add comments to the talks
7. View the status per topic

 

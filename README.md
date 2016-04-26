# lastobot

Playground for telegram bot

-------------------
Measures bot
-------------------

=== Use Case 1

Mark wants to track his weight. He tells bot that he wants to track his weight
/track weight
Bot sends a keyboard to choose type of counter - [singular, double, category]
Optional: choose unit name TODO subsystem of unit translation
User chooses and bot replies that now he is tracking now counter

=== Use case 2

User notifies bot that about measure
TODO let user to do it in past or amend recent measure
/notch => keyboard of measures => user chooses => enter value => user enters => ok
/notch <substring> => Bot asks for value (or cancel) => ok
Bot saves measure to database

=== Use case 3

User want to see his stats
/stats
Bot sends three graphs - per day, per week and per month


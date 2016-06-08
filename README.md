# lastobot

Playground for telegram bot

Project Trello: [https://trello.com/b/tthaiPG8/lastobot]

-------------------
lastobot commands
-------------------
smoke - Ask bot to remember that you smoked xx cigarettes recently
smokestats - Ask bot to print statistics for your smoking habit
setlocale - Change language you want robot talk to

-------------------
Common chat
-------------------

user -> smoke => bot ::= SmokingValueRetrival
bot -> "Enter number"
user -> XX
	bot -> You smoked XX cigarettes! & saved
user -> _ : not number 
	bot -> Not sure what you smoked

user -> smoke 4
bot -> You smoked 4 cigarettes! & saved

user -> delete
bot -> Deleted last info that you smoked 4 cigarettes!

-------------------
Generic measurement
-------------------

Main idea of lastobot - you are able to measure any phenomena in your life if you know how to measure it, of course. Bot will keep all the records for you.

But how to set up tracking for most rare and custom thing in you life? The process should be the following:
1. Name of the measure - you can be creative if you want to measure something restricted or you are afraid your mom checks your phone.
2. Type - it could be
	- value : some number (X cigarettes smoked) - we can model it as a function of time, see some interesting statistics
	- kind : some categoty you observe (I saw XXX, yesterday i saw YYY) - you can group events by category
3. Units - you define units for the measure (like alcohol)


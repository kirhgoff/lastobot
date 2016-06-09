# lastobot

======================================
Telegram bot to track your life
======================================
Project Trello: [https://trello.com/b/tthaiPG8/lastobot]

I am lastobot. I am talking to you my master. My whole idea tries to catch up the spirit of behaviorists therapy and also provides useful feature you could use in your day to day life. You can choose some way to measure any aspect of your life and use my services to track it. Value is the ability to track it, the internal work you do to keep yourself being conscious doing that. I propose you a game - to keep the information stream up and running, and will do my best to give you rewards for it. If you can measure it and affect the numbers - you are controlling it.

Currently I can track the count of cigarettes you smoke, but in future I will be measuring weight (engineers are already working on it) and final step in first phase will be measuring alcohol - to keep track of how much you drink.

-------------------
lastobot commands
-------------------
setlocale - Change language you want robot to talk
smoke - Ask bot to remember that you smoked xx cigarettes recently
smokestats - Ask bot to print statistics for your smoking habit
weight - Ask bot to measure you current weight
weightstats - Get weight measuring stats
bug - tell bot what looks like a problem with its behaviour
feature - ask bot to provide more services
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


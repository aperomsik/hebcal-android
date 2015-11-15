# hebcal-android
An Android app wrapped around hebcal.c .

This is a simple Android app which takes data from Hebcal, and shows it in a simple view combined with events 
from your calendars. Data can be viewed by month, week, or day-- and the day view include extra zemanim.

I have been using this app for about five years now, since I got my first Android phone, but the build process 
made sharing inconvenient. Now that the experimental Android Studio NDK plugin has advanced enough that it lets
me coax it into building this app, the time seems right to share.

After cloning:
```
git submodule init 
git submodule update
```
Then import the project into Android Studio and try to build it. Probably it will complain that you have
missing SDK versions and/or wrong tool versions, but it should offer to fix those issues for you one at a time.
When it's done downloading, the project might actually build.

TODO:
  - Try to convince upstream to accept my hebcal changes
  - Then ask if the hebcal team might want to take ownership of this app.

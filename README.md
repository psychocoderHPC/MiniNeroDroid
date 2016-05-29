# MiniNeroDroid

For suggestions / to help with development, join the new team board at: https://trello.com/minineroappsdevelopment 

A convenience-oriented Monero phone wallet for Android. 

This is currently (almost) fully functional and feature equivalent to the windows version -( I actually broke the transactions view when I updated the web app, so I need to go back and fix that soon).
If you want to use it, you can clone the repo and open it in Android Studio 1.5.1+ (I am currently using 2.0), just put your mininodo ip / key in the settings menu, or scan it from MiniNero Web using the qr scanner on the main page. (Requires MiniNodo.js to work: https://github.com/shennoether/mininodo ) Additionally, I may try to put it in the F-droid repo or google play store soon. 

####Features:####
* Bitcoin / Xmr uri and qr scanning support
* Send to xmr addresses directly or to bitcoin addresses via xmr.to
* Save / Load / Delete addresses in the small address book 
* Nacl Authentication between the app and your Monero Server
* Nacl Encryption support on top of https for added privacy
* Password support
* Toggle for Dark / Light theme
* View past transactions with links to various block-explorers


This app requires MiniNodo server running on the same box as your Monero installation: 
https://github.com/ShenNoether/MiniNodo

####Screenshots:####
http://imgur.com/a/h5FuV

This should eventually be feature equivalent to the existing windows universal app version:

https://github.com/ShenNoether/MiniNeroUniversal

iOs version is next!

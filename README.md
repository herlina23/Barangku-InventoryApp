# MyFridge

This is an app I made as the final project for the Udacity course [Android Basics: Data Storage](https://www.udacity.com/course/android-basics-data-storage--ud845).

## The course
The course taught the basics of database operations in Android using a SQLite database. From there, the instructors transitioned us to using Loaders and ContentResolvers instead of direct database operations, for better UI responsiveness and inter-operability between other apps.

The final assignment was to build an app that maintains and displays a list of inventory for an imaginary business.

## My app
I wanted to build an app that I could actually use myself; instead of a business inventory, this app maintains an inventory of the stuff that I have in the fridge/pantry. I have a bad habit of going shopping without checking beforehand what I have, so I end up with duplicates of a lot of stuff (especially spices - I have an absurd amount of cumin!).

![alt text](https://github.com/marshallaf/MyFridge/blob/master/screen1_w280.jpg "Main app screen")
![alt text](https://github.com/marshallaf/MyFridge/blob/master/screen2_w280.jpg "Editor activity")
![alt text](https://github.com/marshallaf/MyFridge/blob/master/screen3_w280.jpg "View activity")

It turned out pretty well for a first run. I'm especially pleased with the conversion functionality - the user can change the units displayed and the app converts to different units. In addition, when you mark some of the food as used, you can specify the units and the app does the correct calculation for you.

![alt text](https://github.com/marshallaf/MyFridge/blob/master/screen4_w280.jpg "Units converted")
![alt text](https://github.com/marshallaf/MyFridge/blob/master/screen5_w280.jpg "Using food")
![alt text](https://github.com/marshallaf/MyFridge/blob/master/screen6_w280.jpg "Food used")

Some of the concepts that I had to learn on my own for this were:
* Camera intents and stored files
* Enabling/disabling UI elements
* Figma

### Camera intents and stored files
I wanted to add the ability to take photos of the items. Using a camera intent was very easy, and I had never used a `onActivityResult` callback before, so that was valuable to learn. I also had to deal with loading the image in the correct orientation, which I did via a standard approach using the EXIF data and a `Matrix` object for the transform. However, I did this on the UI thread (I know, bad) so I'll need to fix this using a loader in a future commit so it's a little more peppy.

### Enabling/disabling UI elements
In the `MainActivity` there's an option to delete all the entries. I wanted to make one of those confirmation dialogs that make you type the word "delete" before allowing you to take such a drastic action. I implemented a `TextWatcher` to monitor the text box for changes and only enable the "delete" button if the user had typed the word delete.

I also toyed with disabling/enabling EditTexts in the EditorActivity, as originally I tried to have that activity serve both view and editing functions. This was a little more cumbersome than a button, but I was able to make it work. However, I scrapped it when I decided to polish the view UI a bit more and the dual purpose editor was no longer needed.

### Figma
I built the mocks for the FoodViewActivity in [Figma](https://www.figma.com/), an online mock-up creator that is awesome! I had never used a UI design app before and I was very pleased. It is so much easier and faster to try stuff out and build an outline in Figma than doing it in code in Android Studio initially.

## Future possibilities
* Use loader for images
* Add recipes
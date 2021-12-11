# Define4Me
A graphical Java application which finds the definitions to a list of words given to it, making vocabulary 
homework less tedious, among other things. It uses [meetDeveloper](https://github.com/meetDeveloper)'s 
[Free Dictionary API](https://dictionaryapi.dev/) for finding definitions.

### Prerequisites
* Having a [JDK](https://adoptium.net/) installed of at least version 17
* An internet connection
* A list of words with each word or phrase being in a new line. (Define4Me will automatically get rid of 
hyphens, bullet points, extra spaces, or other discrepancies around the words by itself)

## How to use
Once you have downloaded the .jar file of the version you would like to run from the 
[releases](https://github.com/soggy-sandwich/define4me/releases) page, you can double-click it like any
other application. (If it does not open make sure you have a proper JDK installed, refer to the 
[prerequisites](https://github.com/soggy-sandwich/define4me#prerequisites) section)

_On macOS, you may have to right-click the .jar file and click "open" to bypass the unknown developer
warning._

Upon opening the application, you will see a "paste" button on the left, which will paste the last thing
you copied into Define4Me, preferably being your list of words. Once pasted the words Define4Me finds will 
be added to the list in the center, where you can remove or add more through the buttons on the bottom.
After this you can click the "Define" button on the right, the time it takes to find definitions depends
on your internet connection, but it will soon populate a text area on-screen with the words and their 
definitions from where you can right-click to copy them.

If you would like to choose from the available definitions if a word contains multiple definitions, go to
the menu bar (top of screen on macOS, top of app anywhere else) and select File > Preferences and choose
"Ask If Multiple" for the definition preference.

## Building from source
_This is not officially supported, as there may be unwanted side effects from currently experimental features._

Download the source code and extract the folder inside, then open the
extracted folder with your Terminal/Command Prompt and running gradle's build command.

* **On Windows** cd into the folder and run ```gradlew build``` in Command Prompt.
* **On macOS** or other **Linux OS's** (you may need to run ```chmod +x gradlew``` before this
  works) cd into the folder and run ```./gradlew build``` in the terminal.

Once finished, the resulting files will be in the ```build``` folder. The .jar will be in ```build > libs``` and
gradle's default run scripts will be in ```build > bin```.

## License
Define4Me is licensed under the GNU General Public License v3.0, more information can
be found in the [LICENSE](https://github.com/soggy-sandwich/define4me/blob/master/LICENSE) file.

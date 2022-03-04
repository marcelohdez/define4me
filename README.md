# Define4Me
A graphical Java application which finds the definitions to a list of words given to it, making vocabulary 
homework less tedious, among other things. It uses [meetDeveloper](https://github.com/meetDeveloper)'s 
[Free Dictionary API](https://dictionaryapi.dev/) and can use Wikipedia for finding definitions.

### Prerequisites
* Having a [JDK](https://adoptium.net/) installed of at least version 17
* An internet connection
* A list of words with each word or phrase being in a new line. (Define4Me will automatically get rid of 
hyphens, bullet points, extra spaces, or other discrepancies around the words by itself)

## How to set up
Upon downloading a .jar file from the [releases](https://github.com/marcelohdez/define4me/releases) 
page, you can double-click it like any other application. If it does not open make sure you have a proper
JDK installed, see the [prerequisites](https://github.com/marcelohdez/define4me#prerequisites) section.

_On macOS, you may have to right-click the .jar file and click "open" to bypass the unknown developer
warning._


## How to use
After pasting text into Define4Me, every line will be made into a phrase to be defined and will be added
to the list on-screen. You may right-click any word on this list to edit it. After this, clicking "Define"
on the right will begin the process of searching for the given word(s)' definitions. A progressbar will
travel accross the bottom of the window indicating progress, once finished the "Definitions" tab will
populate with the definitions found, this text can be right-clicked to copy.

If you would like to choose a definition for a word with multiple definitions, go to the menu bar (top
of screen on macOS, top of app anywhere else) and select "Program" then "Preferences" and choose "Ask If
Multiple" for the definition preference.

## Building from source
_This is not officially supported, as there may be unwanted side effects from currently experimental features._

Download the source code and extract the folder inside, then open the
extracted folder with your Terminal/Command Prompt and running gradle's build command.

* **On Windows** cd into the folder and run ```gradlew build``` in Command Prompt or Terminal.
* **On macOS** or other **Linux OS's** cd into the folder and (you may need to run ```chmod +x gradlew```
before this works) run ```./gradlew build``` in the terminal.

Once finished, the resulting files will be in the ```build``` folder. The .jar will be in ```build > libs``` and
gradle's default run scripts will be in ```build > bin```.

## License
Define4Me is licensed under the GNU General Public License v3.0, more information can
be found in the [LICENSE](https://github.com/marcelohdez/define4me/blob/master/LICENSE) file.

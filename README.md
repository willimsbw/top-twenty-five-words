# Top 25 Words

Downloading this source code and converting to an executable .jar file will allow you to run the program TopTwentyFiveWords. This program will create (or connect to) a database in a local mySQL instance with tables "websites" and "words", and utilize those tables to store all of the text from a url you specify, along with each individual word from that text and the number of times it occurs. It will then tell you the 25 most frequently-used words from that url, and the number of times they appeared. You can use this program repeatedly to build out a database of what text is on what url, even overwriting old data for pages that were looked up more than 2 days ago.

## Getting Started

If you meet all of the prerequisites and are connected to the internet, clone or download this repo and convert it to an executable jar file. Then run that jar file from the command line.

##### *Note: I would recommend running from Terminal or Command Prompt, as my use of a Console object can cause issues for third-party command line tools like git Bash if they aren't configured properly.*

### Prerequisites

* For this program to run you will need to have [mySQL](https://dev.mysql.com/downloads/windows/installer/5.7.html) installed and a local instance that you can access. You will need to know its hostname and port number if they are not the defaults (localhost and 3306, respectively).

* You will also need to be able to [compile java projects into executable jar files](https://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygen3a).

* You will need to be connected to the internet when you run this program. If you're not, it will not be able to get text from any websites.

## Tests

There are no automated tests at this time for this program, but feel free to try to break it with weird or incorrect inputs. I have included in the "tests" folder age-a-record.sql. This is a simple sql command that will need to be edited so it uses the database name you've picked out and the url you've looked up already.

Once edited, it will back-date the create_date timestamp of a "website" record in your database with the url you provided to 2018-04-15 19:27:42, making it appear more than two days old. This will allow you to test how the program handles being fed a url that you have looked up more than two days ago without having to wait.

## Built With

* [Java 8](http://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html)
* the [Jsoup library](https://jsoup.org/download)
* [mySQL](https://www.mysql.com/) and their [connector/J tool](https://dev.mysql.com/downloads/connector/j/5.1.html)

## Contributing

If you find an issue or have a feature you think I should add, feel free to submit an issue or pull request!

## Authors

* **Bryan Williams** - *Initial work* - [willimsbw](https://github.com/willimsbw)

See also the list of [contributors](https://github.com/willimsbw/movie-website/graphs/contributors)
who participated in this project.

## Acknowledgments

* Thanks to Paul Bock for being generally great and sending me this challenge
* [PurpleBooth](https://gist.github.com/PurpleBooth/109311bb0361f32d87a2), for providing this really great readme template

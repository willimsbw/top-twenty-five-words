# Top 25 Words

Downloading this source code and converting to an executable jar will allow you to run the program TopTwentyFiveWords. This program will create (or connect to) a database in a local mySQL instance with tables "websites" and "words", and utilize those tables to store all of the text from a url you specify, along with each individual word from that text and the number of times it occurs. It will then tell you the 25 most frequently-used words from that url, and the number of times they appeared. You can use this program repeatedly to build out a database of what text is on what url, even overwriting old data for pages that were looked up more than 2 days ago.

## Getting Started

If you meet all of the prerequisites and are connected to the internet, clone or download this repo and convert it to an executable jar file. Then run that jar file from the command line.

##### *Note: I would recommend running from Terminal or Command Prompt, as my use of Console objects can cause issues for third-party command line tools like git Bash if they aren't configured properly.*

### Prerequisites

* For this program to run you will need to have [mySQL](https://dev.mysql.com/downloads/windows/installer/5.7.html) installed and a local instance that you can access.

* You will also need to be able to [compile java projects into executable jar files](https://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygen3a).

* You will need to be connected to the internet when you run this program. If you're not, it will not be able to get text from any websites.

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

# NitroHammer (In Progress)

[NitroHammer](http://www.tnevin.com)

Nitro-Hammer is an command line tool and a runtime jar file, which provide backend services by automatically creating rest service, the DAO layer, and the database beans, all auto-generated. Also, auto-generated are JUnit tests. The auto-generated sources are translated from a database definition file (.xml), using a JEXL template file (see Apache Commons JEXL). The XML files for describing the database, schema, tables, columns, and therefore beans, dao layer, rest services and unit test cases, simplifies creation, deletion and alterations of the
database schema, and corresponding application, and are referred to as the database model (.xml).

## Features

The NitroHanmmer command line tool has the following features:

* Creation of the database tables, triggers, index, foreign keys, etc, from .xml model files.
* Generate .xml model definitions from an existing database
* Generate SQL from .xml model definitions.
* Generate Java code from the .xml model definitions. Pre-defined templates for generating pojo beans, dao layer, rest services, and test cases is provided.
* Generate a test database based on the .xml model files.
* All java, portable to any Java supported OS.
* Supports JDBC, therefore MySql, H2, Oracle, PostgreSQL, also limited support for Mongo, HBase, and JPA.

## Additional Documentation

* [NitroHammer User Manual](http://www.tnevin.com/nitrohammer.html)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the software and how to install them

```
* java 1.6 or better.
* ant 1.9 or better (optional).
* For Windows, install CygWin, latest.
```

### Installing

1. Download the nitro-hammer zip file, and unzip it.

```
https://github.com/vipersoftwareservices/nitrohammer
```

2. Run the build script if building sources is desired, runtime jars are available.

```
ant clean all
```


## Running the tests

Run the tests, by running ant command.

```
ant test
```

View the JUnit test results, by bringing the following file up in browser.
For windows, double click the file in the disk explorer, the location of the file is:

```
<install-directory>/build/reports/index.html
```

View the code coverage file in the browser..
For windows, double click the file in the disk explorer, the location of the file is:

```
<install-directory>/build/jacoco/index.html
```

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Check on coding style by running:

```
ant checkstyle
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/vipersoftwareservices/nitrohammer) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Tom Nevin** - *Initial work* - [NitroHammer](https://github.com/vipersoftwareservices/nitrohammer)

See also the list of [contributors](https://github.com/vipersoftwareservices/nitrohammer/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc


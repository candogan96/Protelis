# Using Protelis in your projects

Theoretically, you should be able to do it just by including Protelis as a dependency from the Maven central repository.
Right now, however, that results in lots of broken dependencies.
Instead, set up as a developer (using the instructions below) and put a dependency between the two projects.

# Setting as a developer for Protelis

To work on Protelis as a developer, the Eclipse development
environment is strongly recommended, and these instructions will
assume Eclipse.

## Eclipse Prerequisites:
* Eclipse 4.4+ (Luna)
* Java 8+
* Gradle IDE
* git
* Checkstyle 5.9+

If you do not have all of these, install them from the Eclipse Marketplace

## Installation and Test
To install, import a new git project in Eclipse, cloning from:
  https://github.com/Protelis/Protelis.git

The project should automatically download all dependencies needed via
Gradle and build itself.

To test, run the project as JUnit tests.

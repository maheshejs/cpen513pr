# Routing

This project implements two versions of a Negotiated Congestion Algorithm:

- The first version implements the Lee-Moore Shortest Path Routing Algorithm
- The second version implements an A* algorithm

The program reads in placement and netlist information and outputs the routing, the number of cells visited (labelled) for each net, and an indication of whether the routing was successful.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 19 or above
- Gradle

### Running

```shell
git clone https://github.com/maheshejs/cpen513pr.git
cd cpen513pr
./gradlew run
```

## Source Code

The source code files for the project are located at :
```
  └── app
      └── src
          └── main
              └── java
                  └── ass1
```

## Graphics

The project includes benchmark graphics in the `graphics/` folder.

## Authors

* **Joseph Maheshe** - *Initial work*

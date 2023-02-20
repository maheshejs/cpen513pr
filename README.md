# Placement

This project implements a simulated-annealing based placement tool that targets standard cells.
The optimization goal is to minimize the half-perimeter of the smallest bounding box containing all blocks for
each connection, summed over all connections.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 19 or above
- Gradle

### Running

```shell
git clone https://github.com/maheshejs/cpen513pr.git
cd cpen513pr
./gradlew run --args="<benchmarkFile> <useRowSpacing>"
```

### Testing

```shell
./gradlew clean test
```

## Source Code

The source code files for the project are located at :
```
  └── app
      └── src
          └── main
              └── java
                  └── ass2
          └── test
              └── java
                  └── ass2
      └── data
          └── benchmarks
              └── ass2
```

## Authors

* **Joseph Maheshe** - *Initial work*

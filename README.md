# Top-down Partitioning-based Placement

This project implements a top-down partitioning-based placement algorithm.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 19 or above
- Gradle

### Running

```shell
git clone https://github.com/maheshejs/cpen513pr.git
cd cpen513pr
./gradlew run --args="<benchmarkFile> <useRowSpacing> <useFM> <recursionDepth>"
```

### Testing

```shell
./gradlew clean test --tests proj.AppTest
```

## Source Code

The source code files for the project are located at :
```
  └── app
      └── src
          └── main
              └── java
                  └── proj
          └── test
              └── java
                  └── proj
      └── data
          └── benchmarks
              └── ass2
```

## Authors

* **Joseph Maheshe** - *Initial work*

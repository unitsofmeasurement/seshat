# Seshat Units of Measurement Implementation

This branch contains the source files for [Seshat web site](https://unitsofmeasurement.github.io/seshat).
For the master branch containing Java source code, see https://github.com/unitsofmeasurement/seshat.

## Note for maintainers
For updating the web site after a new release,
execute the following command in the source code directory:

```
mvn package -Dmaven.javadoc.disableNoFonts=true
```

Move `target/report/apidocs/*` files to the `api/` directory of this repository.
Check the size of the `target/seshat-*.jar` file and update the `index.html` file accordingly.
Adjust the number of classes and the Seshat version number.

# Seshat Units of Measurement Implementation

[![CircleCI](https://circleci.com/gh/unitsofmeasurement/seshat.svg?style=svg)](https://circleci.com/gh/unitsofmeasurement/seshat)
[![Stability: Active](https://masterminds.github.io/stability/active.svg)](https://masterminds.github.io/stability/active.html)
[![License](https://img.shields.io/badge/license-Apache2-red.svg)](http://opensource.org/licenses/apache-2.0)

Seshat (from the ancient Egyptian goddess of knowledge, writing and surveying)
is an implementation of Units of Measurement API defined by JSR 363. Seshat is
a subset of [Apache Spatial Information System (SIS)](http://sis.apache.org/)
library keeping only the classes required for JSR 363 implementation.
Seshat supports arithmetic operations on units and on quantities.
The unit (including SI prefix) and the quantity type resulting from
those arithmetic operations are automatically inferred.
For example this line of code:

```
System.out.println( Units.PASCAL.multiply(1000) );
```

prints "kPa", _i.e._ the kilo prefix has been automatically applied
(SI prefixes are applied on SI units only, not on other systems).
Other example:

```
Force  f = Quantities.create(4, Units.NEWTON);
Length d = Quantities.create(6, Units.MILLIMETRE);
Time   t = Quantities.create(3, Units.SECOND);
Quantity<?> e = f.multiply(d).divide(t);
System.out.println(e);
System.out.println("Instance of Power: " + (e instanceof Power));
```

prints:

```
8 mW
Instance of Power: true
```

In this example, Seshat detects that the result of N*m/s is Watt,
inherits the milli prefix from millimetre and creates an instance
of `Power`, not just `Quantity<Power>` (the generic parent).

Parsing and formatting use Unicode symbols by default, as in µg/m².
Parenthesis are recognized at parsing time and used for denominators at formatting time, as in kg/(m²⋅s).
While uncommon, Seshat accepts fractional powers as in m^⅔.
Some sentences like _"100 feet"_, _"square metre"_ and _"degree Kelvin"_
are also recognized at parsing time.

Seshat requires Java 8 at runtime (Java 10 at compile time)
and has no dependency other than JSR 363 and `java.base` module.


## Maven dependency

Latest release can be used in a Maven project with following configuration:

```
<project>
  <dependencies>
    <dependency>
      <groupId>tech.uom</groupId>
      <artifactId>seshat</artifactId>
      <version>1.0</version>
    </dependency>
  </dependencies>
</project>
```


## Links
* [Web site](https://unitsofmeasurement.github.io/seshat/)
* [Javadoc](https://unitsofmeasurement.github.io/seshat/api/)
* [GitHub](https://github.com/unitsofmeasurement/seshat)

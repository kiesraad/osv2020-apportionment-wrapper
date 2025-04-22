# OSV2020 apportionment wrapper

Prerequisites: Java Development Kit (JDK, tested with [Corretto 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/))

First download the OSV2020 source code:
```
./download-osv2020-code.sh
```
Then run the apportionment wrapper:
```
./gradlew run
```

The apportionment wrapper test case can be changed by editing
`apportionment-wrapper/src/main/java/nl/kiesraad/osv2020_apportionment_wrapper/Main.java`.

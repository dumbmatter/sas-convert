# sas-convert
Simple SAS7BDAT to CSV conversion tool based on the [Parso library](http://lifescience.opensource.epam.com/parso.html)
and [opencsv](http://opencsv.sourceforge.net).

### Usage
This will create file.csv:
```bash
./sas-convert file.sas7bdat
```

If you have multiple SAS files, you can create a CSV file next to each one.

This will create a.csv and b.csv:
```bash
./sas-convert a.sas7bdat b.sas7bdat
```

This will create a.csv and folder/b.csv:
```bash
./sas-convert a.sas7bdat folder/b.sas7bdat

```

This will create one CSV file for each SAS7BDAT file in this folder:
```bash
./sas-convert *.sas7bdat
```

### Build from source
```bash
mvn package
```

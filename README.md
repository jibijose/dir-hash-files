[![Build Status](https://ci.appveyor.com/api/projects/status/github/jibijose/dir-hash-files?branch=master&svg=true)](https://ci.appveyor.com/project/jibijose/dir-hash-files) 

# Introduction
TODO

# Build setup

## Requirements
| Software      | Version  |        Verify |                                            Comments |
|---------------|:--------:|--------------:|----------------------------------------------------:|
| java          |    11    | java -version | Shoud work with java 8 by downgrading it in pom.xml |

## Build locally
`
./mvnw clean package
`
# Usage

## Modes 

| Mode                    | description                                                                                   |
|-------------------------|:----------------------------------------------------------------------------------------------|
| [create](#create)       | Create new fileInfo file from directory/drive                                                 |
| [recreate](#recreate)   | Create new fileInfo file from directory/drive and another fileinfo file                       |
| [compare](#compare)     | Create new hashstatus file from directory(s)/drive(s)/fileinfo(s)                             |
| [recompare](#recompare) | Create new hashstatus file from directory(s)/drive(s)/fileinfo(s) and another hashstatus file |

## Common parameters
| parameter | flag | mandatory | values                                    | description                       |
|-----------|:----:|:---------:|:------------------------------------------|:----------------------------------|
| mode      |  m   |   true    | [create/recreate/compare/recompare]       | mode of application run           |
| hash      |  h   |   false   | [MD2,MD5,SHA,SHA224,SHA256,SHA384,SHA512] | Hash not calculated if not passed |
| password  |  p   |   false   | string                                    | Output file encryption password   |

## Mode based parameters

### create
Creates signature of all files in a directory or drive. Hashing and setting output file pasword are optional.  

| parameter | flag | mandatory | values             | description                        |
|-----------|:----:|:---------:|:-------------------|:-----------------------------------|
| input     |  i   |   true    | Directory or Drive | Any directory or disk drive        |
| output    |  o   |   true    | xlsx file          | Writable xlsx output fileinfo file |

```
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p false -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p false -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p true -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx
```

### recreate
Recreates signature of all files starting with a base file xlsx file. Hashing as per base file, and output file password in optional.  

| parameter   | flag | mandatory | values             | description                        |
|-------------|:----:|:---------:|:-------------------|:-----------------------------------|
| input       |  i   |   true    | Directory or Drive | Any directory or disk drive        |
| fileinfo    |  f   |   true    | xlsx file          | Readable xlsx input fileinfo file  |
| output file |  o   |   true    | xlsx file          | Writable xlsx output fileinfo file |
``` 
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recreate -p false -h MD5 -i ".\src\test\resources\testfiles\leftdir" -f "fileinfo.xlsx" -o "fileinfonew.xlsx"
```

### compare
Compares left, center (optional) and right directory/drive/fileinfo. Hashing should match in all inputs and output file password is optional.  

| parameter    | flag | mandatory | values                               | description                                  |
|--------------|:----:|:---------:|:-------------------------------------|:---------------------------------------------|
| left input   |  l   |   true    | Directory or Drive or fileinfo file  | Any directory or disk drive or fileinfo file |
| center input |  c   |   false   | Directory or Drive or fileinfo file  | Any directory or disk drive or fileinfo file |
| right input  |  r   |   true    | Directory or Drive or fileinfo file  | Any directory or disk drive or fileinfo file |
| output file  |  o   |   true    | xlsx file                            | Writable xlsx output hashstatus file         |
```
/**************  two way comparison checks **********************/
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p true -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  

/**************  three way comparison checks **********************/  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p true -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```

### recompare
Recompares left, center (optional) and right directory/drive/fileinfo starting with a base hashstatus file xlsx file. Hashing should match in all inputs and output file password is optional.  

| parameter    | flag | mandatory | values                              | description                                  |
|--------------|:----:|:---------:|:------------------------------------|:---------------------------------------------|
| left input   |  l   |   true    | Directory or Drive or fileinfo file | Any directory or disk drive or fileinfo file |
| center input |  c   |   false   | Directory or Drive or fileinfo file | Any directory or disk drive or fileinfo file |
| right input  |  r   |   true    | Directory or Drive or fileinfo file | Any directory or disk drive or fileinfo file |
| hashstatus   |  f   |   true    | xlsx file                           | Readable xlsx input hashstatus file          |
| output file  |  o   |   true    | xlsx file                           | Writable xlsx output hashstatus file         |
```
/**************  two way comparison checks **********************/
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p true -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  

/**************  three way comparison checks **********************/  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p true -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx 
```

# TODO
| Priority |     Type      |                                    Description |
|----------|:-------------:|-----------------------------------------------:|
| 2        |  functional   | file names should contain markers and datetime |
| 2        |  automation   |                            Auto script samples |
| 1        | documentation |                                 readme updates |
| 1        |      bug      |         behaviour when disk/drive not readable |
| 1        |  functional   |                                    merge files |
| 1        |  functional   |    create/recreate and compare/recompare merge |

### Usage  
java -jar dirhashfiles-1.0.0-shaded.jar

-m,--mode <arg>         Operation mode, mandatory [create|recreate|compare|recompare]  
-p,--passFlag <arg>     Operation mode, mandatory [true|false]  
-h,--hashalgo <arg>     Hash algorithm, optional [MD2|MD5|SHA|SHA224|SHA256|SHA384|SHA512]  
-i,--indir <arg>        In drive/dir, mandatory for createhash mode  
-l,--leftside <arg>     Left side, mandatory for comparehash mode  
-c,--centerside <arg>   Center side, optional for comparehash mode  
-r,--rightside <arg>    Right side, mandatory for comparehash mode  
-f,--infile <arg>       In file xlsx, mandatory for recreate and recompare modes  
-o,--outfile <arg>      Hash output file, mandatory outfile xlsx file name  
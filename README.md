[![Build Status](https://ci.appveyor.com/api/projects/status/github/jibijose/dir-hash-files?branch=master&svg=true)](https://ci.appveyor.com/project/jibijose/dir-hash-files) 

# Introduction
I searched for a trustable windows/linux application to hash my files in a directory/drive and later (after weeks/months/years) check whether files are corrupted or not. I couldn't find any and then went on with writing one in java.  
It also compares two/three sources and report misatches. Sources could be hash signature or directory or drive.  
  
Assuming you have two or three backups of your files in drives/directories, with the output you can easily figure out which file got corrupted and do the corrections manually. If I get time I will add one more mode to auto-sync files.  

# Build setup

## Requirements
| Software      | Version |        Verify |                                               Comments |
|---------------|:-------:|--------------:|-------------------------------------------------------:|
| java          |  11+    | java -version | Any version 8 or above<br/>Check appveyor build status |

## Build locally
```
./mvnw clean package
```
Runnable jar target\dirhashfiles-1.0.0-shaded.jar is created.  

# Usage
java -jar target\dirhashfiles-1.0.0-shaded.jar

-m,--mode <arg>         Operation mode, mandatory [create|compare|merge|recreate|recompare]  
-p,--passFlag <arg>     Password mode, mandatory [true|false]  
-h,--hashalgo <arg>     Hash algorithm, optional [MD2|MD5|SHA|SHA224|SHA256|SHA384|SHA512]  
-i,--indir <arg>        In drive/dir, mandatory for createhash mode  
-l,--leftside <arg>     Left side, mandatory for comparehash mode  
-c,--centerside <arg>   Center side, optional for comparehash mode  
-r,--rightside <arg>    Right side, mandatory for comparehash mode  
-f,--infile <arg>       In file xlsx, mandatory for recreate and recompare modes  
-o,--outfile <arg>      Hash output file, mandatory outfile xlsx file name

## Modes 

| Mode                    | description                                                                                   |
|-------------------------|:----------------------------------------------------------------------------------------------|
| [create](#create)       | Create new fileInfo file from directory/drive                                                 |
| [compare](#compare)     | Create new hashstatus file from directory(s)/drive(s)/fileinfo(s)                             |
| [merge](#merge)         | Merge many fileInfo/hashstatus[two/three] files                                               |
| [recreate](#recreate)   | Create new fileInfo file from directory/drive and another fileinfo file                       |
| [recompare](#recompare) | Create new hashstatus file from directory(s)/drive(s)/fileinfo(s) and another hashstatus file |

## Common parameters
| parameter   | flag | mandatory | values                                    | description                                     |
|-------------|:----:|:---------:|:------------------------------------------|:------------------------------------------------|
| mode        |  m   |   true    | [merge/create/recreate/compare/recompare] | mode of application run                         |
| hash        |  h   |   false   | [MD2,MD5,SHA,SHA224,SHA256,SHA384,SHA512] | Hash not calculated if not passed               |
| password    |  p   |   false   | string                                    | Output file encryption password                 |
| output file |  o   |   true    | xlsx file                                 | Writable xlsx output file [fileinfo/hashstatus] |
## Mode based parameters


### create
Creates signature of all files in a directory or drive. Hashing and setting output file password are optional.  

| parameter | flag | mandatory | values             | description                        |
|-----------|:----:|:---------:|:-------------------|:-----------------------------------|
| input     |  i   |   true    | Directory or Drive | Any directory or disk drive        |

```
############ create fileinfo file without hash or password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p false -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx
```
```
############ create fileinfo file with hash and without password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p false -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  
```
```
############ create fileinfo file with hash and password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p true -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx
```

### compare
Compares left, center (optional) and right directory/drive/fileinfo. Hashing should match in all inputs and output file password is optional.  

| parameter    | flag | mandatory | values                               | description                                  |
|--------------|:----:|:---------:|:-------------------------------------|:---------------------------------------------|
| left input   |  l   |   true    | Directory or Drive or fileinfo file  | Any directory or disk drive or fileinfo file |
| center input |  c   |   false   | Directory or Drive or fileinfo file  | Any directory or disk drive or fileinfo file |
| right input  |  r   |   true    | Directory or Drive or fileinfo file  | Any directory or disk drive or fileinfo file |

```
############ two way comparison without hash or password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```
```
############ two way comparison with hash and without password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```
```
############ two way comparison with hash and password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p true -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```
<br></br>
```
############ three way comparison without hash or password ###########################
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```
```
############ three way comparison with hash and without password ###########################
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```
```
############ three way comparison with hash and password ###########################
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p true -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
```

### merge
Merges output files fileino/hashstatus[two/three]. Useful if you want to run in batches and later combine outputs.

| parameter | flag | mandatory | values              | description                                                   |
|-----------|:----:|:---------:|:--------------------|:--------------------------------------------------------------|
| files     |  s   |   true    | files list to merge | comma separated list of fileinfo/hashstatus[two/three] files  |

```
############ merge fileinfo files ##########################################################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m merge -p false -h MD5 -s "fileinfo1.xlsx,fileinfo2.xlsx" -o fileinfomerged.xlsx
```
```
############ merge hashstatus two files ##########################################################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m merge -p false -h MD5 -s "hashstatustwo1.xlsx,hashstatustwo2.xlsx" -o hashstatustwomerged.xlsx 
```
```
############ merge hashstatus three files ##########################################################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m merge -p false -h MD5 -s "hashstatusthree1.xlsx,hashstatusthree2.xlsx" -o hashstatusthreemerged.xlsx
```

### recreate
Recreates signature of all files starting with a base file xlsx file. Hashing as per base file, and output file password in optional.

| parameter   | flag | mandatory | values             | description                        |
|-------------|:----:|:---------:|:-------------------|:-----------------------------------|
| input       |  i   |   true    | Directory or Drive | Any directory or disk drive        |
| fileinfo    |  f   |   true    | xlsx file          | Readable xlsx input fileinfo file  |
``` 
############ recreate fileinfo file with hash and without password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recreate -p false -h MD5 -i ".\src\test\resources\testfiles\leftdir" -f "fileinfo.xlsx" -o "fileinfonew.xlsx"
```

### recompare
Recompares left, center (optional) and right directory/drive/fileinfo starting with a base hashstatus file xlsx file. Hashing should match in all inputs and output file password is optional.

| parameter    | flag | mandatory | values                              | description                                  |
|--------------|:----:|:---------:|:------------------------------------|:---------------------------------------------|
| left input   |  l   |   true    | Directory or Drive or fileinfo file | Any directory or disk drive or fileinfo file |
| center input |  c   |   false   | Directory or Drive or fileinfo file | Any directory or disk drive or fileinfo file |
| right input  |  r   |   true    | Directory or Drive or fileinfo file | Any directory or disk drive or fileinfo file |
| hashstatus   |  f   |   true    | xlsx file                           | Readable xlsx input hashstatus file          |

/**************  two way recomparisons **********************/
```
############ two way recomparison without hash or password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
```
```
############ two way recomparison with hash and without password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
```
```
############ two way recomparison with hash and password ###########################
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p true -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
```
<br></br>
```
############ three way recomparison without hash or password ###########################
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
```
```
############ three way recomparison with hash and without password ###########################
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
```
```
############ three way recomparison with hash and password ###########################
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p true -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx 
```


# TODO
| Priority | Type          | Description                                    |
|----------|:--------------|------------------------------------------------|
| 2        | functional    | file names should contain markers and datetime |
| 2        | automation    | Auto script samples                            |
| 3        | functional    | manual/auto sync mode                          |
| 3        | functional    | include empty directory hashing?               |
 
[![Build Status](https://ci.appveyor.com/api/projects/status/github/jibijose/dir-hash-files?branch=master&svg=true)](https://ci.appveyor.com/project/jibijose/dir-hash-files) 


### Requirements

* Java 11
* Maven 3.0.0 or newer.

### Usage  
java -jar dirhashfiles.jar

-m,--mode <arg>         Operation mode, mandatory [createhash|comparehash]  
-h,--hashalgo <arg>     Hash algorithm, optional [MD2|MD5|SHA|SHA224|SHA256|SHA384|SHA512]  
-i,--indir <arg>        In drive/dir, mandatory for createhash mode  
-l,--leftside <arg>     Left side, mandatory for comparehash mode  
-c,--centerside <arg>   Center side, optional for comparehash mode  
-r,--rightside <arg>    Right side, mandatory for comparehash mode  
-o,--outfile <arg>      Hash output file, mandatory outfile xlsx file name  

### Run locally
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  

/**************  two way comparison checks **********************/  
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  

/**************  three way comparison checks **********************/  
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  

### Build locally
./mvnw clean package

*************
### TODOs
junit tests.  
two drives in two diff thread pools?  
time left?  
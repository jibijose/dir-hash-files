[![Build Status](https://ci.appveyor.com/api/projects/status/github/jibijose/dir-hash-files?branch=master&svg=true)](https://ci.appveyor.com/project/jibijose/dir-hash-files) 


### Requirements

* Java 11
* Maven 3.0.0 or newer.

### Usage  
java -jar dirhashfiles-1.0.0-shaded.jar

-m,--mode <arg>         Operation mode, mandatory [create|recreate|compare|recompare]  
-p,--passFlag <arg>     Operation mode, mandatory [true|false]  
-h,--hashalgo <arg>     Hash algorithm, optional [MD2|MD5|SHA|SHA224|SHA256|SHA384|SHA512]  
-i,--indir <arg>        In drive/dir, mandatory for createhash mode  
-l,--leftside <arg>     Left side, mandatory for comparehash mode  
-c,--centerside <arg>   Center side, optional for comparehash mode  
-r,--rightside <arg>    Right side, mandatory for comparehash mode  
-o,--outfile <arg>      Hash output file, mandatory outfile xlsx file name  

### Run locally 
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p false -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p false -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m create -p true -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx  

/**************  two way comparison checks **********************/
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p true -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  

/**************  three way comparison checks **********************/  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p false -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  
java -Xms1g -Xmx4g -jar target\dirhashfiles-1.0.0-shaded.jar -m compare -p true -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx  

/**************  recreate hash **********************/  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recreate -p false -h MD5 -i ".\src\test\resources\testfiles\leftdir" -f "fileinfo.xlsx" -o "fileinfonew.xlsx"

/**************  recalculate comparison checks **********************/  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  
java -jar target\dirhashfiles-1.0.0-shaded.jar -m recompare -p false -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -f "hashstatus.xlsx" -o hashstatusnew.xlsx  


### Build locally
./mvnw clean package

*************
### TODOs
time left?
filename containing marker + datetime  
check unix file system  
provide auto script.
readme details 
cant mix between windows and linux due to slash+back slash problem  
hash prevails even if date is mismatched...   
check out file writable or not...  
works primarily with windows and mounted drives (ntfs/fat/exfat)  
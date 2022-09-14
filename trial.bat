call mvn clean package

call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -h MD5 -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -i ".\src\test\resources\testfiles\leftdir" -o fileinfo.xlsx

/**************  two way comparison checks **********************/
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -h MD5 -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -l "fileinfo.xlsx" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx

/**************  three way comparison checks **********************/
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -h MD5 -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -l "fileinfo.xlsx" -c ".\src\test\resources\testfiles\centerdir" -r ".\src\test\resources\testfiles\rightdir" -o hashstatus.xlsx
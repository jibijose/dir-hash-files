

call mvn clean package



call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -i "C:\pubgroupe" -o fileinfo.xlsx



call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -l "fileinfo.xlsx" -r "C:\PB" -o hashstatus.xlsx
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -l "C:\pubgroupe" -r "C:\PB" -o hashstatus.xlsx

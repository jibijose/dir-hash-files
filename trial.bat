call mvn clean package

call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -d "C:\pubgroupe" -o jj.sig
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m checkhash -d "C:\PB" -i jj.sig
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -dl "C:\pubgroupe" -dr "C:\PB"
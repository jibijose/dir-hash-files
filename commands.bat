
mvn clean package

### SMALL TEST FILES
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -d "I:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins" -o jj.sig
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m checkhash -d "R:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins" -i jj.sig
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -dl "I:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins" -dr "R:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins"



### MEDIUM TEST FILES
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -d "I:\EBOOKS" -o jj.sig
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m checkhash -d "R:\EBOOKS" -i jj.sig
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -dl "I:\EBOOKS" -dr "R:\EBOOKS"


### BIG TEST FILES
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -d "I:\TO_SYNC_UNSORTED" -o jj.sig
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m checkhash -d "R:\TO_SYNC_UNSORTED" -i jj.sig
java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -dl "I:\TO_SYNC_UNSORTED" -dr "R:\TO_SYNC_UNSORTED"


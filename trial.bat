call mvn clean package

call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m createhash -d "I:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins" -o jj.sig
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m checkhash -d "R:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins" -i jj.sig
call java -jar target\dirhashfiles-1.0-SNAPSHOT-shaded.jar -m comparehash -dl "I:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins" -dr "R:\EBOOKS\CHESS EBOOKS\Karpov's Strategic Wins"
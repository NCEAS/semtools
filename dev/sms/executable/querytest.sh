echo "QueryProcessor"

java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 1 1 > resultlog/testquery1_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 2 1 > resultlog/testquery1_2_1.txt
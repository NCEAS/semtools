echo "QueryProcessor"

echo "query with one condition" 
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 1 1 > resultlog/testquery1_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 2 1 > resultlog/testquery1_2_1.txt

echo "query with two conditions (one aggregate, one non-aggregate)"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query2.txt 1 1 > resultlog/testquery2_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query2.txt 2 1 > resultlog/testquery2_2_1.txt

echo "query with context"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query3.txt 1 1 > resultlog/testquery3_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query3.txt 1 1 > resultlog/testquery3_1_1.txt
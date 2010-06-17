echo "QueryProcessor"

echo "query with one condition" 
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 1 oboe_syn 0 > resultlog/testquery1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 2 oboe_syn 0 > resultlog/testquery1_mdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 1 oboe_syn 1 > resultlog/testquery1_rdb_drid.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query1.txt 2 oboe_syn 1 > resultlog/testquery1_mdb_drid.txt

echo "query with two conditions (one aggregate, one non-aggregate)"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query2.txt 1 oboe_syn 1 > resultlog/testquery2_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query2.txt 2 oboe_syn 1 > resultlog/testquery2_2_1.txt

echo "query with context"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query3.txt 1 oboe_syn 1 > resultlog/testquery3_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query3.txt 2 oboe_syn 1 > resultlog/testquery3_2_1.txt

echo "query with multiple context"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query4.txt 1 oboe_syn 1 > resultlog/testquery4_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor test1-query4.txt 2 oboe_syn 1 > resultlog/testquery4_2_1.txt

echo "query real data"
echo "tested the following two"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor mobrien-query1.txt 1 oboe_real 1 > resultlog/mobrienquery1_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor mobrien-query1.txt 2 oboe_real 1 > resultlog/mobrienquery1_2_1.txt


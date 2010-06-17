
echo "query real data"
echo "tested the following two"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor mobrien-query1.txt 1 oboe_real 1 > resultlog/mobrienquery1_1_1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor mobrien-query1.txt 2 oboe_real 1 > resultlog/mobrienquery1_2_1.txt


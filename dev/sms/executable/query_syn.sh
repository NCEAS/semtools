
echo "query synthetic data"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 1 oboe_syn 1 >resultlog/query_m5_s0.01_m1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 2 oboe_syn 1 >resultlog/query_m5_s0.01_m2.txt

java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.05 1 oboe_syn 1 >resultlog/query_m5_s0.05_m1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.05 2 oboe_syn 1 >resultlog/query_m5_s0.05_m2.txt

java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 1 oboe_syn 1 >resultlog/query_m5_s0.1_m1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 2 oboe_syn 1 >resultlog/query_m5_s0.1_m2.txt

java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.2 1 oboe_syn 1 >resultlog/query_m5_s0.2_m1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.2 2 oboe_syn 1 >resultlog/query_m5_s0.2_m2.txt

java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 1 oboe_syn 1 >resultlog/query_m5_s0.5_m1.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 2 oboe_syn 1 >resultlog/query_m5_s0.5_m2.txt

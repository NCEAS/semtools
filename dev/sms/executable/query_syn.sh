
echo "query synthetic data"
echo "=========="
echo "Get both dataset id and record id"
echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 1 oboe_syn 1 >resultlog/query_m5_s0.01_rdb_drid.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 2 oboe_syn 1 >resultlog/query_m5_s0.01_mdb_drid.txt

echo "selectivity=0.05"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.05 1 oboe_syn 1 >resultlog/query_m5_s0.05_rdb_drid.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.05 2 oboe_syn 1 >resultlog/query_m5_s0.05_mdb_drid.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 1 oboe_syn 1 >resultlog/query_m5_s0.1_rdb_drid.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 2 oboe_syn 1 >resultlog/query_m5_s0.1_mdb_drid.txt

echo "selectivity=0.2"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.2 1 oboe_syn 1 >resultlog/query_m5_s0.2_rdb_drid.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.2 2 oboe_syn 1 >resultlog/query_m5_s0.2_mdb_drid.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 1 oboe_syn 1 >resultlog/query_m5_s0.5_rdb_drid.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 2 oboe_syn 1 >resultlog/query_m5_s0.5_mdb_drid.txt

echo "=========="
echo "Get only dataset id"
echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 1 oboe_syn 0 >resultlog/query_m5_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 2 oboe_syn 0 >resultlog/query_m5_s0.01_mdb_did.txt

echo "selectivity=0.05"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.05 1 oboe_syn 0 >resultlog/query_m5_s0.05_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.05 2 oboe_syn 0 >resultlog/query_m5_s0.05_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 1 oboe_syn 0 >resultlog/query_m5_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 2 oboe_syn 0 >resultlog/query_m5_s0.1_mdb_did.txt

echo "selectivity=0.2"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.2 1 oboe_syn 0 >resultlog/query_m5_s0.2_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.2 2 oboe_syn 0 >resultlog/query_m5_s0.2_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 1 oboe_syn 0 >resultlog/query_m5_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 2 oboe_syn 0 >resultlog/query_m5_s0.5_mdb_did.txt

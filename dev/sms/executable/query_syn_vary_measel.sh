 
echo "Get only dataset id"
echo "m1 (1.0)"
echo "selectivity=0.001"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.0010 1 oboe_syn 0 >resultlog/query_m1_s0.001_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.0010 2 oboe_syn 0 >resultlog/query_m1_s0.001_mdb_did.txt

echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.01 1 oboe_syn 0 >resultlog/query_m1_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.01 2 oboe_syn 0 >resultlog/query_m1_s0.01_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.1 1 oboe_syn 0 >resultlog/query_m1_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.1 2 oboe_syn 0 >resultlog/query_m1_s0.1_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.5 1 oboe_syn 0 >resultlog/query_m1_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m1_s0.5 2 oboe_syn 0 >resultlog/query_m1_s0.5_mdb_did.txt

echo "============="
echo "m3 (1.0)"
echo "selectivity=0.001"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.0010 1 oboe_syn 0 >resultlog/query_m3_s0.001_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.0010 2 oboe_syn 0 >resultlog/query_m3_s0.001_mdb_did.txt

echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.01 1 oboe_syn 0 >resultlog/query_m3_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.01 2 oboe_syn 0 >resultlog/query_m3_s0.01_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.1 1 oboe_syn 0 >resultlog/query_m3_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.1 2 oboe_syn 0 >resultlog/query_m3_s0.1_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.5 1 oboe_syn 0 >resultlog/query_m3_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m3_s0.5 2 oboe_syn 0 >resultlog/query_m3_s0.5_mdb_did.txt

echo "============="
echo "m5 (0.5)"
echo "selectivity=0.001"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.0010 1 oboe_syn 0 >resultlog/query_m5_s0.001_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.0010 2 oboe_syn 0 >resultlog/query_m5_s0.001_mdb_did.txt

echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 1 oboe_syn 0 >resultlog/query_m5_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.01 2 oboe_syn 0 >resultlog/query_m5_s0.01_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 1 oboe_syn 0 >resultlog/query_m5_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.1 2 oboe_syn 0 >resultlog/query_m5_s0.1_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 1 oboe_syn 0 >resultlog/query_m5_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m5_s0.5 2 oboe_syn 0 >resultlog/query_m5_s0.5_mdb_did.txt

echo "============="
echo "m7 (0.2)"
echo "selectivity=0.001"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.0010 1 oboe_syn 0 >resultlog/query_m7_s0.001_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.0010 2 oboe_syn 0 >resultlog/query_m7_s0.001_mdb_did.txt

echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.01 1 oboe_syn 0 >resultlog/query_m7_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.01 2 oboe_syn 0 >resultlog/query_m7_s0.01_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.1 1 oboe_syn 0 >resultlog/query_m7_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.1 2 oboe_syn 0 >resultlog/query_m7_s0.1_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.5 1 oboe_syn 0 >resultlog/query_m7_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m7_s0.5 2 oboe_syn 0 >resultlog/query_m7_s0.5_mdb_did.txt

echo "============="
echo "m9 (0.1)"
echo "selectivity=0.001"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.0010 1 oboe_syn 0 >resultlog/query_m9_s0.001_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.0010 2 oboe_syn 0 >resultlog/query_m9_s0.001_mdb_did.txt

echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.01 1 oboe_syn 0 >resultlog/query_m9_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.01 2 oboe_syn 0 >resultlog/query_m9_s0.01_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.1 1 oboe_syn 0 >resultlog/query_m9_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.1 2 oboe_syn 0 >resultlog/query_m9_s0.1_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.5 1 oboe_syn 0 >resultlog/query_m9_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m9_s0.5 2 oboe_syn 0 >resultlog/query_m9_s0.5_mdb_did.txt

echo "============="
echo "m11 (0.01)"
echo "selectivity=0.001"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.0010 1 oboe_syn 0 >resultlog/query_m11_s0.001_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.0010 2 oboe_syn 0 >resultlog/query_m11_s0.001_mdb_did.txt

echo "selectivity=0.01"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.01 1 oboe_syn 0 >resultlog/query_m11_s0.01_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.01 2 oboe_syn 0 >resultlog/query_m11_s0.01_mdb_did.txt

echo "selectivity=0.1"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.1 1 oboe_syn 0 >resultlog/query_m11_s0.1_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.1 2 oboe_syn 0 >resultlog/query_m11_s0.1_mdb_did.txt

echo "selectivity=0.5"
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.5 1 oboe_syn 0 >resultlog/query_m11_s0.5_rdb_did.txt
java -cp oboe.jar org.ecoinformatics.oboe.QueryProcessor query_m11_s0.5 2 oboe_syn 0 >resultlog/query_m11_s0.5_mdb_did.txt
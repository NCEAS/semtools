
echo "1. Generate annotation specifications and test data"
java -cp oboe.jar org.ecoinformatics.oboe.AnnotSpecGenerator syn 20
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_1 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_2 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_3 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_4 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_5 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_6 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_7 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_8 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_9 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_10 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_11 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_12 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_13 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_14 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_15 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_16 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_17 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_18 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_19 5000
java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGenerator syn_20 5000
echo
echo "2. Clean database first..."
echo " (0. both, 1: mdb, 2: rawdb)"
echo
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_1-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_2-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_3-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_4-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_5-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_6-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_7-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_8-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_9-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_10-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_11-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_12-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_13-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_14-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_15-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_16-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_17-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_18-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_19-n5000-data.txt oboe_syn 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_20-n5000-data.txt oboe_syn 0
echo
echo "3. Load synthetic data ............."
echo "need to run sync_data_generator.sh first"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_1-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_2-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_3-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_4-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_5-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_6-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_7-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_8-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_9-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_10-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_11-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_12-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_13-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_14-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_15-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_16-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_17-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_18-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_19-n5000-data.txt oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader syn_20-n5000-data.txt oboe_syn
echo
echo "4. Materialize synthatic data........."
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_1-annot.xml syn_1-n5000-data.txt syn_1 oboe_syn >resultlog/syn_1.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_2-annot.xml syn_2-n5000-data.txt syn_2 oboe_syn >resultlog/syn_2.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_3-annot.xml syn_3-n5000-data.txt syn_3 oboe_syn >resultlog/syn_3.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_4-annot.xml syn_4-n5000-data.txt syn_4 oboe_syn >resultlog/syn_4.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_5-annot.xml syn_5-n5000-data.txt syn_5 oboe_syn >resultlog/syn_5.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_6-annot.xml syn_6-n5000-data.txt syn_6 oboe_syn >resultlog/syn_6.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_7-annot.xml syn_7-n5000-data.txt syn_7 oboe_syn >resultlog/syn_7.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_8-annot.xml syn_8-n5000-data.txt syn_8 oboe_syn >resultlog/syn_8.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_9-annot.xml syn_9-n5000-data.txt syn_9 oboe_syn >resultlog/syn_9.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_10-annot.xml syn_10-n5000-data.txt syn_10 oboe_syn >resultlog/syn_10.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_11-annot.xml syn_11-n5000-data.txt syn_11 oboe_syn >resultlog/syn_11.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_12-annot.xml syn_12-n5000-data.txt syn_12 oboe_syn >resultlog/syn_12.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_13-annot.xml syn_13-n5000-data.txt syn_13 oboe_syn >resultlog/syn_13.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_14-annot.xml syn_14-n5000-data.txt syn_14 oboe_syn >resultlog/syn_14.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_15-annot.xml syn_15-n5000-data.txt syn_15 oboe_syn >resultlog/syn_15.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_16-annot.xml syn_16-n5000-data.txt syn_16 oboe_syn >resultlog/syn_16.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_17-annot.xml syn_17-n5000-data.txt syn_17 oboe_syn >resultlog/syn_17.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_18-annot.xml syn_18-n5000-data.txt syn_18 oboe_syn >resultlog/syn_18.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_19-annot.xml syn_19-n5000-data.txt syn_19 oboe_syn >resultlog/syn_19.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null syn_20-annot.xml syn_20-n5000-data.txt syn_20 oboe_syn >resultlog/syn_20.txt


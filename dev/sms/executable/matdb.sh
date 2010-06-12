echo "MaterializeDB"

echo "test data" 
java -cp oboe.jar org.ecoinformatics.oboe.MaterializeDB null test1-annot.xml test1-data.txt test1 
java -cp oboe.jar org.ecoinformatics.oboe.MaterializeDB null test2-annot.xml test2-data.txt test2

echo "Real data from Margarat"
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null mobrien.36.59.xml mobrien.36.59.stream_chemistry_allyears.txt mobrien.36.59 >resultlog/mobrien.36.59.mat.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null mobrien.38.19.xml mobrien.38.19.AB00_allyears.txt mobrien.38.19 >resultlog/mobrien.38.19.mat.txt

echo "Materialize synthatic data"
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

echo "Clean the materialized db"
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_1-n5000-data.txt oboe_syn 1
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_1-n5000-data.txt oboe_syn 2

java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner syn_2-n5000-data.txt oboe_syn 1
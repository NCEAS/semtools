echo "RawDataLoader"

echo "Load test data"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader test1-data.txt oboe_test
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader test2-data.txt oboe_test

echo "Load real data"
echo "12 attributes, 21373 rows"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader mobrien.36.59.stream_chemistry_allyears.txt oboe_real
echo "4 attributes, 51531 rows"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader mobrien.38.19.AB00_allyears.txt oboe_real

echo "synthetic data loader"
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

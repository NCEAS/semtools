echo "RawDataLoader"

echo "Load test data"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader test1-data.txt oboe_test
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader test2-data.txt oboe_test

echo "Load real data"
echo "12 attributes, 21373 rows"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader mobrien.36.59.stream_chemistry_allyears.txt oboe_real
echo "4 attributes, 51531 rows"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader mobrien.38.19.AB00_allyears.txt oboe_real

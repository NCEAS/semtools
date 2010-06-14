
echo "============================="
echo "1. Clean test data ...."

java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner test1-data.txt oboe_test 0
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.TBCleaner test2-data.txt oboe_test 0

echo "============================="
echo "2. Load test data ...."
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader test1-data.txt oboe_test
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader test2-data.txt oboe_test

echo "============================="
echo "3. Materialize test data..." 
java -cp oboe.jar org.ecoinformatics.oboe.MaterializeDB null test1-annot.xml test1-data.txt test1 oboe_syn
java -cp oboe.jar org.ecoinformatics.oboe.MaterializeDB null test2-annot.xml test2-data.txt test2 oboe_syn


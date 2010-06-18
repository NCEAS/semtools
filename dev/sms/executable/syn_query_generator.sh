

java -cp oboe.jar org.ecoinformatics.oboe.SyntheticDataGeneratorChain 2000 5000

echo "Load test data"
java -cp oboe.jar org.ecoinformatics.oboe.QueryGenerator syn 20

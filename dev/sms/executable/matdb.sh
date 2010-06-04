echo "MaterializeDB"

echo "test data" 
java -cp oboe.jar org.ecoinformatics.oboe.MaterializeDB null test1-annot.xml test1-data.txt test1 
java -cp oboe.jar org.ecoinformatics.oboe.MaterializeDB null test2-annot.xml test2-data.txt test2

echo "Real data from Margarat"
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null mobrien.36.59.xml mobrien.36.59.stream_chemistry_allyears.txt mobrien.36.59 >resultlog/mobrien.36.59.mat.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null mobrien.38.19.xml mobrien.38.19.AB00_allyears.txt mobrien.38.19 >resultlog/mobrien.38.19.mat.txt